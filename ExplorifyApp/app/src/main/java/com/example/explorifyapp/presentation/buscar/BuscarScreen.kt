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
import com.example.explorifyapp.data.remote.publications.RetrofitPublicationsInstance
import com.example.explorifyapp.presentation.publicaciones.MyPublicationsViewModel
import com.example.explorifyapp.presentation.publicaciones.MyPublicationsViewModelFactory
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted
import com.example.explorifyapp.domain.repository.PublicationsMapRepository
import android.widget.Toast
import com.example.explorifyapp.data.remote.dto.PublicationMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Block
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.Close

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun BuscarScreen(navController: NavController, loginViewModel: LoginViewModel = viewModel(),) {
    var menuExpanded by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("") }

    val repo = remember { PublicationsMapRepository(RetrofitPublicationsInstance.api) }
    val factory = remember { BuscarViewModelFactory(repo) }
    val viewModel: BuscarViewModel = viewModel(factory = factory)

    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    var selectedPublication by remember { mutableStateOf<PublicationMap?>(null) }
    val publications by viewModel::publications

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
        val userId = loginViewModel.userId
        val token = loginViewModel.token
        // posiblemente también usar userId si lo tienes
        if (!token.isNullOrEmpty() && userId.isNotEmpty()) {
            viewModel.loadPublications(userId, token)
        }
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
                    onClick = { navController.navigate("publicaciones") }
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
               /* val publicaciones = listOf(
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
                }*/

                // 🗺️ Agregar publicaciones del ViewModel al mapa
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
                               /* Toast.makeText(
                                    context,
                                    "${pub.title}\n${pub.description}",
                                    Toast.LENGTH_SHORT
                                ).show()*/
                                selectedPublication = pub
                                true
                            }
                        }
                        map.overlays.add(marker)
                    }
                }

                map.invalidate()
            }

            // 🔄 Loading / error
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            viewModel.errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
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
                            // 🧭 Encabezado con userId y botón "X"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = " ${pub.userId}",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f),
                                    color = Color.Gray
                                )

                                IconButton(
                                    onClick = { selectedPublication = null },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cerrar",
                                        tint = Color.Red
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            // 🖼 Imagen de la publicación
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