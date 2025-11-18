package com.explorify.explorifyapp.presentation.buscar

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.explorify.explorifyapp.presentation.login.LoginViewModel
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import android.location.Geocoder
import com.explorify.explorifyapp.data.remote.publications.RetrofitPublicationsInstance
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted
import com.explorify.explorifyapp.domain.repository.PublicationsMapRepository
import com.explorify.explorifyapp.data.remote.dto.PublicationMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextAlign
import com.explorify.explorifyapp.data.remote.publications.RetrofitUsersInstance
import com.explorify.explorifyapp.domain.repository.UserRepositoryImpl
import java.util.Locale
import android.content.Intent
import android.content.IntentSender
import android.provider.Settings
import com.google.android.gms.location.*
import com.google.android.gms.common.api.ResolvableApiException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import android.net.ConnectivityManager
import androidx.compose.ui.window.DialogProperties
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.ui.graphics.graphicsLayer
import java.io.File


/*
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun BuscarScreen(navController: NavController, loginViewModel: LoginViewModel = viewModel()) {
    var userName by remember { mutableStateOf("") }
    val repo = remember { PublicationsMapRepository(RetrofitPublicationsInstance.api) }
    val factory = remember { BuscarViewModelFactory(repo) }
    val viewModel: BuscarViewModel = viewModel(factory = factory)

    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    var selectedPublication by remember { mutableStateOf<PublicationMap?>(null) }
    val userRepo = remember { UserRepositoryImpl(RetrofitUsersInstance.api) }
    var userMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var searchQuery by remember { mutableStateOf("") }

    val settingsClient = remember { LocationServices.getSettingsClient(context) }
    val locationRequest = remember {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
    }
    val locationSettingsRequest = remember {
        LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build()
    }

    var locationEnabled by remember { mutableStateOf<Boolean?>(null) }
    var usarPuntoFijo by remember { mutableStateOf(false) }


// Launcher para resolver el diálogo del sistema (activar ubicación)
    val resolutionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            locationEnabled = true
        } else {
            locationEnabled = false
        }
    }

// Chequear si está activada la ubicación del sistema
    LaunchedEffect(Unit) {
        val task = settingsClient.checkLocationSettings(locationSettingsRequest)
        task.addOnSuccessListener { locationEnabled = true }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(e.resolution).build()
                    resolutionLauncher.launch(intentSenderRequest)
                } catch (_: IntentSender.SendIntentException) {
                    locationEnabled = false
                }
            } else {
                locationEnabled = false
            }
        }
    }

    // 🔐 Validar si hay sesión
    LaunchedEffect(Unit) {
        val isLoggedIn = loginViewModel.isLoggedIn()
        if (!isLoggedIn) {
            navController.navigate("login") {
                popUpTo("buscar") { inclusive = true }
            }
        }

        userName = loginViewModel.userName
        val userId = loginViewModel.userId
        val token = loginViewModel.token

        if (!token.isNullOrEmpty() && userId.isNotEmpty()) {
            viewModel.loadPublications(userId, token)
            try {
                val users = userRepo.getAllUsers(token)
                userMap = users.associate { u -> u.id to u.name }
            } catch (_: Exception) {
            }
        }
    }

    // Configurar el mapa
    LaunchedEffect(mapView) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(14.0)
    }

    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    userLocation = GeoPoint(it.latitude, it.longitude)
                    mapView.controller.setCenter(userLocation)
                }
            }
        } else {
            locationPermissionState.launchPermissionRequest()
        }
    }

    /*
    when (locationEnabled) {
        null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        false -> {
            PrendeUbicacionScreen {
                // Acción cuando el usuario decida abrir ajustes
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            return
        }

        true -> {
            /* seguimos con tu código normal */
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Buscar Aventuras Cercanas") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                            }
                        },
                    )
                },
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                            label = { Text("Inicio") },
                            selected = false,
                            onClick = { navController.navigate("publicaciones") }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                            label = { Text("Buscar") },
                            selected = true,
                            onClick = { /* ya está aquí */ }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                            label = { Text("Perfil") },
                            selected = false,
                            onClick = { navController.navigate("perfil") }
                        )
                    }
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color(0xFFF5F5F5))
                ) {
                    // 🔍 Barra de búsqueda
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar lugar o dirección...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3C9D6D),
                            unfocusedBorderColor = Color.Gray,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            cursorColor = Color(0xFF3C9D6D),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Limpiar")
                                }
                            }
                        }
                    )

                    Button(
                        onClick = {
                            if (searchQuery.isNotEmpty()) {
                                val geocoder = Geocoder(context, Locale.getDefault())
                                try {
                                    val results = geocoder.getFromLocationName(searchQuery, 1)
                                    if (!results.isNullOrEmpty()) {
                                        val loc = results[0]
                                        val point = GeoPoint(loc.latitude, loc.longitude)
                                        mapView.controller.setCenter(point)
                                        mapView.controller.setZoom(16.0)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C9D6D))
                    ) {
                        Text("Buscar", color = Color.White)
                    }

                    // 🗺️ Mapa
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                    ) {
                        AndroidView(
                            factory = { mapView },
                            modifier = Modifier.fillMaxSize()
                        ) { map ->
                            map.overlays.clear()

                            userLocation?.let { location ->
                                val userMarker = Marker(map).apply {
                                    position = location
                                    title = "Tu ubicación"
                                    icon = ContextCompat.getDrawable(context, android.R.drawable.star_big_off)
                                }
                                map.overlays.add(userMarker)
                                map.controller.setCenter(location)
                            }

                            // ✅ Mostrar publicaciones cercanas
                            viewModel.publications.forEach { pub ->
                                val lat = pub.latitud.toDoubleOrNull()
                                val lon = pub.longitud.toDoubleOrNull()
                                if (lat != null && lon != null) {
                                    val marker = Marker(map).apply {
                                        position = GeoPoint(lat, lon)
                                        title = pub.title
                                        snippet = pub.description
                                        icon = ContextCompat.getDrawable(context, android.R.drawable.star_big_on)
                                        setOnMarkerClickListener { _, _ ->
                                            selectedPublication = pub
                                            true
                                        }
                                    }
                                    map.overlays.add(marker)
                                }
                            }

                            map.invalidate()
                        }

                        if (viewModel.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }

                        viewModel.errorMessage?.let {
                            Text(
                                text = it,
                                color = Color.Red,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .background(Color.White.copy(alpha = 0.9f))
                                    .padding(8.dp)
                            )
                        }
                    }

                    // 🪟 Popup de publicación seleccionada
                    selectedPublication?.let { pub ->
                        Dialog(onDismissRequest = { selectedPublication = null }) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .background(Color.White)
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val authorName = remember(userMap, pub.userId) {
                                        userMap[pub.userId] ?: "Usuario desconocido"
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF355031))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = authorName,
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.weight(1f),
                                            color = Color.White
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Color.Red)
                                                .clickable { selectedPublication = null },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Cerrar",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    AsyncImage(
                                        model = pub.imageUrl,
                                        contentDescription = pub.title,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(pub.title, style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(pub.description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }

    }
    }
    */
    when {
        usarPuntoFijo -> {
            // El usuario decidió usar el punto fijo
            BuscarScreenMapa(
                navController = navController,
                loginViewModel = loginViewModel,
                usarPuntoFijo = true
            )
            return
        }

        locationEnabled == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        locationEnabled == false -> {
            PrendeUbicacionScreen(
                onOpenSettings = {
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                },
                onUsarPuntoFijo = {
                    usarPuntoFijo = true
                }
            )
            return
        }

        else -> {
            // Si la ubicación está activada
            BuscarScreenMapa(
                navController = navController,
                loginViewModel = loginViewModel,
                usarPuntoFijo = false
            )
            return
        }
    }
}
*/
/*
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun BuscarScreen(
    navController: NavController,
    loginViewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current

    // Estados principales
    var locationEnabled by remember { mutableStateOf<Boolean?>(null) }
    var usarPuntoFijo by remember { mutableStateOf(false) }
    var mostrarModalUbicacion by remember { mutableStateOf(false) }

    // ⚙️ Configuración para verificar la ubicación
    val settingsClient = remember { LocationServices.getSettingsClient(context) }
    val locationRequest = remember {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
    }
    val locationSettingsRequest = remember {
        LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()
    }

    // Launcher para resolución del sistema (activar ubicación)
    val resolutionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            locationEnabled = true
        } else {
            locationEnabled = false
        }
    }

    // 🚦 Verificar si el GPS está activo
    LaunchedEffect(Unit) {
        val task = settingsClient.checkLocationSettings(locationSettingsRequest)
        task.addOnSuccessListener {
            locationEnabled = true
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(e.resolution).build()
                    resolutionLauncher.launch(intentSenderRequest)
                } catch (_: IntentSender.SendIntentException) {
                    locationEnabled = false
                }
            } else {
                locationEnabled = false
            }
        }
    }

    // 🧭 Mostrar según estado de ubicación
    when {
        usarPuntoFijo -> {
            BuscarScreenMapa(
                navController = navController,
                loginViewModel = loginViewModel,
                usarPuntoFijo = true
            )
        }

        locationEnabled == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        locationEnabled == true -> {
            BuscarScreenMapa(
                navController = navController,
                loginViewModel = loginViewModel,
                usarPuntoFijo = false
            )
        }

        locationEnabled == false -> {
            // Mostrar el mapa pero con modal bloqueando interacción
            BuscarScreenMapa(
                navController = navController,
                loginViewModel = loginViewModel,
                usarPuntoFijo = false
            )
            mostrarModalUbicacion = true
        }
    }

    // 💬 Modal de “Activa tu ubicación”
    if (mostrarModalUbicacion && !usarPuntoFijo && locationEnabled == false) {
        PrendeUbicacionDialog(
            onOpenSettings = {
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                mostrarModalUbicacion = false
            },
            onUsarPuntoFijo = {
                usarPuntoFijo = true
                mostrarModalUbicacion = false
            }
        )
    }
}


@Composable
fun PrendeUbicacionScreen(
    onOpenSettings: () -> Unit,
    onUsarPuntoFijo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(34.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Activa tu ubicación para continuar", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onOpenSettings) {
            Text("Abrir ajustes de ubicación")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onUsarPuntoFijo) {
            Text("Usar punto fijo")
        }
    }
}
*/

@Composable
fun PrendeUbicacionDialog(
    onOpenSettings: () -> Unit,
    onUsarPuntoFijo: () -> Unit
) {
    Dialog(
        onDismissRequest = { /* no permitir cerrar tocando fuera */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .height(300.dp)
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            // Animación de entrada y salida
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(300)) + scaleIn(
                    initialScale = 0.85f,
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Activa tu ubicación para continuar",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onOpenSettings,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C9D6D)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Abrir ajustes de ubicación", color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = onUsarPuntoFijo,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C9D6D)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Usar punto fijo (Ciudad de México)", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun BuscarScreenMapa(
    navController: NavController,
    loginViewModel: LoginViewModel = viewModel(),
    usarPuntoFijo: Boolean
) {
    var userName by remember { mutableStateOf("") }
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    // ViewModels y repos
    val repo = remember { PublicationsMapRepository(RetrofitPublicationsInstance.api) }
    val factory = remember { BuscarViewModelFactory(repo) }
    val viewModel: BuscarViewModel = viewModel(factory = factory)

    val userRepo = remember { UserRepositoryImpl(RetrofitUsersInstance.api) }
    var userMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    // Estados UI
    var searchQuery by remember { mutableStateOf("") }
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }

    var showFullImage by remember { mutableStateOf(false) }
    var selectedPublication by remember { mutableStateOf<PublicationMap?>(null) }

    // ⚙️ Configurar mapa base
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        mapView.setTileSource(
            if (connectionIsSlow(context)) TileSourceFactory.USGS_SAT
            else TileSourceFactory.MAPNIK
        )
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(14.0)
    }

    // 🧩 Manejar permisos o punto fijo
    if (!usarPuntoFijo) {
        val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

        LaunchedEffect(locationPermissionState.status) {
            if (locationPermissionState.status.isGranted) {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        userLocation = GeoPoint(it.latitude, it.longitude)
                        mapView.controller.setCenter(userLocation)
                    }
                }
            } else {
                locationPermissionState.launchPermissionRequest()
            }
        }
    } else {
        // 📍 Punto fijo predeterminado
        val puntoFijo = GeoPoint(19.4326, -99.1332) // Ciudad de México, MX

        LaunchedEffect(Unit) {
            userLocation = puntoFijo
            mapView.controller.setCenter(puntoFijo)
            mapView.controller.setZoom(14.0)

        }
    }

    // 👤 Cargar datos de usuario y publicaciones
    LaunchedEffect(Unit) {
        userMap = emptyMap() // carga después
    }

    LaunchedEffect(key1 = loginViewModel.token) {
        val token = loginViewModel.token
        val userId = loginViewModel.userId
        if (!token.isNullOrEmpty()) {
            viewModel.loadPublications(userId, token)
            userMap = try { userRepo.getAllUsers(token).associate { it.id to it.name } }
            catch (_: Exception) { emptyMap() }
        }
    }

    // 🧱 UI principal
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar Aventuras Cercanas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = false,
                    onClick = { navController.navigate("publicaciones") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    label = { Text("Buscar") },
                    selected = true,
                    onClick = { /* ya estamos aquí */ }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = false,
                    onClick = { navController.navigate("perfil") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            // 🔍 Barra de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { newValue ->
                    searchQuery = sanitizeSearchInput(newValue)
                },
                placeholder = { Text("Buscar lugar o dirección...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3C9D6D),
                    unfocusedBorderColor = Color.Gray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = Color(0xFF3C9D6D),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                }
            )

            Button(
                onClick = {
                    if (searchQuery.isNotEmpty()) {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        try {
                            val results = geocoder.getFromLocationName(searchQuery, 1)
                            if (!results.isNullOrEmpty()) {
                                val loc = results[0]
                                val point = GeoPoint(loc.latitude, loc.longitude)
                                mapView.controller.setCenter(point)
                                mapView.controller.setZoom(16.0)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C9D6D))
            ) {
                Text("Buscar", color = Color.White)
            }

            // 🗺️ Mapa
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
            ) {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize()
                ) { map ->
                    map.overlays.clear()

                    // 📍 Marcador de usuario o punto fijo
                    userLocation?.let { location ->
                        val userMarker = Marker(map).apply {
                            position = location
                            title = if (usarPuntoFijo) "Punto fijo" else "Tu ubicación"
                            icon = ContextCompat.getDrawable(
                                context,
                                android.R.drawable.star_big_off
                            )
                        }
                        map.overlays.add(userMarker)
                        map.controller.setCenter(location)
                    }

                    // 📌 Marcadores de publicaciones
                    viewModel.publications.forEach { pub ->
                        val lat = pub.latitud.toDoubleOrNull()
                        val lon = pub.longitud.toDoubleOrNull()
                        if (lat != null && lon != null) {
                            val marker = Marker(map).apply {
                                position = GeoPoint(lat, lon)
                                title = pub.title
                                snippet = pub.description
                                icon = ContextCompat.getDrawable(
                                    context,
                                    android.R.drawable.star_big_on
                                )
                                setOnMarkerClickListener { _, _ ->
                                    selectedPublication = pub
                                    true
                                }
                            }
                            map.overlays.add(marker)
                        }
                    }

                    map.invalidate()
                }

                // ⏳ Cargando publicaciones
                if (viewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                // ⚠️ Error
                viewModel.errorMessage?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color.White.copy(alpha = 0.9f))
                            .padding(8.dp)
                    )
                }
            }

            // 🪟 Popup con detalle de publicación
            selectedPublication?.let { pub ->
                Dialog(onDismissRequest = { selectedPublication = null }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .background(Color.White)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val authorName = remember(userMap, pub.userId) {
                                userMap[pub.userId] ?: "Usuario desconocido"
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF355031))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = authorName,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f),
                                    color = Color.White
                                )
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red)
                                        .clickable { selectedPublication = null },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cerrar",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            AsyncImage(
                                model = pub.imageUrl,
                                contentDescription = pub.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                showFullImage = true
                            },
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(pub.title, style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(pub.description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

/*
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun BuscarScreen(
    navController: NavController,
    loginViewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var locationEnabled by remember { mutableStateOf(false) }
    val mapView = remember { MapView(context) }
    var showPrendeUbicacion by remember { mutableStateOf(false) }

    var selectedPublication by remember { mutableStateOf<PublicationMap?>(null) }
    var userMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var searchQuery by remember { mutableStateOf("") }

    val repo = remember { PublicationsMapRepository(RetrofitPublicationsInstance.api) }
    val factory = remember { BuscarViewModelFactory(repo) }
    val viewModel: BuscarViewModel = viewModel(factory = factory)
    val userRepo = remember { UserRepositoryImpl(RetrofitUsersInstance.api) }

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // Launcher para manejar diálogo del sistema para encender GPS
    val resolutionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Usuario presionó "OK" → usar ubicación real
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    userLocation = GeoPoint(it.latitude, it.longitude)
                    locationEnabled = true
                }
            }
        } else {
            // Usuario presionó "No, thanks" → usar ubicación fija
            userLocation = GeoPoint(19.432608, -99.133209) // Ciudad de México
            locationEnabled = true
        }
    }

    // Función para chequear configuración de GPS
    fun checkLocationSettings() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build()
        val settingsClient = LocationServices.getSettingsClient(context)
        settingsClient.checkLocationSettings(builder)
            .addOnSuccessListener {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        userLocation = GeoPoint(it.latitude, it.longitude)
                        locationEnabled = true
                    }
                }
            }
            .addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    val intentSenderRequest = IntentSenderRequest.Builder(e.resolution).build()
                    resolutionLauncher.launch(intentSenderRequest)
                } else {
                    userLocation = GeoPoint(19.432608, -99.133209) // Ciudad de México
                    locationEnabled = true
                }
            }
    }

    // 🔐 Validar sesión y cargar datos
    LaunchedEffect(Unit) {
        val isLoggedIn = loginViewModel.isLoggedIn()
        if (!isLoggedIn) {
            navController.navigate("login") { popUpTo("buscar") { inclusive = true } }
        }

        val userName = loginViewModel.userName
        val userId = loginViewModel.userId
        val token = loginViewModel.token

        if (!token.isNullOrEmpty() && userId.isNotEmpty()) {
            viewModel.loadPublications(userId, token)
            try {
                val users = userRepo.getAllUsers(token)
                userMap = users.associate { u -> u.id to u.name }
            } catch (_: Exception) { }
        }
    }

    // Configurar el mapa
    LaunchedEffect(mapView) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(14.0)
    }

    // Chequear permisos y GPS
    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            checkLocationSettings()
        } else {
            showPrendeUbicacion = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar Aventuras Cercanas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = false,
                    onClick = { navController.navigate("publicaciones") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    label = { Text("Buscar") },
                    selected = true,
                    onClick = { /* ya estás aquí */ }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = false,
                    onClick = { navController.navigate("perfil") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            // Barra de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar lugar o dirección...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3C9D6D),
                    unfocusedBorderColor = Color.Gray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = Color(0xFF3C9D6D),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                }
            )

            Button(
                onClick = {
                    if (searchQuery.isNotEmpty()) {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        try {
                            val results = geocoder.getFromLocationName(searchQuery, 1)
                            if (!results.isNullOrEmpty()) {
                                val loc = results[0]
                                val point = GeoPoint(loc.latitude, loc.longitude)
                                mapView.controller.setCenter(point)
                                mapView.controller.setZoom(16.0)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C9D6D))
            ) {
                Text("Buscar", color = Color.White)
            }

            // Mapa
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
            ) {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize()
                ) { map ->
                    map.overlays.clear()

                    userLocation?.let { location ->
                        val userMarker = Marker(map).apply {
                            position = location
                            title = "Tu ubicación"
                            icon = ContextCompat.getDrawable(context, android.R.drawable.star_big_off)
                        }
                        map.overlays.add(userMarker)
                        map.controller.setCenter(location)
                    }

                    viewModel.publications.forEach { pub ->
                        val lat = pub.latitud.toDoubleOrNull()
                        val lon = pub.longitud.toDoubleOrNull()
                        if (lat != null && lon != null) {
                            val marker = Marker(map).apply {
                                position = GeoPoint(lat, lon)
                                title = pub.title
                                snippet = pub.description
                                icon = ContextCompat.getDrawable(context, android.R.drawable.star_big_on)
                                setOnMarkerClickListener { _, _ ->
                                    selectedPublication = pub
                                    true
                                }
                            }
                            map.overlays.add(marker)
                        }
                    }

                    map.invalidate()
                }

                if (viewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                viewModel.errorMessage?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color.White.copy(alpha = 0.9f))
                            .padding(8.dp)
                    )
                }
            }

            // Popup de publicación seleccionada
            selectedPublication?.let { pub ->
                Dialog(onDismissRequest = { selectedPublication = null }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .background(Color.White)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val authorName = remember(userMap, pub.userId) {
                                userMap[pub.userId] ?: "Usuario desconocido"
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF355031))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = authorName,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f),
                                    color = Color.White
                                )
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red)
                                        .clickable { selectedPublication = null },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cerrar",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            AsyncImage(
                                model = pub.imageUrl,
                                contentDescription = pub.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(pub.title, style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(pub.description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                    }
                }
            }

            // Modal de permisos (solo si permisos denegados)
            if (showPrendeUbicacion) {
                PrendeUbicacionDialog(
                    onOpenSettings = {
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        context.startActivity(intent)
                    },
                    onUsarPuntoFijo = {
                        userLocation = GeoPoint(19.432608, -99.133209) // Ciudad de México
                        locationEnabled = true
                        showPrendeUbicacion = false
                    }
                )
            }
        }
    }
}
*/
/*
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun BuscarScreen(navController: NavController, loginViewModel: LoginViewModel = viewModel()) {
    var userName by remember { mutableStateOf("") }
    val repo = remember { PublicationsMapRepository(RetrofitPublicationsInstance.api) }
    val factory = remember { BuscarViewModelFactory(repo) }
    val viewModel: BuscarViewModel = viewModel(factory = factory)

    val context = LocalContext.current
    val activity = LocalContext.current as Activity
    val mapView = remember { MapView(context) }

    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var locationEnabled by remember { mutableStateOf(false) }

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    var selectedPublication by remember { mutableStateOf<PublicationMap?>(null) }
    val userRepo = remember { UserRepositoryImpl(RetrofitUsersInstance.api) }
    var userMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var searchQuery by remember { mutableStateOf("") }

    // Launcher para el diálogo del sistema
    val resolutionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Usuario activó ubicación: usamos ubicación real
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                userLocation = location?.let { GeoPoint(it.latitude, it.longitude) }
                locationEnabled = true
            }?.addOnFailureListener {
                userLocation = GeoPoint(19.432608, -99.133209)
                locationEnabled = true
            }
        } else {
            // Usuario dijo "No, thanks": usamos ubicación fija
            userLocation = GeoPoint(19.432608, -99.133209)
            locationEnabled = true
        }
    }

    // Función para chequear GPS y lanzar diálogo del sistema si está apagado
    fun checkGpsAndLaunch() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            // GPS ya estaba activado
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                userLocation = location?.let { GeoPoint(it.latitude, it.longitude) }
                locationEnabled = true
            }
        }

        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                // Lanzamos diálogo del sistema
                val intentSenderRequest = IntentSenderRequest.Builder(e.resolution).build()
                resolutionLauncher.launch(intentSenderRequest)
            } else {
                // Otro error, fallback a ubicación fija
                userLocation = GeoPoint(19.432608, -99.133209)
                locationEnabled = true
            }
        }
    }

    // Permisos de ubicación
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            checkGpsAndLaunch()
        } else {
            locationPermissionState.launchPermissionRequest()
        }
    }

    // 🔐 Validar si hay sesión
    LaunchedEffect(Unit) {
        val isLoggedIn = loginViewModel.isLoggedIn()
        if (!isLoggedIn) {
            navController.navigate("login") {
                popUpTo("buscar") { inclusive = true }
            }
        }

        userName = loginViewModel.userName
        val userId = loginViewModel.userId
        val token = loginViewModel.token

        if (!token.isNullOrEmpty() && userId.isNotEmpty()) {
            viewModel.loadPublications(userId, token)
            try {
                val users = userRepo.getAllUsers(token)
                userMap = users.associate { u -> u.id to u.name }
            } catch (_: Exception) { }
        }
    }

    // Configurar el mapa
    LaunchedEffect(mapView) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(14.0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar Aventuras Cercanas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = false,
                    onClick = { navController.navigate("publicaciones") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    label = { Text("Buscar") },
                    selected = true,
                    onClick = { /* ya está aquí */ }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = false,
                    onClick = { navController.navigate("perfil") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            // 🔍 Barra de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar lugar o dirección...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3C9D6D),
                    unfocusedBorderColor = Color.Gray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = Color(0xFF3C9D6D),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                }
            )

            Button(
                onClick = {
                    if (searchQuery.isNotEmpty()) {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        try {
                            val results = geocoder.getFromLocationName(searchQuery, 1)
                            if (!results.isNullOrEmpty()) {
                                val loc = results[0]
                                val point = GeoPoint(loc.latitude, loc.longitude)
                                mapView.controller.setCenter(point)
                                mapView.controller.setZoom(16.0)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C9D6D))
            ) {
                Text("Buscar", color = Color.White)
            }

            // 🗺️ Mapa
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
            ) {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize()
                ) { map ->
                    map.overlays.clear()

                    userLocation?.let { location ->
                        val userMarker = Marker(map).apply {
                            position = location
                            title = "Tu ubicación"
                            icon = ContextCompat.getDrawable(context, android.R.drawable.star_big_off)
                        }
                        map.overlays.add(userMarker)
                        map.controller.setCenter(location)
                    }

                    // ✅ Mostrar publicaciones cercanas
                    viewModel.publications.forEach { pub ->
                        val lat = pub.latitud.toDoubleOrNull()
                        val lon = pub.longitud.toDoubleOrNull()
                        if (lat != null && lon != null) {
                            val marker = Marker(map).apply {
                                position = GeoPoint(lat, lon)
                                title = pub.title
                                snippet = pub.description
                                icon = ContextCompat.getDrawable(context, android.R.drawable.star_big_on)
                                setOnMarkerClickListener { _, _ ->
                                    selectedPublication = pub
                                    true
                                }
                            }
                            map.overlays.add(marker)
                        }
                    }

                    map.invalidate()
                }

                if (viewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                viewModel.errorMessage?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color.White.copy(alpha = 0.9f))
                            .padding(8.dp)
                    )
                }
            }

            // 🪟 Popup de publicación seleccionada
            selectedPublication?.let { pub ->
                Dialog(onDismissRequest = { selectedPublication = null }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .background(Color.White)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val authorName = remember(userMap, pub.userId) {
                                userMap[pub.userId] ?: "Usuario desconocido"
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF355031))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = authorName,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f),
                                    color = Color.White
                                )
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red)
                                        .clickable { selectedPublication = null },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cerrar",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            AsyncImage(
                                model = pub.imageUrl,
                                contentDescription = pub.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(pub.title, style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(pub.description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
*/

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun BuscarScreen(navController: NavController, loginViewModel: LoginViewModel = viewModel()) {
    var userName by remember { mutableStateOf("") }
    val repo = remember { PublicationsMapRepository(RetrofitPublicationsInstance.api) }
    val factory = remember { BuscarViewModelFactory(repo) }
    val viewModel: BuscarViewModel = viewModel(factory = factory)

    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var locationEnabled by remember { mutableStateOf(false) }
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    var selectedPublication by remember { mutableStateOf<PublicationMap?>(null) }
    val userRepo = remember { UserRepositoryImpl(RetrofitUsersInstance.api) }
    var userMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var searchQuery by remember { mutableStateOf("") }

    var isImageLoading by remember { mutableStateOf(true) }
    var showFullImage by remember { mutableStateOf(false) }
    var showFullDescription by remember { mutableStateOf(false) }

    // Launcher para dialogo del sistema
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val locationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    val resolutionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Usuario dijo OK → obtener ubicación real
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        userLocation = GeoPoint(location.latitude, location.longitude)
                    } else {
                        userLocation = GeoPoint(19.432608, -99.133209) // fallback
                    }
                    locationEnabled = true
                }
        } else {
            // Usuario dijo No → usar punto fijo
            userLocation = GeoPoint(19.432608, -99.133209)
            locationEnabled = true
        }
    }

    // Validar sesión y cargar datos
    LaunchedEffect(Unit) {
        val isLoggedIn = loginViewModel.isLoggedIn()
        if (!isLoggedIn) {
            navController.navigate("login") { popUpTo("buscar") { inclusive = true } }
        }
        userName = loginViewModel.userName
        val userId = loginViewModel.userId
        val token = loginViewModel.token

        if (!token.isNullOrEmpty() && userId.isNotEmpty()) {
            viewModel.loadPublications(userId, token)
            try {
                val users = userRepo.getAllUsers(token)
                userMap = users.associate { u -> u.id to u.name }
            } catch (_: Exception) { }
        }
    }

    // Configurar mapa
    LaunchedEffect(mapView) {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName

            // 🔥 Cache interno (mucho más rápido)
            osmdroidBasePath = File(context.cacheDir, "osmdroid")
            osmdroidTileCache = File(osmdroidBasePath, "tiles")

            // 🔥 Menos hilos para evitar cuelgues
            tileDownloadThreads = 2
            tileFileSystemThreads = 2

            // 🔥 Evitar sobrecarga
            cacheMapTileCount = 4000
            cacheMapTileOvershoot = 100
        }
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(14.0)
    }

    // Lanzar solicitud de permisos
    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            // Comprobar si GPS está activado
            val locationRequestCheck = LocationRequest.create()
            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequestCheck)
            val client = LocationServices.getSettingsClient(context)
            val task = client.checkLocationSettings(builder.build())
            task.addOnSuccessListener {
                // GPS activado → obtener ubicación
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            userLocation = GeoPoint(location.latitude, location.longitude)
                        } else {
                            userLocation = GeoPoint(19.432608, -99.133209)
                        }
                        locationEnabled = true
                    }
            }
            task.addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        resolutionLauncher.launch(
                            IntentSenderRequest.Builder(exception.resolution).build()
                        )
                    } catch (sendEx: Exception) {
                        // fallback
                        userLocation = GeoPoint(19.432608, -99.133209)
                        locationEnabled = true
                    }
                } else {
                    userLocation = GeoPoint(19.432608, -99.133209)
                    locationEnabled = true
                }
            }
        } else {
            locationPermissionState.launchPermissionRequest()
        }
    }

    // Actualizar el mapa cuando cambie la ubicación
    LaunchedEffect(userLocation) {
        userLocation?.let { loc ->
            mapView.controller.setCenter(loc)
            mapView.controller.setZoom(16.0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar Aventuras Cercanas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = false,
                    onClick = { navController.navigate("publicaciones") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    label = { Text("Buscar") },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = false,
                    onClick = { navController.navigate("perfil") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
           /* // Buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar lugar o dirección...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3C9D6D),
                    unfocusedBorderColor = Color.Gray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = Color(0xFF3C9D6D),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                }
            )

            Button(
                onClick = {
                    if (searchQuery.isNotEmpty()) {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        try {
                            val results = geocoder.getFromLocationName(searchQuery, 1)
                            if (!results.isNullOrEmpty()) {
                                val loc = results[0]
                                val point = GeoPoint(loc.latitude, loc.longitude)
                                mapView.controller.setCenter(point)
                                mapView.controller.setZoom(36.0)
                            }
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C9D6D))
            ) {
                Text("Buscar", color = Color.White)
            }*/

            // Row con buscador y botón de ubicación
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { newValue ->
                        searchQuery = sanitizeSearchInput(newValue)
                    },
                    placeholder = { Text("Buscar lugar o dirección...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3C9D6D),
                        unfocusedBorderColor = Color.Gray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        cursorColor = Color(0xFF3C9D6D),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    trailingIcon = {
                        Row {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Limpiar")
                                }
                            }
                            IconButton(
                                onClick = {
                                    userLocation?.let { loc ->
                                        mapView.controller.setCenter(loc)
                                        mapView.controller.setZoom(16.0)
                                    }
                                },
                                enabled = userLocation != null
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = "Mi ubicación")
                            }
                        }
                    }
                )
            }

// Botón de búsqueda
            Button(
                onClick = {
                    if (searchQuery.isNotEmpty()) {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        try {
                            val results = geocoder.getFromLocationName(searchQuery, 1)
                            if (!results.isNullOrEmpty()) {
                                val loc = results[0]
                                val point = GeoPoint(loc.latitude, loc.longitude)
                                mapView.controller.setCenter(point)
                                mapView.controller.setZoom(16.0)
                            }
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                },
                enabled = searchQuery.isNotEmpty(), // desactivado si está vacío
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (searchQuery.isNotEmpty()) Color(0xFF3C9D6D) else Color.Gray
                )
            ) {
                Text("Buscar", color = Color.White)
            }


            LaunchedEffect(userLocation) {
                userLocation?.let { loc ->
                    mapView.controller.setCenter(loc)
                    mapView.controller.setZoom(16.0)
                }
            }

            // Mapa
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
            ) {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize()
                ) { map ->
                    map.overlays.removeIf { it is Marker && it.title != "Tu ubicación" }

                    userLocation?.let { location ->
                        val userMarker = Marker(map).apply {
                            position = location
                            title = "Tu ubicación"
                            icon = ContextCompat.getDrawable(context, android.R.drawable.star_big_off)
                        }
                        map.overlays.add(userMarker)
                    }

                    // Publicaciones cercanas
                    viewModel.publications.forEach { pub ->
                        val lat = pub.latitud.toDoubleOrNull()
                        val lon = pub.longitud.toDoubleOrNull()
                        if (lat != null && lon != null) {
                            val marker = Marker(map).apply {
                                position = GeoPoint(lat, lon)
                                title = pub.title
                                snippet = pub.description
                                icon = ContextCompat.getDrawable(context, android.R.drawable.star_big_on)
                                setOnMarkerClickListener { _, _ ->
                                    selectedPublication = pub
                                    true
                                }
                            }
                            map.overlays.add(marker)
                        }
                    }

                    map.invalidate()
                }

                if (viewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                viewModel.errorMessage?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color.White.copy(alpha = 0.9f))
                            .padding(8.dp)
                    )
                }
            }

            // Popup de publicación
            selectedPublication?.let { pub ->
                Dialog(onDismissRequest = { selectedPublication = null }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .background(Color.White)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val authorName = remember(userMap, pub.userId) {
                                userMap[pub.userId] ?: "Usuario desconocido"
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF355031))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = authorName,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f),
                                    color = Color.White
                                )
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red)
                                        .clickable { selectedPublication = null },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cerrar",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            // Imagen con loader bonito
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFEAEAEA)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = pub.imageUrl,
                                    contentDescription = pub.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            showFullImage = true
                                        },
                                    contentScale = ContentScale.Crop
                                )

                                if (isImageLoading) {
                                    CircularProgressIndicator(color = Color(0xFF3C9D6D))
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(pub.title, style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            val maxChars = 160

                            Column {
                                Text(
                                    text = if (showFullDescription) pub.description else pub.description.take(maxChars),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )

                                if (pub.description.length > maxChars) {
                                    Text(
                                        text = if (showFullDescription) "Ver menos" else "Ver más",
                                        color = Color(0xFF3C9D6D),
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .clickable { showFullDescription = !showFullDescription }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 📌 VISOR DE IMAGEN COMPLETA — Zoom real sin mover la imagen completa
            if (showFullImage && selectedPublication != null) {

                Dialog(
                    onDismissRequest = { showFullImage = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {

                    var scale by remember { mutableStateOf(1f) }
                    var offsetX by remember { mutableStateOf(0f) }
                    var offsetY by remember { mutableStateOf(0f) }

                    var loading by remember { mutableStateOf(true) }

                    // 🔥 El pan y zoom SOLO afecta la imagen, no el contenedor
                    val transformState = rememberTransformableState { zoomChange, panChange, _ ->

                        val newScale = (scale * zoomChange).coerceIn(1f, 4f)

                        // Límites para NO sacar la imagen de pantalla
                        val maxX = (newScale - 1f) * 500f
                        val maxY = (newScale - 1f) * 800f

                        offsetX = (offsetX + panChange.x).coerceIn(-maxX, maxX)
                        offsetY = (offsetY + panChange.y).coerceIn(-maxY, maxY)

                        scale = newScale
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {

                        // 🔥 AQUÍ va el transformable (solo sobre la imagen)
                        AsyncImage(
                            model = selectedPublication!!.imageUrl,
                            contentDescription = "Vista completa",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offsetX,
                                    translationY = offsetY
                                )
                                .transformable(transformState), // 👈 SOLO aquí
                            contentScale = ContentScale.Fit,
                            onSuccess = { loading = false },
                            onError = { loading = false }
                        )

                        if (loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(50.dp)
                            )
                        }

                        IconButton(
                            onClick = { showFullImage = false },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .size(42.dp)
                                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

fun connectionIsSlow(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return true
    val caps = cm.getNetworkCapabilities(network) ?: return true
    return caps.linkDownstreamBandwidthKbps < 1500
}

fun sanitizeSearchInput(text: String): String {
    // Lista de caracteres peligrosos SIN incluir el espacio
    val forbidden = listOf('<', '>', '/', '\\', '"', '\'', '{', '}', '`', '=', ';')

    var clean = text
    forbidden.forEach { char ->
        clean = clean.replace(char.toString(), "")
    }

    return clean
}
