package com.explorify.explorifyapp.data.remote.publications

import com.explorify.explorifyapp.data.remote.dto.Publication
import com.explorify.explorifyapp.data.remote.dto.PublicationsMapResponse
import com.explorify.explorifyapp.data.remote.dto.PublicationsResponse
import com.explorify.explorifyapp.data.remote.dto.publications.CreatePublicationRequest
import com.explorify.explorifyapp.data.remote.dto.publications.EmailData
import com.explorify.explorifyapp.data.remote.dto.publications.EmailRequest
import com.explorify.explorifyapp.data.remote.dto.publications.ResponseVerify
import com.explorify.explorifyapp.data.remote.dto.publications.SingleEmail
import com.explorify.explorifyapp.data.remote.dto.publications.UpdatePublicationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import com.explorify.explorifyapp.data.remote.dto.publications.ReportResponse
import com.explorify.explorifyapp.data.remote.dto.publications.Report

interface ReportApi {

    // 🔹 Crear publicación
    /*@POST("api/Publication/report")
    suspend fun create(
        @Body body: CreatePublicationRequest,
        @Header("Authorization") token: String
    ): Response<PublicationsResponse>
    */

    // 🔹 Obtener todas los reportes
    @GET("api/Publication/report")
    suspend fun getAll(
        @Header("Authorization") token: String
    ): Response<List<Report>>

    // 🔹 Obtener una publicación por ID
    @GET("api/Publication/report/{id}")
    suspend fun getById(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Response<Report>

    // 🔹 Obtener publicaciones por usuario
    @GET("api/Publication/report/user/{userId}")
    suspend fun getPublicationId(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<List<Report>>

    // 🔹 Obtener publicacionid
    @GET("api/Publication/report/publication/{publicationId}")
    suspend fun getUsersReport(
        @Path("publicationId") location: String,
        @Header("Authorization") token: String
    ): Response<List<Report>>

}