package com.example.explorifyapp.presentation.publications.list.screens

import androidx.compose.foundation.background
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
import coil.compose.AsyncImage
import com.example.explorifyapp.data.remote.room.AppDatabase
import com.example.explorifyapp.presentation.publications.list.CreatePublicationViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePublicationScreen(
    vm: CreatePublicationViewModel,
    onBack: () -> Unit,
    onPublishDone: () -> Unit,
    userId: String
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 🔹 Cargar el token y el userId desde Room
    var token by remember { mutableStateOf<String?>(null) }
    var userId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(context).authTokenDao()
            token = dao.getToken()?.token
            userId = dao.getToken()?.userId
        }
    }

    // 🔹 Estados locales para el formulario
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var imageUrl by remember {
        mutableStateOf("https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885__480.jpg")
    }

    val ui = vm.uiState
    var isPublishing by remember { mutableStateOf(false) }

    // 🔹 Mostrar mensajes de error
    LaunchedEffect(ui.error) {
        ui.error?.let {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(it)
        }
    }

    // 🔹 Mostrar éxito
    LaunchedEffect(ui.success) {
        if (ui.success) {
            isPublishing = false
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar("Publicación creada correctamente")

            title = ""
            description = ""
            location = ""
            kotlinx.coroutines.delay(1500)

            onPublishDone()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Nueva Publicación", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
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

                    if (description.isBlank() || location.isBlank()) {
                        scope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            snackbarHostState.showSnackbar("Debes llenar la descripción y ubicación")
                        }
                        return@Button
                    }

                    // ⚠️ Asegúrate de tener userId
                    if (userId.isNullOrEmpty()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Error: No se encontró el usuario autenticado")
                        }
                        return@Button
                    }

                    isPublishing = true

                    vm.createPublication(
                        context = context,
                        imageUrl = imageUrl,
                        title = title,
                        description = description,
                        location = location,
                        userId = userId!!,
                        onDone = { }
                    )
                },
                enabled = !ui.loading && !isPublishing,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF355E3B))
            ) {
                if (vm.uiState.loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Publicar", color = Color.White)
                }
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

            // Imagen principal (simple)
            Box(Modifier.size(120.dp)) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                )
                IconButton(
                    onClick = { /* TODO: abrir selector de imágenes */ },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(48.dp)
                        .background(Color(0xFF355E3B), CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar imagen", tint = Color.White)
                }
            }

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE8F0E5),
                    unfocusedContainerColor = Color(0xFFE8F0E5)
                )
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Escribe una descripción") },
                modifier = Modifier.fillMaxWidth(),
                isError = description.isBlank(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE8F0E5),
                    unfocusedContainerColor = Color(0xFFE8F0E5)
                )
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Añade una ubicación") },
                leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                isError = location.isBlank(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE8F0E5),
                    unfocusedContainerColor = Color(0xFFE8F0E5)
                )
            )
        }
    }
}
