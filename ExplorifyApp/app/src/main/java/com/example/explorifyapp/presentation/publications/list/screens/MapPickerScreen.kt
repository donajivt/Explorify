package com.example.explorifyapp.presentation.publications.list.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.view.MotionEvent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
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

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val permission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    var selectedPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var selectedPlaceName by remember { mutableStateOf<String>("") }

    // Configuración inicial del mapa
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(6.0)
        mapView.controller.setCenter(GeoPoint(19.43, -99.13)) // CDMX inicial
        permission.launchPermissionRequest()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seleccionar ubicación") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 🗺️ Mapa principal
            AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize()) { map ->
                map.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        val projection = map.projection
                        val point =
                            projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
                        selectedPoint = point
                        updateMarker(map, context, point)

                        CoroutineScope(Dispatchers.IO).launch {
                            val placeName =
                                getPlaceNameFromOSM(point.latitude, point.longitude)
                            withContext(Dispatchers.Main) {
                                selectedPlaceName = placeName ?: "Ubicación seleccionada"
                            }
                        }
                    }
                    false
                }
            }

            // 🔍 Campo de búsqueda flotante
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(10.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(8.dp)
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Buscar lugar...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
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
                                            mapView.controller.setZoom(15.0)
                                            mapView.controller.setCenter(point)
                                        }
                                    }
                                }
                            }
                        }) { Text("Buscar") }
                    }
                )
            }

            // 📍 Botón Mi ubicación (FAB clásico)
            FloatingActionButton(
                onClick = {
                    if (permission.status.isGranted) {
                        val fused = LocationServices.getFusedLocationProviderClient(context)
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            fused.lastLocation.addOnSuccessListener { loc ->
                                loc?.let {
                                    val point = GeoPoint(it.latitude, it.longitude)
                                    selectedPoint = point
                                    updateMarker(mapView, context, point)
                                    mapView.controller.setZoom(15.0)
                                    mapView.controller.setCenter(point)
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val placeName = getPlaceNameFromOSM(
                                            it.latitude,
                                            it.longitude
                                        )
                                        withContext(Dispatchers.Main) {
                                            selectedPlaceName =
                                                placeName ?: "Mi ubicación actual"
                                        }
                                    }
                                }
                            }
                        }
                    } else permission.launchPermissionRequest()
                },
                containerColor = Color(0xFF355E3B),
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            ) {
                Text("📍")
            }

            // ✅ Botón Confirmar
            Button(
                onClick = {
                    selectedPoint?.let {
                        navController.previousBackStackEntry?.savedStateHandle?.apply {
                            set("selected_location_name", selectedPlaceName)
                            set("selected_latitude", it.latitude.toString())
                            set("selected_longitude", it.longitude.toString())
                        }
                        navController.popBackStack()
                    }
                },
                enabled = selectedPoint != null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 90.dp)
                    .fillMaxWidth(0.9f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF355E3B))
            ) {
                Text("Confirmar ubicación", color = Color.White)
            }

            // 📝 Lugar seleccionado
            if (selectedPlaceName.isNotEmpty()) {
                Text(
                    text = "📍 $selectedPlaceName",
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    color = Color.Black
                )
            }
        }
    }

    BackHandler { navController.popBackStack() }
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

// 🌍 Reverse geocoding
suspend fun getPlaceNameFromOSM(lat: Double, lon: Double): String? = withContext(Dispatchers.IO) {
    try {
        val url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lon"
        val response = URL(url).readText()
        JSONObject(response).getString("display_name")
    } catch (e: Exception) {
        null
    }
}

// 🌍 Forward geocoding
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
