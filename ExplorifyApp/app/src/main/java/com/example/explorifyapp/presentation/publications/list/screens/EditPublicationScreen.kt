package com.example.explorifyapp.presentation.publications.list.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.explorifyapp.data.remote.publications.RetrofitPublicationsInstance
import com.example.explorifyapp.domain.repository.PublicationRepositoryImpl
import com.example.explorifyapp.presentation.login.LoginViewModel
import kotlinx.coroutines.Job
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPublicationScreen(
    publicationId: String,
    navController: NavController,
    token: String,
    userId: String,
    loginViewModel: LoginViewModel = viewModel()
) {
    val repo = remember { PublicationRepositoryImpl(RetrofitPublicationsInstance.api) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var latitud by remember { mutableStateOf<String?>(null) }
    var longitud by remember { mutableStateOf<String?>(null) }
    var imageUrl by rememberSaveable { mutableStateOf("") }

    var hasLoadedOnce by rememberSaveable { mutableStateOf(false) }

    // 🔹 Cargar datos solo una vez
    LaunchedEffect(publicationId) {
        if (!hasLoadedOnce && token.isNotEmpty() && userId.isNotEmpty()) {
            hasLoadedOnce = true
            isLoading = true
            try {
                val pub = repo.getUserPublications(userId, token).find { it.id == publicationId }
                pub?.let {
                    title = it.title
                    description = it.description
                    location = it.location
                    latitud = it.latitud
                    longitud = it.longitud
                    imageUrl = it.imageUrl
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Error al cargar la publicación")
            } finally {
                isLoading = false
            }
        }
    }

    // 🔁 Escuchar cambios del mapa (cuando regresa de MapPickerScreen)
    LaunchedEffect(Unit) {
        val handle = navController.currentBackStackEntry?.savedStateHandle
        handle?.getLiveData<String>("selected_location_name")?.observeForever { name ->
            val lat = handle.get<String>("selected_latitude")
            val lon = handle.get<String>("selected_longitude")

            if (!name.isNullOrEmpty() && !lat.isNullOrEmpty() && !lon.isNullOrEmpty()) {
                location = name
                latitud = lat
                longitud = lon
                println("📍 Nueva ubicación seleccionada: $location ($lat, $lon)")
                handle.remove<String>("selected_location_name")
                handle.remove<String>("selected_latitude")
                handle.remove<String>("selected_longitude")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar publicación") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                Modifier
                    .padding(padding)
                    .padding(horizontal = 20.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Edita tu publicación", style = MaterialTheme.typography.titleMedium)

                // 🖼️ Imagen (solo vista previa, no editable)
                if (imageUrl.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = "Vista previa de imagen",
                        modifier = Modifier
                            .size(160.dp)
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Imagen por defecto si no tiene
                    Image(
                        painter = rememberAsyncImagePainter(
                            "https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885__480.jpg"
                        ),
                        contentDescription = "Imagen por defecto",
                        modifier = Modifier
                            .size(160.dp)
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF3A3A3A),   // Fondo al enfocar
                        unfocusedContainerColor = Color(0xFF2C2C2C), // Fondo normal
                        disabledContainerColor = Color(0xFF2C2C2C),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.White,
                        cursorColor = Color(0xFF3C9D6D),             // Cursor verde
                        focusedLabelColor = Color(0xFF3C9D6D),       // Label verde al enfocar
                        unfocusedLabelColor = Color(0xFFAFAFAF),     // Label gris inactivo
                        disabledLabelColor = Color(0xFFAFAFAF),
                        focusedBorderColor = Color(0xFF3C9D6D),      // Borde verde activo
                        unfocusedBorderColor = Color(0xFFAFAFAF),    // Borde gris inactivo
                        disabledBorderColor = Color(0xFFAFAFAF)
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF3A3A3A),
                        unfocusedContainerColor = Color(0xFF2C2C2C),
                        disabledContainerColor = Color(0xFF2C2C2C),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.White,
                        cursorColor = Color(0xFF3C9D6D),
                        focusedLabelColor = Color(0xFF3C9D6D),
                        unfocusedLabelColor = Color(0xFFAFAFAF),
                        disabledLabelColor = Color(0xFFAFAFAF),
                        focusedBorderColor = Color(0xFF3C9D6D),
                        unfocusedBorderColor = Color(0xFFAFAFAF),
                        disabledBorderColor = Color(0xFFAFAFAF)
                    )
                )

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
                        focusedContainerColor = Color(0xFF2C2C2C),
                        unfocusedContainerColor = Color(0xFF2C2C2C),
                        disabledContainerColor = Color(0xFF2C2C2C),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.White,
                        cursorColor = Color(0xFF3C9D6D),
                        focusedLabelColor = Color(0xFF3C9D6D),
                        unfocusedLabelColor = Color(0xFFAFAFAF),
                        disabledLabelColor = Color(0xFFAFAFAF),
                        focusedBorderColor = Color(0xFF3C9D6D),
                        unfocusedBorderColor = Color(0xFFAFAFAF),
                        disabledBorderColor = Color(0xFFAFAFAF)
                    )
                )

                Spacer(Modifier.height(10.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                repo.update(
                                    id = publicationId,
                                    imageUrl = if (imageUrl.isBlank()) "https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885__480.jpg" else imageUrl, // ✅ se conserva la actual
                                    title = title,
                                    description = description,
                                    location = location,
                                    latitud = latitud,
                                    longitud = longitud,
                                    userId = userId,
                                    token = token
                                )
                                snackbarHostState.showSnackbar("Publicación actualizada correctamente")
                                delay(1000)
                                navController.popBackStack()
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Error al actualizar")
                            }
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Guardar cambios")
                }
            }
        }
    }
}
