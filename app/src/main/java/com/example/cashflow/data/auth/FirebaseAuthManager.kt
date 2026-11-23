package com.example.cashflow.data.auth

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    val currentUser = auth.currentUser

    fun getFriendlyErrorMessage(e: Exception): String {
        val errorCode = (e as? FirebaseAuthException)?.errorCode ?: ""
        return when {
            e is FirebaseNetworkException || e.message?.contains("network", true) == true ->
                "No internet connection. Please check your connection and try again."
            errorCode == "ERROR_EMAIL_ALREADY_IN_USE" || e.message?.contains("email-already-in-use", true) == true ->
                "An account with this email already exists. Please log in instead."
            errorCode == "ERROR_INVALID_EMAIL" || e.message?.contains("invalid-email", true) == true ->
                "Please enter a valid email address."
            errorCode == "ERROR_WEAK_PASSWORD" || e.message?.contains("weak-password", true) == true ->
                "Your password is too weak. Please use a stronger password."
            errorCode == "ERROR_WRONG_PASSWORD" || errorCode == "ERROR_USER_NOT_FOUND" || errorCode == "INVALID_LOGIN_CREDENTIALS" || e.message?.contains("invalid-credential", true) == true ->
                "The email or password is incorrect. Please check your details and try again."
            errorCode == "ERROR_USER_DISABLED" ->
                "Your account is currently unavailable. Please contact support."
            errorCode == "CONFIGURATION_NOT_FOUND" || e.message?.contains("CONFIGURATION_NOT_FOUND", true) == true ->
                "We are currently unable to process your registration. Please try again later."
            else ->
                e.localizedMessage ?: "Something went wrong while creating your account. Please try again later."
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                // Profile recovery check: ensure users/{uid} document exists in Firestore
                try {
                    val userDocRef = firestore.collection("users").document(user.uid)
                    val docSnapshot = userDocRef.get().await()
                    if (!docSnapshot.exists()) {
                        val inferredRole = if (email.equals("admin@qmail.com", true) || email.equals("Admin@gmail.com", true)) "Admin"
                                           else if (email.equals("officer@gmail.com", true)) "Loan Officer"
                                           else "Customer"
                        val recoveryData = hashMapOf(
                            "uid" to user.uid,
                            "name" to email.substringBefore("@").replaceFirstChar { it.uppercase() },
                            "email" to email,
                            "phone" to "",
                            "role" to inferredRole,
                            "kycStatus" to "Pending Documents",
                            "createdAt" to Timestamp.now()
                        )
                        userDocRef.set(recoveryData, SetOptions.merge()).await()
                    }
                } catch (recoveryEx: Exception) {
                    Log.e("FirebaseAuthManager", "Profile recovery failed during login", recoveryEx)
                }

                Result.success(user)
            } else {
                Result.failure(Exception("The email or password is incorrect. Please check your details and try again."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(getFriendlyErrorMessage(e)))
        }
    }

    suspend fun signUpWithEmail(name: String, email: String, password: String, phone: String, requestedRole: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                try {
                    // Secure role assignment: prevent unauthorized role escalation
                    val secureRole = if (email.equals("admin@qmail.com", true) || email.equals("Admin@gmail.com", true)) "Admin"
                                     else if (email.equals("officer@gmail.com", true)) "Loan Officer"
                                     else "Customer"

                    val userData = hashMapOf(
                        "uid" to user.uid,
                        "name" to name,
                        "email" to email,
                        "phone" to phone,
                        "role" to secureRole,
                        "kycStatus" to "Pending Documents",
                        "createdAt" to Timestamp.now()
                    )
                    firestore.collection("users").document(user.uid).set(userData, SetOptions.merge()).await()
                    Result.success(user)
                } catch (firestoreEx: Exception) {
                    Log.e("FirebaseAuthManager", "Firestore profile creation failed", firestoreEx)
                    Result.failure(Exception("Profile setup failed: ${firestoreEx.localizedMessage ?: "Unknown Firestore error"}"))
                }
            } else {
                Result.failure(Exception("Something went wrong while creating your account. Please try again later."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(getFriendlyErrorMessage(e)))
        }
    }

    suspend fun getUserRole(uid: String): String {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            val rawRole = doc.getString("role") ?: "Customer"
            when {
                rawRole.contains("admin", true) -> "Admin"
                rawRole.contains("officer", true) || rawRole.contains("loan", true) -> "Loan Officer"
                else -> "Customer"
            }
        } catch (_: Exception) {
            "Customer"
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
