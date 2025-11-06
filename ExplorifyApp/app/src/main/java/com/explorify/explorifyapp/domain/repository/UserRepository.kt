package com.explorify.explorifyapp.domain.repository

import com.explorify.explorifyapp.data.remote.users.UsersApiService
import com.explorify.explorifyapp.data.remote.dto.users.UsersResponse
//import com.google.android.gms.common.api.Response
import retrofit2.Response
import com.explorify.explorifyapp.data.remote.dto.users.UserRequest
import com.explorify.explorifyapp.data.remote.dto.users.UserResponse
import com.explorify.explorifyapp.data.remote.dto.users.SimpleResponse


class UserRepository(private val api: UsersApiService) {

    // 🔹 Obtener todos los usuarios
    suspend fun getUsers(token: String): UsersResponse {
        return api.getAll("Bearer $token")
    }

    // 🔹 Obtener el usuario actual
    suspend fun getCurrentUser(token: String): UserResponse {
        return api.getUser("Bearer $token")
    }

    // 🔹 Editar el usuario actual
    suspend fun editCurrentUser(token: String, userRequest: UserRequest): Response<SimpleResponse> {
        return api.editUser(userRequest, "Bearer $token")
    }

    // 🔹 Eliminar el usuario actual
    suspend fun deleteCurrentUser(token: String): Response<Unit> {
        return api.deleteUser("Bearer $token")
    }
}