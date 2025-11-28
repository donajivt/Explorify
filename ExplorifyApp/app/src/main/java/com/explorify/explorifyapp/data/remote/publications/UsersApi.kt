package com.explorify.explorifyapp.data.remote.publications

import UserResponse
import com.explorify.explorifyapp.data.remote.model.User
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface UsersApi {
    @GET("/api/Users")
    suspend fun getAllUsers(
        @Header("Authorization") token: String
    ): UserResponse

    @GET("/api/Users/{id}")
    suspend fun getUserById(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): User
}