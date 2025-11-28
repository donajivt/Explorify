package com.explorify.explorifyapp.messaging

import android.util.Log
import com.explorify.explorifyapp.data.remote.room.AppDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.e("FCM_TOKEN", "🔥 Nuevo token FCM generado = $token")

        // No guardar en Room, no enviar al backend.
        // Se enviará cuando el usuario inicie sesión (LoginViewModel).
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.e("FCM_PUSH", "🔥 LLEGÓ UNA NOTIFICACIÓN")
        Log.e("FCM_PUSH", "Raw payload completo: ${message.data}")

        val title = message.data["title"] ?: "Explorify"
        val body = (message.data["body"] ?: message.data["message"])
            ?.takeIf { it.isNotBlank() }
            ?: "Tienes una nueva notificación"
        val publicacionId = message.data["publicacionId"]
        Log.e("FCM_PUSH", "PUBLICACION_ID RECIBIDO = $publicacionId")
        NotificationHelper.showNotification(
            context = this,
            title = title,
            message = body,
            publicacionId = publicacionId
        )
    }
}
