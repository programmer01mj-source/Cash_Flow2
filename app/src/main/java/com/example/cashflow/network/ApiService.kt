package com.example.cashflow.network

import retrofit2.Response
import retrofit2.http.*

data class AuthRequest(val name: String?, val email: String, val password: String?, val role: String?)
data class AuthResponse(val message: String, val token: String, val user: UserDto)
data class UserDto(val id: String, val name: String, val email: String, val role: String)

data class LoanApplyRequest(val amount: Double, val tenure: Int, val income: Double, val nationalId: String? = null, val dob: String? = null)
data class LoanItem(val id: String, val borrowerName: String, val amount: Double, val tenure: Int, val income: Double, var status: String)

data class PaymentRequest(val amount: Double, val mode: String)
data class PaymentResponse(val message: String, val transaction: TransactionDto)
data class TransactionDto(val id: String, val date: String, val amount: Double, val mode: String, val type: String)
data class TransactionItem(val id: String, val date: String, val amount: Double, val mode: String, val type: String)

data class AdminMetrics(
    val totalCapitalDisbursed: Double,
    val totalPrincipalRepaid: Double,
    val totalInterestEarned: Double,
    val nonPerformingLoans: Int,
    val activeBorrowersCount: Int,
    val scheduledReportStatus: String
)

interface ApiService {
    @POST("api/auth/signup")
    suspend fun signup(@Body request: AuthRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @GET("api/loans")
    suspend fun getLoans(@Header("Authorization") token: String): Response<List<LoanItem>>

    @POST("api/loans/apply")
    suspend fun applyLoan(@Header("Authorization") token: String, @Body request: LoanApplyRequest): Response<Map<String, Any>>

    @PATCH("api/loans/{id}/status")
    suspend fun updateLoanStatus(@Header("Authorization") token: String, @Path("id") id: String, @Body body: Map<String, String>): Response<Map<String, Any>>

    @GET("api/transactions")
    suspend fun getTransactions(@Header("Authorization") token: String): Response<List<TransactionItem>>

    @POST("api/transactions/pay")
    suspend fun makePayment(@Header("Authorization") token: String, @Body request: PaymentRequest): Response<PaymentResponse>

    @GET("api/admin/metrics")
    suspend fun getAdminMetrics(@Header("Authorization") token: String): Response<AdminMetrics>
}
