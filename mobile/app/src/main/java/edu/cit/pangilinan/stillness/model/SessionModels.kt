package edu.cit.pangilinan.stillness.model

/**
 * SessionDto — Matches backend's edu.cit.pangilinan.stillness.session.dto.SessionDto
 *
 * Field names must exactly match the JSON keys returned by the API.
 */
data class SessionDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val instructor: InstructorDto? = null,
    val type: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val capacity: Int = 0,
    val bookedCount: Int = 0,
    val price: Double = 0.0,
    val thumbnailUrl: String? = null,
    val location: String? = null,
    val status: String? = null,
    // Legacy fields kept for backward compat with older cached data
    val category: String? = null,
    val instructorName: String? = null,
    val instructorId: String? = null,
    val date: String? = null,
    val enrolledCount: Int = 0,
    val imageUrl: String? = null,
    val tags: List<String>? = null,
    val requirements: String? = null
) {
    /** Resolved type: prefer 'type' from backend, fall back to legacy 'category' */
    fun resolvedType(): String = type ?: category ?: "Wellness"

    /** Resolved instructor name: prefer nested instructor.fullName, fall back to flat instructorName */
    fun resolvedInstructorName(): String = instructor?.fullName ?: instructorName ?: "Instructor"

    /** Resolved booked count: prefer 'bookedCount' from backend, fall back to legacy 'enrolledCount' */
    fun resolvedBookedCount(): Int = if (bookedCount > 0) bookedCount else enrolledCount

    /** Resolved date string: parse from ISO startTime if 'date' not set */
    fun resolvedDate(): String {
        if (!date.isNullOrBlank()) return date
        if (!startTime.isNullOrBlank()) {
            return try {
                startTime.substring(0, 10) // "2026-05-15T10:00:00" -> "2026-05-15"
            } catch (e: Exception) {
                ""
            }
        }
        return ""
    }

    /** Resolved start time display (HH:mm format from ISO) */
    fun resolvedStartTimeDisplay(): String {
        if (!startTime.isNullOrBlank() && startTime.contains("T")) {
            return try {
                val timePart = startTime.substring(11, 16) // "10:00"
                val hour = timePart.substring(0, 2).toInt()
                val min = timePart.substring(3, 5)
                val ampm = if (hour >= 12) "PM" else "AM"
                val h12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                "$h12:$min $ampm"
            } catch (e: Exception) {
                startTime
            }
        }
        return startTime ?: ""
    }

    /** Resolved end time display (HH:mm format from ISO) */
    fun resolvedEndTimeDisplay(): String {
        if (!endTime.isNullOrBlank() && endTime.contains("T")) {
            return try {
                val timePart = endTime.substring(11, 16)
                val hour = timePart.substring(0, 2).toInt()
                val min = timePart.substring(3, 5)
                val ampm = if (hour >= 12) "PM" else "AM"
                val h12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                "$h12:$min $ampm"
            } catch (e: Exception) {
                endTime
            }
        }
        return endTime ?: ""
    }
}

data class InstructorDto(
    val id: String? = null,
    val fullName: String? = null,
    val profileImageUrl: String? = null
)

data class SessionListData(
    val sessions: List<SessionDto>,
    val pagination: PaginationDto
)

data class PaginationDto(
    val page: Int,
    val limit: Int,
    val total: Long,
    val pages: Int
)

data class SessionResponse(
    val success: Boolean,
    val data: SessionListData?,
    val error: ErrorDetail?
)
