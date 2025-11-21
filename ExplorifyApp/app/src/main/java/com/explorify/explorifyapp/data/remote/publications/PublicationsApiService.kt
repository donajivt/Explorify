package com.explorify.explorifyapp.data.remote.publications

import com.explorify.explorifyapp.data.remote.dto.Publication
import com.explorify.explorifyapp.data.remote.dto.PublicationsResponse
import com.explorify.explorifyapp.data.remote.dto.publications.CreatePublicationRequest
import com.explorify.explorifyapp.data.remote.dto.publications.UpdatePublicationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import com.explorify.explorifyapp.data.remote.dto.PublicationsMapResponse
import com.explorify.explorifyapp.data.remote.dto.publications.EmailData
import com.explorify.explorifyapp.data.remote.dto.publications.EmailRequest
import com.explorify.explorifyapp.data.remote.dto.publications.ResponseVerify
import com.explorify.explorifyapp.data.remote.dto.publications.SingleEmail
import com.explorify.explorifyapp.data.remote.model.PublicationResponse

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
    ): Response<PublicationResponse>

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

    // 🔹 Enviar Correo
    @POST("/api/Email/send")
    suspend fun sendEmail(
        @Body body: EmailData,
    ): Response<EmailRequest>

    //Verificar Correo
    @POST("/api/Email/verify")
    suspend fun verifyEmail(
        @Body body: SingleEmail,
    ): Response<ResponseVerify>

    //Borra publicacion Admin
    @DELETE("api/Publication/admin/{id}")
    suspend fun deleteAdmin(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Response<Unit>
}
