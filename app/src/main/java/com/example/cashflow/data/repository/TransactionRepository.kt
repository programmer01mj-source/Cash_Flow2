package com.example.cashflow.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class TransactionDocument(
    val txnId: String = "",
    val borrowerEmail: String = "",
    val amount: Double = 0.0,
    val mode: String = "",
    val type: String = "Payback",
    val date: String = ""
)

interface TransactionRepository {
    suspend fun recordTransaction(txn: TransactionDocument): Result<String>
    suspend fun getTransactionsForUser(email: String): List<TransactionDocument>
    suspend fun getAllTransactions(): List<TransactionDocument>
}

class FirebaseTransactionRepository : TransactionRepository {
    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun recordTransaction(txn: TransactionDocument): Result<String> {
        return try {
            val docRef = firestore.collection("transactions").document()
            val txnWithId = txn.copy(txnId = docRef.id)
            docRef.set(txnWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTransactionsForUser(email: String): List<TransactionDocument> {
        return try {
            val snapshot = firestore.collection("transactions").whereEqualTo("borrowerEmail", email).get().await()
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
