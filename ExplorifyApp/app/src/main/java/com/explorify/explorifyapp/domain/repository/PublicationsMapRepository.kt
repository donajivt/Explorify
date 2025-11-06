package com.explorify.explorifyapp.domain.repository

import com.explorify.explorifyapp.data.remote.dto.PublicationsMapResponse
import com.explorify.explorifyapp.data.remote.publications.PublicationsApiService

class PublicationsMapRepository(private val api: PublicationsApiService) { //userId: String,
    suspend fun getMapsPublications( token: String): PublicationsMapResponse {
        return api.getAllMaps("Bearer $token")
    }
}