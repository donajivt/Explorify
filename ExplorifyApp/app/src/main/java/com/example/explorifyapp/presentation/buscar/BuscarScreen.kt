package com.example.explorifyapp.presentation.buscar


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.explorifyapp.presentation.login.LoginViewModel
import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.runtime.*
import com.google.android.gms.location.LocationServices
import com.google.maps.android.compose.*
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import androidx.compose.ui.viewinterop.AndroidView
import android.R.attr.title
import android.R.attr.icon
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import android.content.Context
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun BuscarScreen(navController: NavController, loginViewModel: LoginViewModel = viewModel(),) {
    var menuExpanded by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("") }

    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // 🔐 Validar si hay sesión
    LaunchedEffect(Unit) {
        val isLoggedIn = loginViewModel.isLoggedIn()
        if (!isLoggedIn) {
            navController.navigate("login") {
                popUpTo("buscar") { inclusive = true }
            }
        }
        //permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        userName=loginViewModel.userName
    }

    // Configurar el mapa una sola vez
    LaunchedEffect(mapView) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(14.0)
    }


    // Obtener ubicación actual si tiene permiso
    /*if (hasPermission) {
        val fusedLocationClient = remember {
            LocationServices.getFusedLocationProviderClient(context)
        }

        LaunchedEffect(Unit) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        userLocation = GeoPoint(location.latitude, location.longitude)
                        mapView.controller.setCenter(userLocation)
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }*/
    LaunchedEffect(locationPermissionState.status) { // ⚠️ aquí usamos status
        if (locationPermissionState.status.isGranted) { // ✅ comprobar si está concedido
            //val context = LocalContext.current
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    userLocation = GeoPoint(it.latitude, it.longitude)
                    mapView.controller.setCenter(userLocation)
                }
            }
        } else {
            locationPermissionState.launchPermissionRequest() // pedir permiso
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
                actions = {
                    // Menú de perfil con logout
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Perfil")
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Perfil") },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate("perfil")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Cerrar sesión") },
                                onClick = {
                                    menuExpanded = false
                                    loginViewModel.logout {
                                        navController.navigate("login") {
                                            popUpTo("mypublications") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
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
                    onClick = { navController.navigate("inicio/${userName}") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    label = { Text("Buscar") },
                    selected = true,
                    onClick = { navController.navigate("buscar") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = false,
                    onClick = {navController.navigate("perfil")}
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize()
            ) { map ->

                map.overlays.clear()

                userLocation?.let { location ->
                    // Centrar el mapa


                    // Marcador de la ubicación del usuario
                    val userMarker = Marker(map).apply {
                        position = location
                        title = "Tu ubicación"
                        icon = ContextCompat.getDrawable(context, android.R.drawable.star_big_off)//ic_menu_mylocation
                    }
                    map.overlays.add(userMarker)
                    map.controller.setZoom(15.0)
                    map.controller.setCenter(location)
                }
                //map.invalidate()
                // 🔍 Ejemplo: publicaciones cercanas (simuladas)
                val publicaciones = listOf(
                    GeoPoint(19.433, -99.133), // CDMX centro
                    GeoPoint(19.440, -99.140), // Lugar cercano
                )

                publicaciones.forEachIndexed { index, pub ->
                    val marker = Marker(map).apply {
                        position = pub
                        title = "Publicación #${index + 1}"
                        snippet = "Cerca de ti"
                        icon = ContextCompat.getDrawable(context, android.R.drawable.star_big_on)
                    }
                    map.overlays.add(marker)
                }

                map.invalidate()
            }
        }
    }
}