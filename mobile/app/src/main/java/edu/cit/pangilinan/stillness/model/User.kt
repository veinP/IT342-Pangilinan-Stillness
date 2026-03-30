package edu.cit.pangilinan.stillness.model

data class User(
    val id: String = "",
    val email: String = "",
    val fullName: String = "",
    val role: String = "",
    val profileImageUrl: String? = null,
    val createdAt: String = ""
)
