package com.example.explorifyapp.data.remote.users

import com.example.explorifyapp.data.remote.auth.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitUsersInstance {
    private const val BASE_URL = "http://explorify-users.somee.com/" // URl de la Api
    val api: UsersApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UsersApiService::class.java)
    }
}