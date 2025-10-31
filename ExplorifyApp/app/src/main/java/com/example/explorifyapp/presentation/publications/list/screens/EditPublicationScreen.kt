package com.example.explorifyapp.presentation.publications.list.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.LocationOn
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
import androidx.compose.ui.text.font.FontWeight
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
                handle.remove<String>("selected_location_name")
                handle.remove<String>("selected_latitude")
                handle.remove<String>("selected_longitude")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Editar publicación",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color(0xFF2E473B),
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color(0xFF3C9D6D)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF2F0EC)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        if (isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF3C9D6D))
            }
        } else {
            Column(
                Modifier
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
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {

                // 🖼️ Imagen previa
                Card(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(MaterialTheme.shapes.large),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F0EC))
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            if (imageUrl.isNotEmpty()) imageUrl
                            else "https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885__480.jpg"
                        ),
                        contentDescription = "Vista previa de imagen",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Campos
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFDDF5E3),
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color(0xFF2B2B2B),
                        unfocusedTextColor = Color(0xFF2B2B2B),
                        cursorColor = Color(0xFF3C9D6D),
                        focusedBorderColor = Color(0xFF3C9D6D),
                        unfocusedBorderColor = Color(0xFFBFAE94),
                        focusedLabelColor = Color(0xFF3C9D6D),
                        unfocusedLabelColor = Color(0xFF6B4F3B)
                    )
                )

                Spacer(Modifier.height(14.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFDDF5E3),
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color(0xFF2B2B2B),
                        unfocusedTextColor = Color(0xFF2B2B2B),
                        cursorColor = Color(0xFF3C9D6D),
                        focusedBorderColor = Color(0xFF3C9D6D),
                        unfocusedBorderColor = Color(0xFFBFAE94),
                        focusedLabelColor = Color(0xFF3C9D6D),
                        unfocusedLabelColor = Color(0xFF6B4F3B)
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
                            tint = if (location.isNotBlank()) Color(0xFF3C9D6D) else Color(
                                0xFF6B4F3B
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("map_picker") },
                    enabled = false,
                    readOnly = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (location.isNotBlank()) Color(0xFFDDF5E3) else Color.White,
                        unfocusedContainerColor = if (location.isNotBlank()) Color(0xFFDDF5E3) else Color.White,
                        disabledContainerColor = if (location.isNotBlank()) Color(0xFFDDF5E3) else Color.White,
                        focusedTextColor = Color(0xFF2B2B2B),
                        unfocusedTextColor = Color(0xFF2B2B2B),
                        disabledTextColor = Color(0xFF2B2B2B),
                        cursorColor = Color(0xFF3C9D6D),
                        focusedBorderColor = Color(0xFF3C9D6D),
                        unfocusedBorderColor = Color(0xFFBFAE94),
                        disabledBorderColor = Color(0xFFBFAE94),
                        focusedLabelColor = Color(0xFF3C9D6D),
                        unfocusedLabelColor = Color(0xFF6B4F3B),
                        disabledLabelColor = Color(0xFF6B4F3B)
                    )
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                repo.update(
                                    id = publicationId,
                                    imageUrl = if (imageUrl.isBlank()) "https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885__480.jpg" else imageUrl,
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
                        .height(52.dp)
                        .clip(MaterialTheme.shapes.large),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C9D6D))
                ) {
                    if (isSaving)
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    else
                        Text(
                            "Guardar cambios",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                }
            }
        }
    }
}