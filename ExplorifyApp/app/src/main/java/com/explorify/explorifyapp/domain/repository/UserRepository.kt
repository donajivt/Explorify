package com.explorify.explorifyapp.domain.repository

import com.explorify.explorifyapp.data.remote.users.UsersApiService
import com.explorify.explorifyapp.data.remote.dto.users.UsersResponse
//import com.google.android.gms.common.api.Response
import retrofit2.Response
import com.explorify.explorifyapp.data.remote.dto.users.UserRequest
import com.explorify.explorifyapp.data.remote.dto.users.UserResponse
import com.explorify.explorifyapp.data.remote.dto.users.SimpleResponse
import java.util.StringJoiner
import java.io.File
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import com.explorify.explorifyapp.data.remote.dto.users.Emails

class UserRepository(private val api: UsersApiService) {

    // 🔹 Obtener todos los usuarios
    suspend fun getUsers(token: String): Response<UsersResponse> {
        return api.getAll("Bearer $token")
    }

    // 🔹 Obtener el usuario actual
    suspend fun getCurrentUser(token: String): UserResponse {
        return api.getUser("Bearer $token")
    }
/*
    //Obtener por id
    suspend fun getById(token: String,id:String): UserResponse{
        return api.getUserById(id,"Bearer $token")
    }*/
      //Traer por id
    suspend fun getUserById(token: String, userId: String): Response<UserResponse> {
        return api.getById("Bearer $token", userId)
    }
    // 🔹 Editar el usuario actual
    suspend fun editCurrentUser(
        token: String,
        username: String,
        email: String,
        imageFile: File?
    ): Response<SimpleResponse> {

        val usernameBody = username.toRequestBody("text/plain".toMediaType())
        val emailBody = email.toRequestBody("text/plain".toMediaType())

        val imagePart: MultipartBody.Part? = imageFile?.let { file ->
            //val requestFile = file.asRequestBody("image/*".toMediaType())
            val mimeType = when (file.extension.lowercase()) {
                "png" -> "image/png"
                "jpg", "jpeg" -> "image/jpeg"
                else -> "application/octet-stream"
            }
            val requestFile = file.asRequestBody(mimeType.toMediaType())

            MultipartBody.Part.createFormData("profileImage", file.name, requestFile)
        }
        //anterior
         val image = imageFile?.let {
            val reqFile = it.asRequestBody("image/*".toMediaType())
            MultipartBody.Part.createFormData("profileImage", it.name, reqFile)
        }

        return api.editUser(
            username = usernameBody,
            email = emailBody,
            profileImage = imagePart,
            token = "Bearer $token"
        )
    }

    /*suspend fun editCurrentUser(token: String, userRequest: UserRequest): Response<SimpleResponse> {
        return api.editUser(userRequest, "Bearer $token")
    }*/


    //Eliminar por id
    suspend fun deleteUserById(token: String, userId: String): Response<SimpleResponse> {
        return api.deleteUserById("Bearer $token", userId)
    }

    // 🔹 Eliminar el usuario actual
    suspend fun deleteCurrentUser(token: String): Response<Unit> {
        return api.deleteUser("Bearer $token")
    }
    //Cambiar contraseña
    suspend fun changePassword(token: String, emails:Emails):Response<SimpleResponse>{
        return api.changePassword("Bearer ${token}", emails)
    }

}