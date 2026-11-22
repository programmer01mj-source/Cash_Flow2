package com.example.cashflow.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "Customer",
    val kycStatus: String = "Pending Documents",
    val nationalId: String = "",
    val dob: String = ""
)

interface UserRepository {
    suspend fun getUserProfile(uid: String): UserProfile?
    suspend fun updateUserProfile(uid: String, updates: Map<String, Any>): Result<Unit>
}

class FirebaseUserRepository : UserRepository {
    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) {
                UserProfile(
                    uid = doc.getString("uid") ?: uid,
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    phone = doc.getString("phone") ?: "",
                    role = doc.getString("role") ?: "Customer",
                    kycStatus = doc.getString("kycStatus") ?: "Pending Documents",
                    nationalId = doc.getString("nationalId") ?: "",
                    dob = doc.getString("dob") ?: ""
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateUserProfile(uid: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection("users").document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
