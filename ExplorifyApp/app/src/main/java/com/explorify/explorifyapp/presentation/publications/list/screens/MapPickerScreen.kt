package com.explorify.explorifyapp.presentation.publications.list.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.view.MotionEvent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.compose.ui.window.Dialog
import android.provider.Settings
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val permission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    val searchFlow = remember { MutableStateFlow("") }

    var selectedPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var selectedPlaceName by remember { mutableStateOf("") }
    var searching by remember { mutableStateOf(false) }

    // Modal elegante
    var showLocationDialog by remember { mutableStateOf(false) }

    // Control de reintentos automáticos tras conceder permiso / activar GPS
    var pendingLocateAction by remember { mutableStateOf(false) }
    var awaitingGpsEnable by remember { mutableStateOf(false) }
    var mapReady by remember { mutableStateOf(false) }
    var mapLoaded by remember { mutableStateOf(false) }

    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }

    // Función para verificar si la ubicación está habilitada
    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    // Función para obtener ubicación del usuario
    @SuppressLint("MissingPermission")
    fun getUserLocation() {
        if (!permission.status.isGranted) {
            pendingLocateAction = true
            permission.launchPermissionRequest()
            return
        }

        if (!isLocationEnabled()) {
            pendingLocateAction = true
            showLocationDialog = true
            return
        }

        val fused = LocationServices.getFusedLocationProviderClient(context)
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!fine && !coarse) return

        scope.launch(Dispatchers.IO) {
            try {
                // 🔁 Reintento automático 3 veces si falla
                repeat(3) { attempt ->
                    val cts = CancellationTokenSource()
                    val fresh = withTimeoutOrNull(2000L) {
                        fused.getCurrentLocation(
                            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                            cts.token
                        ).await()
                    }
                    if (fresh != null) {
                        val p = GeoPoint(fresh.latitude, fresh.longitude)
                        userLocation = p
                        withContext(Dispatchers.Main) {
                            selectedPoint = p
                            updateMarker(mapView, context, p)
                            mapView.controller.setZoom(16.0)
                            mapView.controller.animateTo(p)
                        }
                        val name = getPlaceNameFromOSM(context, p.latitude, p.longitude)
                        withContext(Dispatchers.Main) {
                            selectedPlaceName = name ?: "Mi ubicación actual"
                        }
                        return@launch // 🔚 exit loop si ya logró
                    } else {
                        delay(500L * (attempt + 1)) // espera antes de reintentar
                    }
                }
            } catch (_: Exception) { }
        }
    }

    // Reaccionar cuando el permiso cambia a concedido y había una acción pendiente
    LaunchedEffect(permission.status.isGranted) {
        if (permission.status.isGranted && pendingLocateAction) {
            getUserLocation()
            pendingLocateAction = false
        }
    }

    // Detectar cuando el usuario regresa desde Configuración y ya activó el GPS
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (awaitingGpsEnable && isLocationEnabled()) {
                    scope.launch {
                        // Esperar un poco a que el GPS recupere señal
                        kotlinx.coroutines.delay((1200..2200).random().toLong())

                        val fused = LocationServices.getFusedLocationProviderClient(context)
                        try {
                            val cts = CancellationTokenSource()
                            val fresh = withTimeoutOrNull(4000L) {
                                fused.getCurrentLocation(
                                    Priority.PRIORITY_HIGH_ACCURACY,
                                    cts.token
                                ).await()
                            }

                            if (fresh != null) {
                                val p = GeoPoint(fresh.latitude, fresh.longitude)
                                userLocation = p
                                selectedPoint = p
                                updateMarker(mapView, context, p)
                                mapView.controller.setZoom(16.0)
                                mapView.controller.animateTo(p)
                                withContext(Dispatchers.IO) {
                                    val name = getPlaceNameFromOSM(context, p.latitude, p.longitude)
                                    withContext(Dispatchers.Main) {
                                        selectedPlaceName = name ?: "Mi ubicación actual"
                                    }
                                }
                            } else {
                                // Si no obtiene nada, reintenta una vez más
                                kotlinx.coroutines.delay(1200)
                                getUserLocation()
                            }
                        } catch (_: Exception) { }

                        awaitingGpsEnable = false
                        pendingLocateAction = false
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }



    // ---------- OSMDroid configuración rápida ----------
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = context.packageName
        Configuration.getInstance().tileDownloadThreads = 4
        Configuration.getInstance().tileFileSystemCacheMaxBytes = 1024L * 1024 * 256
        Configuration.getInstance().tileFileSystemCacheTrimBytes = 1024L * 1024 * 200

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(6.0)

        // Por defecto CDMX
        mapView.controller.setCenter(GeoPoint(19.43, -99.13))

        // Intentar obtener ubicación si hay permisos y GPS activo
        if (permission.status.isGranted && isLocationEnabled()) {
            getUserLocation()
        } else {
            // Si no hay permisos o ubicación, mostrar CDMX
            mapView.controller.setZoom(12.0)
        }
    }

    // ---------- Debounce de búsqueda (300ms) ----------
    LaunchedEffect(Unit) {
        searchFlow.debounce(300L).collectLatest { q ->
            if (q.length < 3) return@collectLatest
            searching = true
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

    // 🌍 Modal elegante para activar ubicación (versión mejorada visualmente)
    if (showLocationDialog) {
        Dialog(onDismissRequest = { showLocationDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAF8)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(28.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Ubicación",
                        tint = Color(0xFF3C9D6D),
                        modifier = Modifier.size(65.dp)
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "Activa tu ubicación",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color(0xFF2E473B)
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Explorify necesita acceso a tu ubicación para mostrarte lugares cercanos y mejorar tu experiencia. Activa los servicios de ubicación para continuar.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF5E4C3A),
                            lineHeight = 20.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(26.dp))
                    Button(
                        onClick = {
                            showLocationDialog = false
                            awaitingGpsEnable = true
                            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C9D6D))
                    ) {
                        Text(
                            "Abrir configuración",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = {
                            showLocationDialog = false
                            pendingLocateAction = false
                            awaitingGpsEnable = false
                        }
                    ) {
                        Text(
                            "Cancelar",
                            color = Color(0xFF3C9D6D),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
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
            // 🗺️ Mapa principal
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
                        scope.launch(Dispatchers.IO) {
                            val place = getPlaceNameFromOSM(context, point.latitude, point.longitude)
                            withContext(Dispatchers.Main) {
                                selectedPlaceName = place ?: "Ubicación seleccionada"
                            }
                        }
                    }
                    false
                }
                if (!mapLoaded) {
                    map.postDelayed({ mapLoaded = true }, 1000)
                }
            }

            if (!mapLoaded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF4F6F4)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF3C9D6D))
                }
            }
            // 🔍 Campo de búsqueda
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
                    onValueChange = { newValue ->
                        val cleanText = sanitizeSearchInput(newValue.text)

                        searchText = newValue.copy(text = cleanText)
                        searchFlow.value = cleanText
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
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            TextButton(onClick = {
                                val q = searchText.text.trim()
                                // Si el campo está vacío, regresar a ubicación del usuario
                                if (q.isEmpty()) {
                                    // Marca que el usuario quiere ir a su ubicación
                                    pendingLocateAction = true
                                    getUserLocation()
                                } else {
                                    searching = true
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

            // 📍 FAB ir a mi ubicación (usa la misma lógica centralizada)
            FloatingActionButton(
                onClick = {
                    pendingLocateAction = true
                    getUserLocation()
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
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 160.dp) // Espacio para no tapar el botón
                        .fillMaxWidth(0.9f)
                        .background(
                            Color.White.copy(alpha = 0.92f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "📍",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = selectedPlaceName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

    BackHandler { navController.popBackStack() }

    DisposableEffect(Unit) {
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }
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
        .addInterceptor { chain ->
            var attempt = 0
            var response = chain.proceed(chain.request())
            while (!response.isSuccessful && attempt < 2) {
                attempt++
                response.close()
                response = chain.proceed(chain.request())
            }
            response
        }
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
            withTimeoutOrNull(4000L) {
                val url = "https://nominatim.openstreetmap.org/search?format=json&q=${query}"
                val req = Request.Builder()
                    .url(url)
                    .header("User-Agent", "${context.packageName}/1.0 (ExplorifyApp)")
                    .header("Accept-Language", "es")
                    .header("Accept-Encoding", "gzip")
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

fun sanitizeSearchInput(input: String): String {
    val forbidden = listOf('<', '>', '/', '\\', '"', '\'', '{', '}', '`', '=')
    var cleaned = input
    forbidden.forEach { c ->
        cleaned = cleaned.replace(c.toString(), "")
    }
    return cleaned.trim()
}
