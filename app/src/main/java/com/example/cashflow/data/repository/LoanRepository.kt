package com.example.cashflow.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await

data class LoanDocument(
    val loanId: String = "",
    val borrowerId: String = "",
    val borrowerEmail: String = "",
    val amount: Double = 0.0,
    val tenure: Int = 12,
    val income: Double = 0.0,
    val status: String = "PENDING",
    val nationalId: String = "",
    val dob: String = "",
    val createdAt: Timestamp? = null
)

interface LoanRepository {
    suspend fun applyForLoan(amount: Double, tenure: Int, income: Double, nationalId: String, dob: String): Result<String>
    suspend fun getLoansForUser(uid: String): List<LoanDocument>
    suspend fun getAllLoans(): List<LoanDocument>
    suspend fun updateLoanStatus(loanId: String, status: String): Result<Unit>
}

class FirebaseLoanRepository : LoanRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override suspend fun applyForLoan(amount: Double, tenure: Int, income: Double, nationalId: String, dob: String): Result<String> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("User not authenticated"))
            val docRef = firestore.collection("loans").document()
            val loan = LoanDocument(
                loanId = docRef.id,
                borrowerId = user.uid,
                borrowerEmail = user.email ?: "",
                amount = amount,
                tenure = tenure,
                income = income,
                status = "PENDING",
                nationalId = nationalId,
                dob = dob,
                createdAt = Timestamp.now()
            )
            docRef.set(loan).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLoansForUser(uid: String): List<LoanDocument> {
        return try {
            val snapshot = firestore.collection("loans").whereEqualTo("borrowerId", uid).get().await()
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
            firestore.collection("loans").document(loanId).update(
                mapOf(
                    "status" to status,
                    "updatedAt" to Timestamp.now()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
