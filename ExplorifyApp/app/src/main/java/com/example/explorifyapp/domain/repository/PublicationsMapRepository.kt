package com.example.explorifyapp.domain.repository

import com.example.explorifyapp.data.remote.dto.PublicationsMapResponse
import com.example.explorifyapp.data.remote.publications.PublicationsApiService
import com.example.explorifyapp.data.remote.publications.RetrofitPublicationsInstance

class PublicationsMapRepository(private val api: PublicationsApiService) { //userId: String,
    suspend fun getMapsPublications( token: String): PublicationsMapResponse {
        return api.getAllMaps("Bearer $token")
    }
}