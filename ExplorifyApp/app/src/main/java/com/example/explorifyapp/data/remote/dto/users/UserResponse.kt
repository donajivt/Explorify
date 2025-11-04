package com.example.explorifyapp.data.remote.dto.users

data class User(
    val id: String,
    val email: String,
    val name: String,
    val passwordHash: String,
    val roles: List<String>,
    val createdAt: String,
    val updatedAt: String
)

data class UsersResponse(
    val result: List<User>,
    val isSuccess: Boolean,
    val message: String
)

data class UserResponse(
    val result: User,
    val isSuccess: Boolean,
    val message: String
)

data class SimpleResponse(
    val result: String,
    val isSuccess: Boolean,
    val message: String
)

