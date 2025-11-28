package com.explorify.explorifyapp.domain.repository

import com.explorify.explorifyapp.data.remote.dto.publications.EmailData
import com.explorify.explorifyapp.data.remote.dto.publications.EmailRequest
import com.explorify.explorifyapp.data.remote.dto.publications.ResponseVerify
import com.explorify.explorifyapp.data.remote.model.Publication
import com.explorify.explorifyapp.data.remote.model.PublicationResponse
import retrofit2.Response
import com.explorify.explorifyapp.data.remote.dto.publications.SingleEmail

/*class PublicationsRepository {
    suspend fun getUserPublications(userId: String, token: String): PublicationsResponse {
        return RetrofitPublicationsInstance.api.getPublicationsByUser(userId,"Bearer $token")
    }
}*/

interface PublicationRepository {
    suspend fun getAll(token: String): List<Publication>
    suspend fun getById(id: String, token: String): PublicationResponse

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

    // 🔹 Enviar correo
    suspend fun sendEmail(emailData: EmailData): Response<EmailRequest>

    suspend fun verifyEmail(email:SingleEmail): Response<ResponseVerify>

    suspend fun deleteadmin(id: String, token: String)
}
