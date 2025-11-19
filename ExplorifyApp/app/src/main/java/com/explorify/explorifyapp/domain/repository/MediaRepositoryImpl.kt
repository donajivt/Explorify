package com.explorify.explorifyapp.domain.repository

import android.util.Log
import retrofit2.HttpException
import com.explorify.explorifyapp.data.remote.dto.MediaResponse
import com.explorify.explorifyapp.data.remote.publications.MediaApi
import okhttp3.MultipartBody

class MediaRepositoryImpl(private val api: MediaApi) {


    suspend fun uploadImage(token: String, file: MultipartBody.Part): MediaResponse {
        Log.e("UPLOAD", "========== SUBIENDO IMAGEN ==========")
        Log.e("UPLOAD", "Token: ${token.take(20)}...")
        Log.e("UPLOAD", "File name: ${file.headers}")
        Log.e("UPLOAD", "File body: ${file.body}")

        val response = api.uploadImage("Bearer $token", file)

        Log.e("UPLOAD", "HTTP CODE = ${response.code()}")
        Log.e("UPLOAD", "RAW = ${response.raw()}")

        val errorText = response.errorBody()?.string()
        Log.e("UPLOAD", "ERROR_BODY = $errorText")

        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        val body = response.body()
            ?: throw Exception("Respuesta vacía del servidor al subir imagen")

        if (!body.isSuccess) {
            throw Exception(body.message.ifBlank { "Error al subir imagen" })
        }

        Log.e("UPLOAD", "Imagen subida con éxito: ${body.result?.secureUrl}")
        return body
    }

    suspend fun deleteImage(token: String,publicId: String): Boolean {
        return try {
            val response = api.deleteImage("Bearer $token",publicId)
            response.isSuccessful && response.body()?.isSuccess == true
        } catch (e: Exception) {
            false
        }
    }
}