package com.explorify.explorifyapp.domain.repository

import com.explorify.explorifyapp.data.remote.dto.LoginRequest
import com.explorify.explorifyapp.data.remote.auth.RetrofitInstance
import com.explorify.explorifyapp.data.remote.room.AppDatabase
import com.explorify.explorifyapp.data.remote.room.AuthToken
import android.content.Context

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

    suspend fun editToken(username: String,email:String){
        tokenDao.updateUserInfo(username = username, email = email)
    }
}