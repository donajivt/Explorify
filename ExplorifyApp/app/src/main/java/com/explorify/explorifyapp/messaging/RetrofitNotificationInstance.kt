package com.explorify.explorifyapp.messaging

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitNotificationInstance {
    private const val BASE_URL = "https://explorify-notifications.runasp.net/"

    val api: NotificationApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NotificationApi::class.java)
    }
}