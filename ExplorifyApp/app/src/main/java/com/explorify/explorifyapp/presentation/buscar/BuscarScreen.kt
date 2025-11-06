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
