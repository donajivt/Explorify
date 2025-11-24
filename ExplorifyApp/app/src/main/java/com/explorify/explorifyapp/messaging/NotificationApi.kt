package com.explorify.explorifyapp.messaging

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface NotificationApi {
    @POST("api/Notification/send")
    suspend fun registerToken(@Body body: Map<String, String>): Response<Unit>
}