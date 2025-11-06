package com.explorify.explorifyapp.data.remote.auth

import com.explorify.explorifyapp.data.remote.dto.LoginRequest
import com.explorify.explorifyapp.data.remote.dto.LoginResponse
import com.explorify.explorifyapp.data.remote.dto.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/AuthApi/login") // Ruta de tu API
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/AuthApi/register")
    suspend fun register(@Body request: RegisterRequest): LoginResponse

}
