package com.explorify.explorifyapp.presentation.publications.list.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
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
    val pubPoint = remember(latitud, longitud) {
        GeoPoint(latitud.toDoubleOrNull() ?: 0.0, longitud.toDoubleOrNull() ?: 0.0)
    }

    // Configuración OSMDroid (como en tu compa)
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = context.packageName
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(6.0)
    }

    // Pedir permiso y obtener lastLocation con listener (sin coroutines)
    @SuppressLint("MissingPermission")
    LaunchedEffect(permission.status.isGranted) {
        if (!permission.status.isGranted) {
            permission.launchPermissionRequest()
            return@LaunchedEffect
        }

        val fused = LocationServices.getFusedLocationProviderClient(context)
        if (!hasLocationPermission(context)) return@LaunchedEffect

        try {
            fused.lastLocation.addOnSuccessListener { loc ->
                loc?.let { userLocation = GeoPoint(it.latitude, it.longitude) }
            }
        } catch (_: SecurityException) {

        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Ubicación de la publicación") }) }
    ) { padding ->
        Box( modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize()
            ) { map ->
                // Limpia overlays para evitar duplicados en recomposición
                map.overlays.clear()

                // Marcador de la publicación
                val pubMarker = Marker(map).apply {
                    position = pubPoint
                    title = locationName
                    icon = ContextCompat.getDrawable(context, android.R.drawable.star_big_on)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                map.overlays.add(pubMarker)

                // Marcador de "Tu ubicación" si ya la tenemos
                userLocation?.let { loc ->
                    val userMarker = Marker(map).apply {
                        position = loc
                        title = "Tu ubicación"
                        icon = ContextCompat.getDrawable(context, android.R.drawable.star_big_off)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    map.overlays.add(userMarker)

                    // 👉 Centrar en TU ubicación (lo que pediste)
                    map.controller.setZoom(15.0)
                    map.controller.setCenter(loc)
                } ?: run {
                    // Fallback: si aún no hay lastLocation, centra en la publicación
                    map.controller.setZoom(15.0)
                    map.controller.setCenter(pubPoint)
                }

                map.invalidate()
            }
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}
