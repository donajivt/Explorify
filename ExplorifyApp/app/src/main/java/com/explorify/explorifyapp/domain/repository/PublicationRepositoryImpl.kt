package com.explorify.explorifyapp.domain.repository

import com.explorify.explorifyapp.data.remote.dto.publications.CreatePublicationRequest
import com.explorify.explorifyapp.data.remote.dto.publications.UpdatePublicationRequest
import com.explorify.explorifyapp.data.remote.mapper.toDomain
import com.explorify.explorifyapp.data.remote.model.Publication
import com.explorify.explorifyapp.data.remote.publications.PublicationsApiService
import retrofit2.HttpException


class PublicationRepositoryImpl(private val api: PublicationsApiService): PublicationRepository {

   override suspend fun getAll(token: String): List<Publication> {
        val response = api.getAll("Bearer $token")
        if (!response.isSuccessful) {
            if (response.code() == 401) throw UnauthorizedException()
            throw HttpException(response)
        }
       val body = response.body()
       return body?.result?.map { it.toDomain() } ?: emptyList()
    }

   override suspend fun getById(id: String, token: String): Publication {
        val response = api.getById(id, "Bearer $token")
        if (!response.isSuccessful) {
            if (response.code() == 401) throw UnauthorizedException()
            throw HttpException(response)
        }
        return response.body()?.toDomain()
            ?: throw IllegalStateException("Publicación no encontrada")
    }

    override suspend fun create(
        imageUrl: String,
        title: String,
        description: String,
        location: String,
        latitud: String?,
        longitud: String?,
        userId: String,
        token: String
    ): Publication {
        val body = CreatePublicationRequest(imageUrl, title, description, location, latitud, longitud, userId)
        val response = api.create(body, "Bearer $token")
        if (!response.isSuccessful) {
            if (response.code() == 401) throw UnauthorizedException()
            throw HttpException(response)
        }
        val bodyResponse = response.body()
        if (bodyResponse?.isSuccess == true) {
            // Solo regresamos un Publication "ficticio" con datos mínimos
            return Publication(
                id = "",
                imageUrl = imageUrl,
                title = title,
                description = description,
                location = location,
                latitud = latitud,
                longitud = longitud,
                userId = userId,
                createdAt = ""
            )
        } else {
            throw IllegalStateException(bodyResponse?.message ?: "Error al crear la publicación")
        }
    }

   override suspend fun update(
        id: String,
        imageUrl: String,
        title: String,
        description: String,
        location: String,
        latitud: String?,
        longitud: String?,
        userId: String,
        token: String
    ): Publication {
        val body = UpdatePublicationRequest(imageUrl, title, description, location,latitud, longitud, userId)
        val response = api.update(id, body, "Bearer $token")
        if (!response.isSuccessful) {
            if (response.code() == 401) throw UnauthorizedException()
            throw HttpException(response)
        }
       val result = try {
           response.body()?.toDomain()
       } catch (e: Exception) {
           null
       }
       return result ?: Publication(
           id = id.ifEmpty { "unknown" },
           imageUrl = imageUrl,
           title = title,
           description = description,
           location = location,
           latitud= latitud,
           longitud = longitud,
           userId = userId,
           createdAt = ""
       )
    }

    override suspend fun delete(id: String, token: String) {
        val response = api.delete(id, "Bearer $token")
        if (!response.isSuccessful) {
            if (response.code() == 401) throw UnauthorizedException()
            throw HttpException(response)
        }
    }

   override suspend fun getUserPublications(userId: String, token: String): List<Publication> {
        val response = api.getPublicationsByUser(userId, "Bearer $token")

        if (!response.isSuccessful) throw HttpException(response)

        val body = response.body()
        if (body == null || !body.isSuccess) {
            throw Exception(body?.message ?: "Error desconocido")
        }

        return body.result.map { it.toDomain() }
    }
}

class UnauthorizedException : Exception("Sesión expirada. Vuelve a iniciar sesión.")
