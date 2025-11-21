package com.explorify.explorifyapp.data.remote.users

import retrofit2.http.Query
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import com.explorify.explorifyapp.data.remote.dto.users.UsersResponse
import com.explorify.explorifyapp.data.remote.dto.users.UserRequest
import com.explorify.explorifyapp.data.remote.dto.users.UserResponse
import com.explorify.explorifyapp.data.remote.dto.users.SimpleResponse
import com.explorify.explorifyapp.data.remote.model.User
import retrofit2.http.Path
import retrofit2.http.Multipart
import retrofit2.http.Part
import okhttp3.RequestBody
import okhttp3.MultipartBody
import com.explorify.explorifyapp.data.remote.dto.users.Emails

interface UsersApiService {

    // 🔹 Obtener todos los usuarios
    @GET("api/Users")
    suspend fun getAll(
        @Header("Authorization") token: String
    ): Response<UsersResponse>
/*
    @GET("api/Users/{id}")
    suspend fun getUserById(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): UserResponse
*/
    //Traer por id
    @GET("api/Users/{id}")
    suspend fun getById(
        @Header("Authorization") token: String,
        @Path("id") userId: String
    ): Response<UserResponse>

    // 🔹 Obtener el usuario actual (por token)
    @GET("api/Users/me")
    suspend fun getUser(
        @Header("Authorization") token: String
    ): UserResponse

    // 🔹 Editar usuario actual
   /* @PUT("api/Users/me")
    suspend fun editUser(
        @Body body: UserRequest,
        @Header("Authorization") token: String
    ): Response<SimpleResponse> */
    @Multipart
    @PUT("api/Users/me")
    suspend fun editUser(
        @Part("Username") username: RequestBody,
        @Part("Email") email: RequestBody,
        @Part profileImage: MultipartBody.Part? =null,
        @Header("Authorization") token: String
    ): Response<SimpleResponse>


    //Eliminar por id
    @DELETE("api/Users")
    suspend fun deleteUserById(
        @Header("Authorization") token: String,
        @Query("userId") userId: String
    ): Response<SimpleResponse>

    // 🔹 Eliminar usuario actual
    @DELETE("api/Users/me")
    suspend fun deleteUser(
        @Header("Authorization") token: String
    ): Response<Unit>

    //Cambiar contraseña
    @PUT("api/Users/password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body passwords: Emails
    ): Response<SimpleResponse>
}
