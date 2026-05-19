package edu.cit.pangilinan.stillness.model

data class PaymentSummary(
    val totalRevenue: Double,
    val paidTransactions: Int,
    val failedTransactions: Int
)

data class PaymentRecord(
    val id: String,
    val bookingNumber: String,
    val userName: String,
    val sessionTitle: String,
    val amount: Double,
    val cardMasked: String,
    val transactionId: String,
    val date: String,
    val status: String
)

data class AdminPaymentsResponse(
    val summary: PaymentSummary,
    val records: List<PaymentRecord>
)

data class Attendee(
    val fullName: String,
    val email: String,
    val bookingNumber: String,
    val status: String,
    val paid: Boolean
)

data class SessionRequest(
    val title: String,
    val description: String,
    val sessionType: String,
    val startTime: String,
    val endTime: String,
    val capacity: Int,
    val price: Double,
    val location: String
)

data class GenericResponse(
    val success: Boolean,
    val message: String?,
    val error: ErrorDetail?
)
