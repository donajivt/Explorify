package com.explorify.explorifyapp

import android.app.Activity
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.explorify.explorifyapp.ui.theme.ExplorifyAppTheme
import com.explorify.explorifyapp.navigation.AppNavigation
import com.google.firebase.messaging.FirebaseMessaging

@Composable
fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

        val context = LocalContext.current
        val permissionGranted = ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            LaunchedEffect(Unit) {

                val activity = context as? Activity   // 👈 CORRECCIÓN

                activity?.let {
                    ActivityCompat.requestPermissions(
                        it,
                        arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                        1001
                    )
                }
            }
        }
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()

        // 🔥 PRUEBA: obtener token FCM del dispositivo
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.e("FCM_DEBUG_MAIN", "TOKEN DESDE MAIN: $token")
            } else {
                Log.e("FCM_DEBUG_MAIN", "ERROR OBTENIENDO TOKEN: ${task.exception}")
            }
        }

        val publicacionIdFromNotification = intent?.getStringExtra("publicacionId")

        setContent {
            ExplorifyAppTheme {
                AppNavigation(startPostId = publicacionIdFromNotification)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val publicacionIdFromNotification = intent.getStringExtra("publicacionId")

        if (!publicacionIdFromNotification.isNullOrEmpty()) {
            setContent {
                ExplorifyAppTheme {
                    AppNavigation(startPostId = publicacionIdFromNotification)
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "explorify_channel",
                "Explorify Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ExplorifyAppTheme {
        Greeting("Android")
    }
}