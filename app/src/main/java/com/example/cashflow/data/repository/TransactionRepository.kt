package com.example.cashflow.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await

data class TransactionDocument(
    val txnId: String = "",
    val borrowerId: String = "",
    val borrowerEmail: String = "",
    val amount: Double = 0.0,
    val mode: String = "",
    val type: String = "Payback",
    val date: String = "",
    val createdAt: Timestamp? = null
)

interface TransactionRepository {
    suspend fun recordTransaction(amount: Double, mode: String, type: String, date: String): Result<String>
    suspend fun getTransactionsForUser(uid: String): List<TransactionDocument>
    suspend fun getAllTransactions(): List<TransactionDocument>
}

class FirebaseTransactionRepository : TransactionRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override suspend fun recordTransaction(amount: Double, mode: String, type: String, date: String): Result<String> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("User not authenticated"))
            val docRef = firestore.collection("transactions").document()
            val txn = TransactionDocument(
                txnId = docRef.id,
                borrowerId = user.uid,
                borrowerEmail = user.email ?: "",
                amount = amount,
                mode = mode,
                type = type,
                date = date,
                createdAt = Timestamp.now()
            )
            docRef.set(txn).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTransactionsForUser(uid: String): List<TransactionDocument> {
        return try {
            val snapshot = firestore.collection("transactions").whereEqualTo("borrowerId", uid).get().await()
            snapshot.documents.mapNotNull { it.toObject(TransactionDocument::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getAllTransactions(): List<TransactionDocument> {
        return try {
            val snapshot = firestore.collection("transactions").get().await()
            snapshot.documents.mapNotNull { it.toObject(TransactionDocument::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
