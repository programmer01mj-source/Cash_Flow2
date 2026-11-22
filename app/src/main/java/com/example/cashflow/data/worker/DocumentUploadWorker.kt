package com.example.cashflow.data.worker

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class DocumentUploadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val fileUriString = inputData.getString("file_uri") ?: return Result.failure()
        val docType = inputData.getString("doc_type") ?: return Result.failure() // "Gov ID", "Selfie", "Pay Slip"
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.failure()

        val fileUri = Uri.parse(fileUriString)
        val storageRef = FirebaseStorage.getInstance().reference
            .child("users")
            .child(uid)
            .child("documents")
            .child("${docType}_${System.currentTimeMillis()}")

        val firestore = FirebaseFirestore.getInstance()

        try {
            // Update state to UPLOADING in Firestore metadata
            val docMetaRef = firestore.collection("users").document(uid).collection("documents").document(docType)
            docMetaRef.set(
                hashMapOf(
                    "docType" to docType,
                    "status" to "UPLOADING",
                    "updatedAt" to Timestamp.now()
                ),
                SetOptions.merge()
            ).await()

            // Upload file to Firebase Storage
            storageRef.putFile(fileUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            // Update state to UPLOADED / AWAITING VERIFICATION
            docMetaRef.set(
                hashMapOf(
                    "docType" to docType,
                    "fileUrl" to downloadUrl,
                    "status" to "UPLOADED",
                    "verificationStatus" to "Awaiting verification",
                    "updatedAt" to Timestamp.now()
                ),
                SetOptions.merge()
            ).await()

            return Result.success()
        } catch (e: Exception) {
            // Update state to FAILED
            try {
                firestore.collection("users").document(uid).collection("documents").document(docType).set(
                    hashMapOf(
                        "docType" to docType,
                        "status" to "FAILED",
                        "error" to (e.localizedMessage ?: "Unknown error"),
                        "updatedAt" to Timestamp.now()
                    ),
                    SetOptions.merge()
                ).await()
            } catch (_: Exception) {}

            return if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
