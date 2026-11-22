package com.example.cashflow.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class LoanSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val loanAmount = inputData.getDouble("loan_amount", 0.0)
        val tenure = inputData.getInt("tenure", 12)
        val income = inputData.getDouble("income", 0.0)
        val nationalId = inputData.getString("national_id") ?: ""
        val dob = inputData.getString("dob") ?: ""

        val user = FirebaseAuth.getInstance().currentUser ?: return Result.failure()
        val firestore = FirebaseFirestore.getInstance()

        try {
            val loanData = hashMapOf(
                "borrowerId" to user.uid,
                "borrowerEmail" to (user.email ?: ""),
                "amount" to loanAmount,
                "tenure" to tenure,
                "income" to income,
                "nationalId" to nationalId,
                "dob" to dob,
                "status" to "Pending",
                "createdAt" to Timestamp.now()
            )

            firestore.collection("loans").add(loanData).await()
            return Result.success()
        } catch (e: Exception) {
            return if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
