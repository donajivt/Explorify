package com.example.explorifyapp.data.remote.publications

import com.example.explorifyapp.data.remote.dto.Publication
import com.example.explorifyapp.data.remote.dto.PublicationsResponse
import com.example.explorifyapp.data.remote.dto.publications.CreatePublicationRequest
import com.example.explorifyapp.data.remote.dto.publications.UpdatePublicationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import com.example.explorifyapp.data.remote.dto.PublicationsMapResponse

interface PublicationsApiService {

    // 🔹 Obtener todas las publicaciones
    @GET("api/Publication")
    suspend fun getAll(
        @Header("Authorization") token: String
    ): Response<PublicationsResponse>

    @GET("api/Publication")
    suspend fun getAllMaps(
        @Header("Authorization") token: String
    ): PublicationsMapResponse

    // 🔹 Obtener una publicación por ID
    @GET("api/Publication/{id}")
    suspend fun getById(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Response<Publication>

    // 🔹 Crear publicación
    @POST("api/Publication")
    suspend fun create(
        @Body body: CreatePublicationRequest,
        @Header("Authorization") token: String
    ): Response<PublicationsResponse>

    // 🔹 Actualizar publicación
    @PUT("api/Publication/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body body: UpdatePublicationRequest,
        @Header("Authorization") token: String
    ): Response<Publication>

    // 🔹 Eliminar publicación
    @DELETE("api/Publication/{id}")
    suspend fun delete(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Response<Unit>

    // 🔹 Obtener publicaciones por usuario
    @GET("/api/Publication/user/{userId}")
    suspend fun getPublicationsByUser(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<PublicationsResponse>

    // 🔹 Obtener publicaciones por ubicación
    @GET("/api/Publication/location/{location}")
    suspend fun getPublicationsByLocation(
        @Path("location") location: String,
        @Header("Authorization") token: String
    ): Response<List<Publication>>
}
