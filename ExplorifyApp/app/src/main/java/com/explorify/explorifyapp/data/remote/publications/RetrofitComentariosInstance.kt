package com.explorify.explorifyapp.data.remote.publications

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitComentariosInstance {
    val api: ComentariosApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://explorify-comments.runasp.net/") //http://explorify-comments.somee.com/
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ComentariosApiService::class.java)
    }
}

//COMENTARIOS
//Ruta:
//YA QUEDOOOOOOOOOO
//P