package com.example.explorifyapp.domain.repository

import com.example.explorifyapp.data.remote.dto.LoginRequest
import com.example.explorifyapp.data.remote.auth.RetrofitInstance
import com.example.explorifyapp.data.remote.room.AppDatabase
import com.example.explorifyapp.data.remote.room.AuthToken
import android.content.Context
import androidx.compose.ui.semantics.Role

class LoginRepository(context: Context) {

    suspend fun login(username: String, password: String) =
        RetrofitInstance.api.login(LoginRequest(username, password))

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


}