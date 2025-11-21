package com.explorify.explorifyapp.data.remote.publications

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitPublicationsInstance {
    private const val BASE_URL = "https://explorify-publications.runasp.net/"//"http://explorify.somee.com/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Cliente HTTP con tiempos configurados
    private val okHttpClient = OkHttpClient.Builder ()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    /*
    val api: PublicationsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PublicationsApiService::class.java)
    }*/

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 🔹 API para publicaciones
    val api: PublicationsApiService by lazy {
        retrofit.create(PublicationsApiService::class.java)
    }

    // 🔹 API para reportes
    val reportApi: ReportApi by lazy {
        retrofit.create(ReportApi::class.java)
    }

}
