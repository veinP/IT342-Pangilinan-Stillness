package edu.cit.pangilinan.stillness.model

data class BookingDto(
    val id: String,
    val bookingNumber: String,
    val session: SessionDto,
    val status: String,
    val paymentStatus: String,
    val amount: Double,
    val bookedAt: String,
    val cancellableUntil: String?
)

data class BookingRequest(
    val sessionId: String,
    val attendeeNotes: String? = null
)

data class SingleBookingResponse(
    val success: Boolean,
    val data: BookingDto?,
    val error: ErrorDetail?
)

data class ListBookingResponse(
    val success: Boolean,
    val data: List<BookingDto>?,
    val error: ErrorDetail?
)
