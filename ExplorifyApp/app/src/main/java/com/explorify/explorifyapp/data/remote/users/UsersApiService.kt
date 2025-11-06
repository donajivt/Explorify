package com.explorify.explorifyapp.data.remote.users

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

interface UsersApiService {

    // 🔹 Obtener todos los usuarios
    @GET("api/Users")
    suspend fun getAll(
        @Header("Authorization") token: String
    ): UsersResponse

    // 🔹 Obtener el usuario actual (por token)
    @GET("api/Users/me")
    suspend fun getUser(
        @Header("Authorization") token: String
    ): UserResponse

    // 🔹 Editar usuario actual
    @PUT("api/Users/me")
    suspend fun editUser(
        @Body body: UserRequest,
        @Header("Authorization") token: String
    ): Response<SimpleResponse>

    // 🔹 Eliminar usuario actual
    @DELETE("api/Users/me")
    suspend fun deleteUser(
        @Header("Authorization") token: String
    ): Response<Unit>
}
