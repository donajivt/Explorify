package com.explorify.explorifyapp.messaging

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.explorify.explorifyapp.MainActivity
import com.explorify.explorifyapp.R

object NotificationHelper {

    private const val CHANNEL_ID = "explorify_channel"

    fun showNotification(
        context: Context,
        title: String,
        message: String,
        publicacionId: String?
    ) {

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("publicacionId", publicacionId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

            android.util.Log.d("NOTIF_HELPER", "postId enviado por notificación: $publicacionId")
        }

        // 🔥 requestCode único por notificación
        val requestCode = System.currentTimeMillis().toInt()

        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Solo si tenemos permiso
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {

            // 🔥 notificationId único para que no se sobrescriban
            val notificationId = System.currentTimeMillis().toInt()

            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }
}
