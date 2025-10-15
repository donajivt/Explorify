package com.example.explorifyapp.domain.repository

import com.example.explorifyapp.data.remote.dto.LoginRequest
import com.example.explorifyapp.data.remote.auth.RetrofitInstance

class LoginRepository {
    suspend fun login(username: String, password: String) =
        RetrofitInstance.api.login(LoginRequest(username, password))
}