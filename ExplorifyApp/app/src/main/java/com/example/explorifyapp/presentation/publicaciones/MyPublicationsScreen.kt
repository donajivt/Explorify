package com.example.explorifyapp.presentation.publicaciones

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.explorifyapp.data.remote.model.Publication
import com.example.explorifyapp.data.remote.publications.RetrofitPublicationsInstance
import com.example.explorifyapp.domain.repository.PublicationRepositoryImpl
import com.example.explorifyapp.presentation.login.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable //,viewModel: MyPublicationsViewModel = viewModel()
fun MyPublicationsScreen(navController: NavController, loginViewModel: LoginViewModel = viewModel()) {

    var menuExpanded by remember { mutableStateOf(false) }
    //val repo = remember { PublicationsRepository() }
    val repo = remember { PublicationRepositoryImpl(RetrofitPublicationsInstance.api) }
    val factory = remember { MyPublicationsViewModelFactory(repo) }
    val viewModel: MyPublicationsViewModel = viewModel(factory = factory)

    LaunchedEffect(Unit) {
        val isLoggedIn = loginViewModel.isLoggedIn()
        if (!isLoggedIn) {
            navController.navigate("login") {
                popUpTo("main") { inclusive = true }
            }
        } else {
            val userId = loginViewModel.userId
            val token = loginViewModel.token
            if (!token.isNullOrEmpty() && userId.isNotEmpty()) {
                viewModel.loadPublications(userId, token)
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Lista de Aventuras") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("perfil") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
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
                },
            )
        },
    )
    {
            paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when {
                viewModel.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                viewModel.errorMessage != null -> {
                    Text(text = viewModel.errorMessage ?: "Error", color = MaterialTheme.colorScheme.error)
                }
                else -> {
                    LazyColumn {
                        items(viewModel.publications) { pub ->
                            PublicationItem(
                                pub = pub,
                                onDelete = {
                                    val token = loginViewModel.token ?: ""
                                    if (token.isNotEmpty()) {
                                        viewModel.deletePublication(pub.id, token)
                                    }
                                },
                                onEdit = {
                                    val token = loginViewModel.token ?: ""
                                    val userId = loginViewModel.userId
                                    navController.navigate("editar_publicacion/${pub.id}?token=$token&userId=$userId")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun PublicationItem(
    pub: Publication,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Imagen superior
            Image(
                painter = rememberAsyncImagePainter(pub.imageUrl),
                contentDescription = pub.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )

            // Contenido textual
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = pub.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = pub.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = pub.location,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Acciones (editar / eliminar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar publicación",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar publicación",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Eliminar publicación") },
            text = { Text("¿Seguro que deseas eliminar esta publicación? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDialog = false
                    }
                ) {
                    Text(
                        "Eliminar",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPublicationDialog(
    publication: Publication,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf(publication.title) }
    var description by remember { mutableStateOf(publication.description) }
    var location by remember { mutableStateOf(publication.location) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar publicación") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Ubicación") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(title, description, location) }) {
                Text("Guardar", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        icon = {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    )
}


