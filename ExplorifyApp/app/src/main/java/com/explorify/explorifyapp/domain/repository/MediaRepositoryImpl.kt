package com.explorify.explorifyapp.domain.repository

import retrofit2.HttpException
import com.explorify.explorifyapp.data.remote.dto.MediaResponse
import com.explorify.explorifyapp.data.remote.publications.MediaApi
import okhttp3.MultipartBody

class MediaRepositoryImpl(private val api: MediaApi) {

    suspend fun uploadImage(token: String, file: MultipartBody.Part): MediaResponse {

        val response = api.uploadImage("Bearer $token", file)

        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        val body = response.body()
            ?: throw Exception("Respuesta vacía del servidor al subir imagen")

        if (!body.isSuccess) {
            throw Exception(body.message.ifBlank { "Error al subir imagen" })
        }

        return body
    }

    suspend fun deleteImage(token: String, publicId: String): Boolean {
        return try {
            val response = api.deleteImage("Bearer $token", publicId)
            response.isSuccessful && response.body()?.isSuccess == true
        } catch (e: Exception) {
            false
        }
    }
}
