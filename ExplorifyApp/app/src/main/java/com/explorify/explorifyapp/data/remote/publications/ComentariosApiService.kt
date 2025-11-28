package com.explorify.explorifyapp.data.remote.publications

import com.explorify.explorifyapp.data.remote.model.Comentario
import retrofit2.Response
import retrofit2.http.*

data class CreateComentarioRequest(
    val publicacionId: String,
    val text: String
)

data class GenericResponse<T>(
    val result: T?,
    val isSuccess: Boolean,
    val message: String?
)

data class CommentsCountResponse(
    val publicacionId: String,
    val count: Int
)

interface ComentariosApiService {

    @GET("api/Comentarios")
    suspend fun getAll(
        @Query("publicacionId") publicacionId: String,
        @Header("Authorization") token: String
    ): Response<List<Comentario>>

    @GET("api/Comentarios/count")
    suspend fun getCount(
        @Query("publicacionId") publicacionId: String,
        @Header("Authorization") token: String
    ): Response<CommentsCountResponse>


    @POST("api/Comentarios")
    suspend fun create(
        @Body body: CreateComentarioRequest,
        @Header("Authorization") token: String
    ): Response<GenericResponse<Comentario>>

    @DELETE("api/Comentarios/{id}")
    suspend fun delete(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Response<GenericResponse<Unit>>
}
