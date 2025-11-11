package com.explorify.explorifyapp.domain.repository

import com.explorify.explorifyapp.data.remote.dto.MediaResult
import com.explorify.explorifyapp.data.remote.publications.MediaApi
import okhttp3.MultipartBody

class MediaRepositoryImpl(private val api: MediaApi) {

    suspend fun uploadImage(token: String, file: MultipartBody.Part): MediaResult? {
        return try {
            val response = api.uploadImage("Bearer $token", file)

            println("🌍 Código HTTP: ${response.code()}")

            if (response.isSuccessful && response.body()?.isSuccess == true) {
                println("🌍 Imagen subida con éxito: ${response.body()}")
                response.body()?.result
            } else {
                // 🔎 Leer el cuerpo de error (si existe)
                val errorBody = response.errorBody()?.string()
                println("🌍 ErrorBody: $errorBody")

                // Extraer el mensaje desde el JSON
                val message = try {
                    val json = org.json.JSONObject(errorBody ?: "{}")
                    json.optString("message", "Error al subir la imagen")
                } catch (_: Exception) {
                    errorBody ?: "Error al subir la imagen"
                }

                // 🚫 Lanzar una excepción con el mensaje del servidor
                throw Exception(message)
            }

        } catch (e: Exception) {
            println("🌍 Excepción al subir imagen: ${e.message}")
            throw e  // <-- Propagamos el error para que el Composable lo capture
        }
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