package com.explorify.explorifyapp.domain.repository

import com.explorify.explorifyapp.data.remote.dto.MediaResult
import com.explorify.explorifyapp.data.remote.publications.MediaApi
import okhttp3.MultipartBody

class MediaRepositoryImpl(private val api: MediaApi) {

    suspend fun uploadImage(token: String,file: MultipartBody.Part): MediaResult? {
        return try {
            val response = api.uploadImage("Bearer $token",file)
            println("🌍 Código HTTP: ${response.code()}")
            println("🌍 Body: ${response.body()}")
            println("🌍 ErrorBody: ${response.errorBody()?.string()}")
            if (response.isSuccessful && response.body()?.isSuccess == true)
                response.body()?.result
            else null
        } catch (e: Exception) {
            null
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