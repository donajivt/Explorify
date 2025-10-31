package com.example.explorifyapp.presentation.publications.list.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.explorifyapp.data.remote.room.AppDatabase
import com.example.explorifyapp.presentation.publications.components.MapPickerView
import com.example.explorifyapp.presentation.publications.list.CreatePublicationViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.OutlinedTextFieldDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePublicationScreen(
    vm: CreatePublicationViewModel,
    navController: NavController,
    onBack: () -> Unit,
    onPublishDone: () -> Unit,
    userId: String
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var token by remember { mutableStateOf<String?>(null) }
    var userIdState by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(context).authTokenDao()
            token = dao.getToken()?.token
            userIdState = dao.getToken()?.userId
        }
    }

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var latitud by remember { mutableStateOf<String?>(null) }
    var longitud by remember { mutableStateOf<String?>(null) }
    val imageUrl =
        remember { "https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885__480.jpg" }

    val ui = vm.uiState
    var isPublishing by remember { mutableStateOf(false) }

    // Resultados del mapa
    LaunchedEffect(navController.currentBackStackEntry?.savedStateHandle?.get<String>("selected_location_name")) {
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
        savedStateHandle?.get<String>("selected_location_name")?.let {
            location = it
            latitud = savedStateHandle.get<String>("selected_latitude")
            longitud = savedStateHandle.get<String>("selected_longitude")
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Nueva Aventura",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E473B)
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Volver",
                            tint = Color(0xFF3C9D6D)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF6F4EF)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        if (isPublishing || ui.loading) return@Button
                        scope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            when {
                                description.isBlank() || location.isBlank() -> {
                                    snackbarHostState.showSnackbar(
                                        "Debes llenar descripción y ubicación",
                                        duration = SnackbarDuration.Short
                                    )
                                    return@launch
                                }

                                userIdState.isNullOrEmpty() -> {
                                    snackbarHostState.showSnackbar(
                                        "No se encontró el usuario autenticado",
                                        duration = SnackbarDuration.Short
                                    )
                                    return@launch
                                }

                                else -> {
                                    isPublishing = true
                                    vm.createPublication(
                                        context = context,
                                        imageUrl = imageUrl,
                                        title = title,
                                        description = description,
                                        location = location,
                                        latitud = latitud,
                                        longitud = longitud,
                                        userId = userIdState!!,
                                        onDone = { onPublishDone() }
                                    )
                                }
                            }
                        }
                    },
                    enabled = !ui.loading && !isPublishing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C9D6D))
                ) {
                    if (ui.loading)
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    else
                        Text(
                            "Publicar aventura",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(
                            Color(0xFFF6F4EF),
                            Color(0xFFDDF5E3)
                        )
                    )
                )
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            // Imagen decorativa
            Card(
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(26.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título (opcional)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color(0xFF3C9D6D)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFDDF5E3),
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF3C9D6D),
                    unfocusedBorderColor = Color(0xFFBFAE94),
                    focusedLabelColor = Color(0xFF3C9D6D),
                    unfocusedLabelColor = Color(0xFF6B4F3B),
                    cursorColor = Color(0xFF3C9D6D),
                    focusedTextColor = Color(0xFF2B2B2B),
                    unfocusedTextColor = Color(0xFF2B2B2B),
                    disabledTextColor = Color(0xFFB0B0B0)
                )
            )

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color(0xFF3C9D6D)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFDDF5E3),
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF3C9D6D),
                    unfocusedBorderColor = Color(0xFFBFAE94),
                    focusedLabelColor = Color(0xFF3C9D6D),
                    unfocusedLabelColor = Color(0xFF6B4F3B),
                    cursorColor = Color(0xFF3C9D6D),
                    focusedTextColor = Color(0xFF2B2B2B),
                    unfocusedTextColor = Color(0xFF2B2B2B),
                    disabledTextColor = Color(0xFFB0B0B0)
                )
            )

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = location,
                onValueChange = {},
                label = { Text("Ubicación") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = if (location.isNotBlank()) Color(0xFF3C9D6D) else Color(0xFF6B4F3B)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("map_picker") },
                enabled = false,
                readOnly = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = if (location.isNotBlank()) Color(0xFFDDF5E3) else Color.White, // 💚 verde claro si hay ubicación
                    unfocusedContainerColor = if (location.isNotBlank()) Color(0xFFDDF5E3) else Color.White,
                    disabledContainerColor = if (location.isNotBlank()) Color(0xFFDDF5E3) else Color.White,
                    focusedTextColor = Color(0xFF2B2B2B),
                    unfocusedTextColor = Color(0xFF2B2B2B),
                    disabledTextColor = Color(0xFF2B2B2B),
                    cursorColor = Color(0xFF3C9D6D),
                    focusedLabelColor = if (location.isNotBlank()) Color(0xFF3C9D6D) else Color(0xFF6B4F3B),
                    unfocusedLabelColor = Color(0xFF6B4F3B),
                    disabledLabelColor = Color(0xFF6B4F3B),
                    focusedBorderColor = if (location.isNotBlank()) Color(0xFF3C9D6D) else Color(0xFFBFAE94),
                    unfocusedBorderColor = if (location.isNotBlank()) Color(0xFF3C9D6D) else Color(0xFFBFAE94),
                    disabledBorderColor = if (location.isNotBlank()) Color(0xFF3C9D6D) else Color(0xFFBFAE94)
                )
            )

            Spacer(Modifier.height(30.dp))
        }
    }
}