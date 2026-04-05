package edu.cit.pangilinan.stillness.model

data class SessionDto(
    val id: String,
    val title: String,
    val description: String,
    val instructorId: String,
    val instructorName: String,
    val category: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val capacity: Int,
    val enrolledCount: Int,
    val status: String,
    val price: Double,
    val imageUrl: String?,
    val tags: List<String>?,
    val requirements: String?,
    val location: String?
)

data class SessionResponse(
    val success: Boolean,
    val data: List<SessionDto>?,
    val error: ErrorDetail?
)
