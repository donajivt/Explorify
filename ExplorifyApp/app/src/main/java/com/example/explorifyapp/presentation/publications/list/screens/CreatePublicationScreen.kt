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
    val imageUrl = remember { "https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885__480.jpg" }

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
                title = { Text("Nueva Publicación", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Volver")
                    }
                },
                actions = {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Perfil",
                        tint = Color(0xFF355E3B),
                        modifier = Modifier.size(40.dp)
                    )
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Button(
                onClick = {
                    if (isPublishing || ui.loading) return@Button

                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()

                        when {
                            description.isBlank() || location.isBlank() ->{
                                snackbarHostState.showSnackbar("Debes llenar descripcion y ubicacion", duration = SnackbarDuration.Short)
                                return@launch
                            }
                            description.isBlank() -> {
                                snackbarHostState.showSnackbar("Debes llenar la descripción", duration = SnackbarDuration.Short)
                                return@launch
                            }

                            location.isBlank() ->{
                                snackbarHostState.showSnackbar("Debes llenar la ubicacion", duration = SnackbarDuration.Short)
                                return@launch
                            }

                            userIdState.isNullOrEmpty() -> {
                                snackbarHostState.showSnackbar("No se encontró el usuario autenticado", duration = SnackbarDuration.Short)
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
                    .padding(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF355E3B))
            ) {
                if (vm.uiState.loading)
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                else
                    Text("Publicar", color = Color.White)
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(10.dp))

            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFDDF5E3),  // 💚 Fondo activo (resalta)
                    unfocusedContainerColor = Color.White,      // 🤍 Fondo inactivo
                    disabledContainerColor = Color.White,
                    focusedTextColor = Color(0xFF2B2B2B),       // Texto oscuro
                    unfocusedTextColor = Color(0xFF2B2B2B),
                    disabledTextColor = Color(0xFF2B2B2B),
                    cursorColor = Color(0xFF3C9D6D),            // Cursor verde
                    focusedLabelColor = Color(0xFF3C9D6D),      // Label verde activo
                    unfocusedLabelColor = Color(0xFF6B4F3B),    // Label café inactivo
                    disabledLabelColor = Color(0xFF6B4F3B),
                    focusedBorderColor = Color(0xFF3C9D6D),     // Borde verde
                    unfocusedBorderColor = Color(0xFFBFAE94),   // Borde gris arena
                    disabledBorderColor = Color(0xFFBFAE94)
                )
            )


            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFDDF5E3),
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedTextColor = Color(0xFF2B2B2B),
                    unfocusedTextColor = Color(0xFF2B2B2B),
                    disabledTextColor = Color(0xFF2B2B2B),
                    cursorColor = Color(0xFF3C9D6D),
                    focusedLabelColor = Color(0xFF3C9D6D),
                    unfocusedLabelColor = Color(0xFF6B4F3B),
                    disabledLabelColor = Color(0xFF6B4F3B),
                    focusedBorderColor = Color(0xFF3C9D6D),
                    unfocusedBorderColor = Color(0xFFBFAE94),
                    disabledBorderColor = Color(0xFFBFAE94)
                )
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = location,
                onValueChange = {},
                label = { Text("Ubicación") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("map_picker") },
                enabled = false,
                readOnly = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedTextColor = Color(0xFF2B2B2B),
                    unfocusedTextColor = Color(0xFF2B2B2B),
                    disabledTextColor = Color(0xFF2B2B2B),
                    cursorColor = Color(0xFF3C9D6D),
                    focusedLabelColor = Color(0xFF3C9D6D),
                    unfocusedLabelColor = Color(0xFF6B4F3B),
                    disabledLabelColor = Color(0xFF6B4F3B),
                    focusedBorderColor = Color(0xFF3C9D6D),
                    unfocusedBorderColor = Color(0xFFBFAE94),
                    disabledBorderColor = Color(0xFFBFAE94)
                )
            )

        }
    }
}
