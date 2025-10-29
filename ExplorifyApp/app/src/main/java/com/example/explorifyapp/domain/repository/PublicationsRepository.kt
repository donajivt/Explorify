package com.example.explorifyapp.domain.repository

import com.example.explorifyapp.data.remote.model.Publication


/*class PublicationsRepository {
    suspend fun getUserPublications(userId: String, token: String): PublicationsResponse {
        return RetrofitPublicationsInstance.api.getPublicationsByUser(userId,"Bearer $token")
    }
}*/
interface PublicationRepository {
    suspend fun getAll(token: String): List<Publication>
    suspend fun getById(id: String, token: String): Publication

    suspend fun getUserPublications(userId: String, token: String): List<Publication>
    suspend fun create(
        imageUrl: String,
        title: String,
        description: String,
        location: String,
        latitud: String?,
        longitud: String?,
        userId: String,
        token: String
    ): Publication

    suspend fun update(
        id: String,
        imageUrl: String,
        title: String,
        description: String,
        location: String,
        latitud: String?,
        longitud: String?,
        userId: String,
        token: String
    ): Publication

    suspend fun delete(id: String, token: String)
}
