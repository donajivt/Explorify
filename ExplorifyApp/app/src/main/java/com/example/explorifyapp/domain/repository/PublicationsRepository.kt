package com.example.explorifyapp.domain.repository

import com.example.explorifyapp.data.remote.dto.PublicationsResponse
import com.example.explorifyapp.data.remote.publications.PublicationsApiService
import com.example.explorifyapp.data.remote.publications.RetrofitPublicationsInstance

/*class PublicationsRepository {
    suspend fun getUserPublications(userId: String, token: String): PublicationsResponse {
        return RetrofitPublicationsInstance.api.getPublicationsByUser(userId,"Bearer $token")
    }
}*/
class PublicationsRepository(private val api: PublicationsApiService) {
    suspend fun getUserPublications(userId: String, token: String): PublicationsResponse {
        return api.getPublicationsByUser(userId, "Bearer $token")
    }
}
