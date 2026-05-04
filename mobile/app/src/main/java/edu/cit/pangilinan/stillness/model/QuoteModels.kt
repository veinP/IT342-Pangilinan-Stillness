package edu.cit.pangilinan.stillness.model

data class QuoteDto(
    val text: String,
    val author: String
)

data class QuoteResponse(
    val success: Boolean,
    val data: QuoteDto?,
    val error: ErrorDetail?
)
