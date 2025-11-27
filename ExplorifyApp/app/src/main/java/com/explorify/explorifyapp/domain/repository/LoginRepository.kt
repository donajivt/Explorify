package com.explorify.explorifyapp.domain.repository

import com.explorify.explorifyapp.data.remote.dto.LoginRequest
import com.explorify.explorifyapp.data.remote.auth.RetrofitInstance
import com.explorify.explorifyapp.data.remote.room.AppDatabase
import com.explorify.explorifyapp.data.remote.room.AuthToken
import android.content.Context
import com.explorify.explorifyapp.data.remote.dto.EmailResponse
import com.explorify.explorifyapp.data.remote.dto.users.SimpleResponse
import com.explorify.explorifyapp.data.remote.users.RetrofitUserInstance

class LoginRepository(context: Context) {

    private val usersApi = RetrofitUserInstance.api   // ✅ Faltaba esto

    suspend fun login(username: String, password: String) =
        RetrofitInstance.api.login(LoginRequest(username, password))

    suspend fun getUsers(): EmailResponse {
       return RetrofitInstance.api.getUsers()
    }
    //ROOM
    private val tokenDao = AppDatabase.getInstance(context).authTokenDao()

    suspend fun saveToken(token: String, username: String,userId: String,userEmail:String,role: String) {
        tokenDao.saveToken(AuthToken(token = token,username = username,userId = userId,userEmail = userEmail, role = role))
    }

    suspend fun getAuthData(): AuthToken? {
        return tokenDao.getToken()
    }

    suspend fun clearToken() {
        tokenDao.clearToken()
    }

    suspend fun editToken(username: String,email:String){
        tokenDao.updateUserInfo(username = username, email = email)
    }

    suspend fun updateDeviceToken(jwtToken: String, body: Map<String, String>): SimpleResponse {
        val response = usersApi.updateDeviceToken(
            token = "Bearer $jwtToken",
            body = body
        )

        return if (response.isSuccessful && response.body() != null) {
            response.body()!!
        } else {
            SimpleResponse(
                result = "",
                isSuccess = false,
                message = response.errorBody()?.string() ?: "Error al actualizar deviceToken"
            )
        }
    }
}