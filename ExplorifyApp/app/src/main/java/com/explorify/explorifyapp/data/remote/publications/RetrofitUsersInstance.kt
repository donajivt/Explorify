package com.explorify.explorifyapp.data.remote.publications

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitUsersInstance {
    val api: UsersApi by lazy {
        Retrofit.Builder()
            .baseUrl( "http://explorify-users.runasp.net/")//"http://explorify-users.somee.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UsersApi::class.java)
    }
}