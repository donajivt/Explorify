package com.explorify.explorifyapp.messaging

import com.explorify.explorifyapp.data.remote.model.NotificationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface NotificationApi {
    @POST("api/Notification/send")
    suspend fun sendNotification(@Body body: Map<String, String>): Response<NotificationResponse>
}