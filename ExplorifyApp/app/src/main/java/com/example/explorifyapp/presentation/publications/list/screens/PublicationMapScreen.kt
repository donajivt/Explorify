package com.example.explorifyapp.presentation.publications.list.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PublicationMapScreen(
    latitud: String,
    longitud: String,
    locationName: String
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val permission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }


    // Configuración inicial del mapa
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE)
        )
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(false)
        mapView.controller.setZoom(5.5)
    }

    // Obtener ubicación actual del usuario
    LaunchedEffect(permission.status.isGranted) {
        if (permission.status.isGranted) {
            val fused = LocationServices.getFusedLocationProviderClient(context)

            // ✅ Verificación explícita de permisos
            if (
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fused.lastLocation.addOnSuccessListener { loc ->
                    loc?.let { userLocation = GeoPoint(it.latitude, it.longitude) }
                }
            }
        } else {
            permission.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ubicación de la publicación") }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize()) { map ->
                val pubPoint = GeoPoint(latitud.toDouble(), longitud.toDouble())

                // marcador publicación
                val pubMarker = Marker(map).apply {
                    position = pubPoint
                    title = locationName
                    icon = ContextCompat.getDrawable(context, android.R.drawable.star_big_on)
                }
                map.overlays.add(pubMarker)

                // marcador usuario (opcional)
                userLocation?.let { loc ->
                    val userMarker = Marker(map).apply {
                        position = loc
                        title = "Tu ubicación"
                        icon = ContextCompat.getDrawable(context, android.R.drawable.star_big_off)
                    }
                    map.overlays.add(userMarker)
                }

                // centrar mapa
                map.controller.setZoom(6.0)
                map.controller.setCenter(pubPoint)
                map.invalidate()
            }
        }
    }
}
