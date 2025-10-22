package com.example.explorifyapp.data.remote.publications

import com.example.explorifyapp.data.remote.dto.PublicationsResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Header
import retrofit2.http.Path

interface PublicationsApiService {
    @GET("/api/Publication/user/{userId}")
    suspend fun getPublicationsByUser(@Path("userId") userId: String, @Header("Authorization") token: String): PublicationsResponse
}
