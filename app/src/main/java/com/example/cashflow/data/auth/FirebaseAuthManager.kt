package com.example.cashflow.data.auth

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    val currentUser = auth.currentUser

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Authentication failed: User is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(name: String, email: String, password: String, phone: String, role: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                val userData = hashMapOf(
                    "uid" to user.uid,
                    "name" to name,
                    "email" to email,
                    "phone" to phone,
                    "role" to role,
                    "kycStatus" to "Pending Documents",
                    "createdAt" to Timestamp.now()
                )
                firestore.collection("users").document(user.uid).set(userData).await()
                Result.success(user)
            } else {
                Result.failure(Exception("Sign up failed: User is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserRole(uid: String): String {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            doc.getString("role") ?: "Customer"
        } catch (e: Exception) {
            "Customer"
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
