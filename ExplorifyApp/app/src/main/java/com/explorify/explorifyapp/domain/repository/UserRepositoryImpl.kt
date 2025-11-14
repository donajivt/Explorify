package com.explorify.explorifyapp.domain.repository

import com.explorify.explorifyapp.data.remote.model.User
import com.explorify.explorifyapp.data.remote.publications.UsersApi

class UserRepositoryImpl(private val api: UsersApi) {
    suspend fun getAllUsers(token: String): List<User> {
        return try {
            val response = api.getAllUsers("Bearer $token")
            response.result ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    suspend fun getUserById(id: String, token: String): User {
        return api.getUserById(id, "Bearer $token")
    }
}
