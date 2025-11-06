package com.explorify.explorifyapp.presentation.publications.list.screens

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val permission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val scope = rememberCoroutineScope()

    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    val searchFlow = remember { MutableStateFlow("") }

    var selectedPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var selectedPlaceName by remember { mutableStateOf("") }
    var searching by remember { mutableStateOf(false) }


    // ---------- OSMDroid configuración rápida ----------
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = context.packageName
        // Hilos extra para descargar tiles y cache más grande (mejora fluidez)
        Configuration.getInstance().tileDownloadThreads = 4
        Configuration.getInstance().tileFileSystemCacheMaxBytes = 1024L * 1024 * 128 // 128MB
        Configuration.getInstance().tileFileSystemCacheTrimBytes = 1024L * 1024 * 96  // 96MB

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(6.0)
        mapView.controller.setCenter(GeoPoint(19.43, -99.13)) // fallback CDMX

        // pide permiso la primera vez
        permission.launchPermissionRequest()
    }

    // ---------- Centrar en tu ubicación: paralelo y rápido ----------
    @SuppressLint("MissingPermission")
    LaunchedEffect(permission.status.isGranted) {
        if (!permission.status.isGranted) return@LaunchedEffect
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!fine && !coarse) return@LaunchedEffect

        val fused = LocationServices.getFusedLocationProviderClient(context)

        try {
            fused.lastLocation.addOnSuccessListener { loc ->
                if (loc != null && selectedPoint == null) {
                    val p = GeoPoint(loc.latitude, loc.longitude)
                    selectedPoint = p
                    updateMarker(mapView, context, p)
                    mapView.controller.setZoom(15.0)
                    mapView.controller.setCenter(p)
                    scope.launch(Dispatchers.IO) {
                        val name = getPlaceNameFromOSM(context, loc.latitude, loc.longitude)
                        withContext(Dispatchers.Main) {
                            selectedPlaceName = name ?: "Mi ubicación actual"
                        }
                    }
                }
            }
        } catch (_: SecurityException) { /* no-op */ }

        // 2) getCurrentLocation (fresco) con timeout
        scope.launch(Dispatchers.IO) {
            try {
                val cts = CancellationTokenSource()
                val fresh = withTimeoutOrNull(1500L) {
                    fused.getCurrentLocation(
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token
                    ).await()
                }
                fresh?.let {
                    val p = GeoPoint(it.latitude, it.longitude)
                    withContext(Dispatchers.Main) {
                        selectedPoint = p
                        updateMarker(mapView, context, p)
                        mapView.controller.setZoom(16.0)
                        mapView.controller.animateTo(p)
                    }
                    val name = getPlaceNameFromOSM(context, it.latitude, it.longitude)
                    withContext(Dispatchers.Main) { selectedPlaceName = name ?: "Mi ubicación actual" }
                }
            } catch (_: Exception) { /* ignore */ }
        }
    }

    // ---------- Debounce de búsqueda (300ms) ----------
    LaunchedEffect(Unit) {
        searchFlow.debounce(300L).collectLatest { q ->
            if (q.length < 3) return@collectLatest
            val result = fetchCoordinatesFromOSM(context, q)
            result?.let { (lat, lon, name) ->
                val p = GeoPoint(lat, lon)
                selectedPoint = p
                selectedPlaceName = name
                updateMarker(mapView, context, p)
                mapView.controller.setZoom(16.0)
                mapView.controller.animateTo(p)
            }
        }
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
            // 🗺️ Mapa principal (tap para colocar marcador + reverse geocoding)
            AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize()) { map ->
                map.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        val projection = map.projection
                        val point = projection.fromPixels(
                            event.x.toInt(),
                            event.y.toInt()
                        ) as GeoPoint
                        selectedPoint = point
                        updateMarker(map, context, point)
                        // reverse geocoding en segundo plano
                        scope.launch(Dispatchers.IO) {
                            val place = getPlaceNameFromOSM(context, point.latitude, point.longitude)
                            withContext(Dispatchers.Main) { selectedPlaceName = place ?: "Ubicación seleccionada" }
                        }
                    }
                    false
                }
            }

            // 🔍 Campo de búsqueda con debounce
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
                    onValueChange = {
                        searchText = it
                        searchFlow.value = it.text  // dispara búsqueda por debounce
                    },
                    label = { Text("Buscar lugar...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    trailingIcon = {
                        if (searching) {
                            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                        } else {
                            TextButton(onClick = {
                                val q = searchText.text.trim()
                                if (q.isNotEmpty()) {
                                    searching = true
                                    // ✔️ Usar corutina, no LaunchedEffect
                                    scope.launch {
                                        val result = fetchCoordinatesFromOSM(context, q)
                                        searching = false
                                        result?.let { (lat, lon, name) ->
                                            val p = GeoPoint(lat, lon)
                                            selectedPoint = p
                                            selectedPlaceName = name
                                            updateMarker(mapView, context, p)
                                            mapView.controller.setZoom(16.0)
                                            mapView.controller.animateTo(p)
                                        }
                                    }
                                }
                            }) { Text("Buscar") }
                        }
                    }
                )
            }

            FloatingActionButton(
                onClick = {
                    if (!permission.status.isGranted) {
                        permission.launchPermissionRequest()
                        return@FloatingActionButton
                    }

                    // ✅ Guard explícito para el linter
                    val fine = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    val coarse = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    if (!fine && !coarse) return@FloatingActionButton

                    val fused = LocationServices.getFusedLocationProviderClient(context)
                    try {
                        fused.lastLocation.addOnSuccessListener { loc ->
                            loc?.let {
                                val p = GeoPoint(it.latitude, it.longitude)
                                selectedPoint = p
                                updateMarker(mapView, context, p)
                                mapView.controller.setZoom(16.0)
                                mapView.controller.animateTo(p)
                                scope.launch(Dispatchers.IO) {
                                    val place = getPlaceNameFromOSM(context, it.latitude, it.longitude)
                                    withContext(Dispatchers.Main) {
                                        selectedPlaceName = place ?: "Mi ubicación actual"
                                    }
                                }
                            }
                        }
                    } catch (_: SecurityException) { /* no-op */ }
                },
                containerColor = Color(0xFF355E3B),
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            ) { Text("📍") }

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

// ------- Helper: actualizar marcador sin parpadeos -------
private fun updateMarker(map: MapView, context: Context, point: GeoPoint) {
    map.overlays.clear()
    val marker = Marker(map).apply {
        position = point
        title = "Ubicación seleccionada"
        icon = ContextCompat.getDrawable(context, android.R.drawable.star_big_on)
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
    }
    map.overlays.add(marker)
    map.invalidate()
}

// ------- Cliente HTTP rápido con timeouts + User-Agent -------
private val httpClient by lazy {
    OkHttpClient.Builder()
        .callTimeout(6, TimeUnit.SECONDS)
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()
}

// 🌍 Reverse geocoding (rápido + cabeceras)
suspend fun getPlaceNameFromOSM(context: Context, lat: Double, lon: Double): String? =
    withContext(Dispatchers.IO) {
        try {
            val url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lon"
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", context.packageName)
                .header("Accept-Language", "es")
                .build()
            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@use null
                val body = resp.body?.string() ?: return@use null
                JSONObject(body).optString("display_name", null)
            }
        } catch (e: Exception) { null }
    }

// 🌍 Forward geocoding con debounce (rápido + cabeceras)
suspend fun fetchCoordinatesFromOSM(context: Context, query: String): Triple<Double, Double, String>? =
    withContext(Dispatchers.IO) {
        try {
            val url = "https://nominatim.openstreetmap.org/search?format=json&q=${query}"
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", context.packageName)
                .header("Accept-Language", "es")
                .build()
            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@use null
                val body = resp.body?.string() ?: return@use null
                val array = JSONArray(body)
                if (array.length() == 0) return@use null
                val obj = array.getJSONObject(0)
                Triple(
                    obj.getDouble("lat"),
                    obj.getDouble("lon"),
                    obj.getString("display_name")
                )
            }
        } catch (e: Exception) { null }
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
