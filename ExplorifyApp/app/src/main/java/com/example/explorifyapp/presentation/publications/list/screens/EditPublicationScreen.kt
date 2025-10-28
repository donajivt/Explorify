package com.example.explorifyapp.presentation.publications.list.screens

import androidx.compose.foundation.Image
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
import com.example.explorifyapp.data.remote.model.Publication
import com.example.explorifyapp.data.remote.publications.RetrofitPublicationsInstance
import com.example.explorifyapp.domain.repository.PublicationRepositoryImpl
import com.example.explorifyapp.presentation.login.LoginViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
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

    var snackbarJob by remember { mutableStateOf<Job?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    LaunchedEffect(loginViewModel.token, publicationId) {
        println("🔑 Token recibido: $token")
        println("🆔 ID publicación: $publicationId")
        if (!token.isNullOrEmpty() && userId.isNotEmpty()) {
            isLoading = true
            try {
                val publications = repo.getUserPublications(userId, token)
                val pub = publications.find { it.id == publicationId }

                if (pub != null) {
                    println("✅ Publicación encontrada entre las del usuario: ${pub.title}")
                    title = pub.title
                    description = pub.description
                    location = pub.location
                    imageUrl = pub.imageUrl
                } else {
                    println("⚠️ No se encontró la publicación entre las del usuario.")
                    coroutineScope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar("No se encontró la publicación seleccionada")
                        delay(1000)
                        snackbarHostState.currentSnackbarData?.dismiss()
                    }
                }

            } catch (e: Exception) {
                println("⚠️ Error al cargar publicación: ${e.message}")
                coroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar("Error al cargar la publicación")
                    delay(1000)
                    snackbarHostState.currentSnackbarData?.dismiss()
                }
            } finally {
                isLoading = false
            }
        }else {
            println("🚫 Token o userId vacío, no se puede cargar la publicación.")
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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Actualiza los datos de tu aventura",
                    style = MaterialTheme.typography.titleMedium
                )

                // 🖼️ Vista previa de imagen actual
                if (imageUrl.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = "Vista previa",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Ubicación") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("URL de imagen") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (isSaving) return@Button

                        // Validar campos
                        if (description.isBlank() || location.isBlank() || imageUrl.isBlank()) {
                            snackbarJob?.cancel()
                            snackbarJob = coroutineScope.launch {
                                snackbarHostState.showSnackbar("Todos los campos (excepto título) son obligatorios")
                                delay(1000)
                                snackbarHostState.currentSnackbarData?.dismiss()
                            }
                            return@Button
                        }

                        coroutineScope.launch {
                            isSaving = true
                            try {

                                repo.update(
                                    id = publicationId,
                                    imageUrl = imageUrl,
                                    title = title,
                                    description = description,
                                    location = location,
                                    userId = userId,
                                    token = token
                                )

                                snackbarJob?.cancel()
                                snackbarJob = coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Publicación actualizada correctamente")
                                    delay(1000)
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                }

                                delay(800)
                                navController.popBackStack()

                            } catch (e: Exception) {
                                snackbarJob?.cancel()
                                snackbarJob = coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Error al actualizar publicación")
                                    delay(1000)
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                }
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Guardar cambios")
                    }
                }
            }
        }
    }
}