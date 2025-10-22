package com.example.explorifyapp.data.remote.publications

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitPublicationsInstance {
    private const val BASE_URL = "http://explorify.somee.com/"

    val api: PublicationsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PublicationsApiService::class.java)
    }
}
