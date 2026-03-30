package edu.cit.pangilinan.stillness.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val role: String = "ROLE_USER"
)

// Matches backend AuthResponse
data class AuthData(
    val user: User,
    val token: String,
    val refreshToken: String
)

// Matches backend ApiResponse structure
data class LoginResponse(
    val success: Boolean,
    val data: AuthData?,
    val error: ErrorDetail?,
    val timestamp: String
)

data class ErrorDetail(
    val code: String?,
    val message: String?,
    val details: Any?
)

data class RegisterResponse(
    val success: Boolean,
    val data: AuthData?,
    val error: ErrorDetail?,
    val timestamp: String
)
