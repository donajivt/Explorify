package com.example.explorifyapp.data.remote.publications

import com.example.explorifyapp.data.remote.dto.DeleteResponse
import com.example.explorifyapp.data.remote.dto.MediaResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface MediaApi {
    @Multipart
    @POST("api/Media/upload/image")
    suspend fun uploadImage(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Response<MediaResponse>

    @DELETE("api/Media/{publicId}")
    suspend fun deleteImage(
        @Header("Authorization") token: String,
        @Path("publicId") publicId: String
    ): Response<DeleteResponse>
}