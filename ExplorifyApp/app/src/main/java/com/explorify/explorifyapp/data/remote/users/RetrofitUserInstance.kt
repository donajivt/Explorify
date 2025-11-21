package com.explorify.explorifyapp.data.remote.users

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitUserInstance {
    private const val BASE_URL = "https://explorify-users.runasp.net/" // URl de la Apihttps://explorify-users.somee.com/
    val api: UsersApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UsersApiService::class.java)
    }
}