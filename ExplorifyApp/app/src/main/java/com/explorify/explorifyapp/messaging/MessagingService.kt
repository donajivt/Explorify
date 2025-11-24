package com.explorify.explorifyapp.messaging

import android.util.Log
import com.explorify.explorifyapp.data.remote.room.AppDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.text.get

class MessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d("FCM_TOKEN", "Nuevo token recibido: $token")

        // Guardamos el token en servidor
        CoroutineScope(Dispatchers.IO).launch {
            sendTokenToBackend(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.data["title"] ?: message.notification?.title ?: "Explorify"
        val body = message.data["body"] ?: message.notification?.body ?: "Tienes una nueva notificación"
        val postId = message.data["postId"]

        Log.d("FCM_DEBUG", "Llega notificación con postId: $postId")

        NotificationHelper.showNotification(
            context = this,
            title = title,
            message = body,
            postId = postId
        )
    }

    private suspend fun sendTokenToBackend(token: String) {
        val dao = AppDatabase.getInstance(applicationContext).authTokenDao()
        val userId = dao.getToken()?.userId ?: return

        val body = mapOf(
            "userId" to userId,
            "title" to "Registro de token",
            "message" to "",
            "deviceToken" to token
        )

        try {
            val response = RetrofitNotificationInstance.api.registerToken(body)
            Log.d("FCM_TOKEN", "Token registrado en backend")
        } catch (e: Exception) {
            Log.e("FCM_TOKEN", "Error enviando token: ${e.message}")
        }
    }
}
