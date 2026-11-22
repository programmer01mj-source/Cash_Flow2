package com.example.cashflow.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class LoanDocument(
    val loanId: String = "",
    val borrowerEmail: String = "",
    val amount: Double = 0.0,
    val tenure: Int = 12,
    val income: Double = 0.0,
    val status: String = "Pending",
    val nationalId: String = "",
    val dob: String = ""
)

interface LoanRepository {
    suspend fun applyForLoan(loan: LoanDocument): Result<String>
    suspend fun getLoansForUser(email: String): List<LoanDocument>
    suspend fun getAllLoans(): List<LoanDocument>
    suspend fun updateLoanStatus(loanId: String, status: String): Result<Unit>
}

class FirebaseLoanRepository : LoanRepository {
    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun applyForLoan(loan: LoanDocument): Result<String> {
        return try {
            val docRef = firestore.collection("loans").document()
            val loanWithId = loan.copy(loanId = docRef.id)
            docRef.set(loanWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLoansForUser(email: String): List<LoanDocument> {
        return try {
            val snapshot = firestore.collection("loans").whereEqualTo("borrowerEmail", email).get().await()
            snapshot.documents.mapNotNull { it.toObject(LoanDocument::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getAllLoans(): List<LoanDocument> {
        return try {
            val snapshot = firestore.collection("loans").get().await()
            snapshot.documents.mapNotNull { it.toObject(LoanDocument::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun updateLoanStatus(loanId: String, status: String): Result<Unit> {
        return try {
            firestore.collection("loans").document(loanId).update("status", status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
