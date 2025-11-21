package com.explorify.explorifyapp.presentation.publicaciones

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.explorify.explorifyapp.data.remote.model.Publication
import com.explorify.explorifyapp.data.remote.publications.RetrofitPublicationsInstance
import com.explorify.explorifyapp.domain.repository.PublicationRepositoryImpl
import com.explorify.explorifyapp.presentation.login.LoginViewModel

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
                title = { Text("Mis Aventuras", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("perfil") }) {
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
                    selected = false,
                    onClick = { navController.navigate("buscar") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = true,
                    onClick = {}
                )
            }
        }
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
                                navController = navController,
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
    navController: NavController,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFDFDFD)
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {

        Column {

            // :::::::::::::::::::: IMAGEN :::::::::::::::::::::::
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
            ) {
                Image(
                    painter = rememberAsyncImagePainter(pub.imageUrl),
                    contentDescription = pub.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Difuminado inferior
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.65f)
                                )
                            )
                        )
                )

                // Etiqueta "Mi Publicación"
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .background(
                            Color(0xFF1E88E5).copy(alpha = 0.85f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "Mi publicación",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                // Toolbar de acciones
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFF2196F3).copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Edit, null, tint = Color.White)
                    }

                    Spacer(Modifier.width(8.dp))

                    IconButton(
                        onClick = { showDialog = true },
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFFE53935).copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Delete, null, tint = Color.White)
                    }
                }
            }

            // :::::::::::::::::::: CUERPO :::::::::::::::::::::::
            Column(modifier = Modifier.padding(18.dp)) {

                Text(
                    text = pub.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF212121)
                    )
                )

                Spacer(Modifier.height(6.dp))

                // Ver más / ver menos
                var expanded by remember { mutableStateOf(false) }
                var overflowingDesc by remember { mutableStateOf(false) }

                Text(
                    text = pub.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF4A4A4A)
                    ),
                    maxLines = if (expanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { layout ->
                        if (layout.hasVisualOverflow) overflowingDesc = true
                    }
                )

                if (overflowingDesc) {
                    Text(
                        text = if (expanded) "Ver menos ▲" else "Ver más ▼",
                        color = Color(0xFF1E88E5),
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable { expanded = !expanded }
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "📍 ${pub.location}",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = Color(0xFF2E7D32)
                    )
                )

                Spacer(Modifier.height(12.dp))

                // Ver comentarios
                Text(
                    text = "💬  Ver comentarios",
                    color = Color(0xFF1E88E5),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .clickable {
                            navController.navigate("comentarios/${pub.id}")
                        }
                )
            }
        }
    }

    // :::::::::::::::::::: DIALOGO ELIMINAR :::::::::::::::::::::::
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Eliminar publicación") },
            text = { Text("¿Seguro que deseas eliminar esta publicación?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDialog = false
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}


