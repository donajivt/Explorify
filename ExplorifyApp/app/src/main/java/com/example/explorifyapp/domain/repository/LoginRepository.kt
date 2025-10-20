package com.example.explorifyapp.domain.repository

import com.example.explorifyapp.data.remote.dto.LoginRequest
import com.example.explorifyapp.data.remote.auth.RetrofitInstance
import com.example.explorifyapp.data.remote.room.AppDatabase
import com.example.explorifyapp.data.remote.room.AuthToken
import android.content.Context

class LoginRepository(context: Context) {
    suspend fun login(username: String, password: String) =
        RetrofitInstance.api.login(LoginRequest(username, password))

    //ROOM
    private val tokenDao = AppDatabase.getInstance(context).authTokenDao()

    suspend fun saveToken(token: String) {
        tokenDao.saveToken(AuthToken(token = token))
    }

    suspend fun getToken(): String? {
        return tokenDao.getToken()?.token
    }

    suspend fun clearToken() {
        tokenDao.clearToken()
    }


}