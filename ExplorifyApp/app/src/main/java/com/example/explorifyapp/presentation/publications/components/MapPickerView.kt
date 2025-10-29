package com.example.explorifyapp.presentation.publications.components

import android.Manifest
import android.content.Context
import android.view.MotionEvent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.net.URL

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapPickerView(
    onLocationSelected: (String, String, String) -> Unit
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val permission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    var selectedPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var selectedPlaceName by remember { mutableStateOf<String>("") }

    // Configurar el mapa solo una vez
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(5.5)
        mapView.controller.setCenter(GeoPoint(23.6345, -102.5528)) // México centro
        permission.launchPermissionRequest()
    }

    // 🔹 Obtener ubicación actual automáticamente
    LaunchedEffect(permission.status) {
        if (permission.status.isGranted) {
            try {
                val fused = LocationServices.getFusedLocationProviderClient(context)
                val loc = withContext(Dispatchers.IO) {
                    suspendCancellableCoroutine { cont ->
                        fused.lastLocation.addOnSuccessListener { location ->
                            cont.resume(location) {}
                        }
                    }
                }
                loc?.let {
                    val point = GeoPoint(it.latitude, it.longitude)
                    selectedPoint = point
                    mapView.controller.setCenter(point)
                    mapView.controller.setZoom(15.0)
                    updateMarker(mapView, context, point)
                    val placeName = getPlaceNameFromOSM(it.latitude, it.longitude)
                    selectedPlaceName = placeName ?: "Mi ubicación actual"
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        } else {
            permission.launchPermissionRequest()
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 🔍 Buscar ciudad o lugar
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Buscar ciudad o lugar...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                TextButton(onClick = {
                    if (searchText.text.isNotBlank()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val result = fetchCoordinatesFromOSM(searchText.text)
                            result?.let { (lat, lon, name) ->
                                withContext(Dispatchers.Main) {
                                    val point = GeoPoint(lat, lon)
                                    selectedPoint = point
                                    selectedPlaceName = name
                                    updateMarker(mapView, context, point)
                                    mapView.controller.setCenter(point)
                                    mapView.controller.setZoom(15.0)
                                }
                            }
                        }
                    }
                }) {
                    Text("Buscar")
                }
            }
        )

        Spacer(Modifier.height(10.dp))

        // 🗺️ Mapa con altura controlada
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp) // tamaño fijo para no tapar nada
        ) {
            AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize()) { map ->
                map.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        val projection = map.projection
                        val point =
                            projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
                        selectedPoint = point
                        updateMarker(map, context, point)

                        CoroutineScope(Dispatchers.IO).launch {
                            val placeName = getPlaceNameFromOSM(point.latitude, point.longitude)
                            withContext(Dispatchers.Main) {
                                selectedPlaceName = placeName ?: "Ubicación seleccionada"
                            }
                        }
                    }
                    false
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // 🧾 Mostrar la ubicación actual o seleccionada
        if (selectedPlaceName.isNotEmpty()) {
            Text(
                text = "📍 $selectedPlaceName",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp)
            )
        }

        Spacer(Modifier.height(10.dp))

        // ✅ Confirmar selección
        Button(
            onClick = {
                selectedPoint?.let {
                    onLocationSelected(
                        selectedPlaceName,
                        it.latitude.toString(),
                        it.longitude.toString()
                    )
                }
            },
            enabled = selectedPoint != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirmar ubicación")
        }
    }
}

// 🚩 Actualizar marcador en el mapa
private fun updateMarker(map: MapView, context: Context, point: GeoPoint) {
    val marker = Marker(map).apply {
        position = point
        title = "Ubicación seleccionada"
        icon = ContextCompat.getDrawable(context, android.R.drawable.star_big_on)
    }
    map.overlays.clear()
    map.overlays.add(marker)
    map.invalidate()
}

// 🌍 Obtener nombre de un punto (reverse geocoding)
suspend fun getPlaceNameFromOSM(lat: Double, lon: Double): String? = withContext(Dispatchers.IO) {
    try {
        val url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lon"
        val response = URL(url).readText()
        JSONObject(response).getString("display_name")
    } catch (e: Exception) {
        null
    }
}

// 🌍 Buscar por texto (forward geocoding)
suspend fun fetchCoordinatesFromOSM(query: String): Triple<Double, Double, String>? =
    withContext(Dispatchers.IO) {
        try {
            val url = "https://nominatim.openstreetmap.org/search?format=json&q=${query}"
            val response = URL(url).readText()
            val array = JSONArray(response)
            if (array.length() > 0) {
                val obj = array.getJSONObject(0)
                Triple(
                    obj.getDouble("lat"),
                    obj.getDouble("lon"),
                    obj.getString("display_name")
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
