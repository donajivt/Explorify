package com.example.explorifyapp.presentation.publications.list.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.explorifyapp.data.remote.model.Publication
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.explorifyapp.data.remote.room.AppDatabase
import com.example.explorifyapp.presentation.login.LoginViewModel
import com.example.explorifyapp.presentation.publications.list.PublicationsListModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicationListScreen(
    vm: PublicationsListModel,
    navController: NavController,
    onCreateClick: (String) -> Unit,
    onOpenDetail: (String) -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val state = vm.uiState
    val swipeState = rememberSwipeRefreshState(isRefreshing = state.loading)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val isLoggedIn = viewModel.isLoggedIn()
        if (!isLoggedIn) {
            navController.navigate("login") {
                popUpTo("publicaciones") { inclusive = true }
            }
        }
    }

    // 🔹 Carga inicial de publicaciones con token desde Room
    LaunchedEffect(Unit) {
        val token: String? = withContext(Dispatchers.IO) {
            AppDatabase.getInstance(context).authTokenDao().getToken()?.token
        }
        if (!token.isNullOrEmpty()) {
            vm.load(token)
        }
    }

    // 🔹 Manejo de errores con Snackbar
    LaunchedEffect(state.error) {
        state.error?.let { msg ->
            val readable = when {
                msg.contains("Unable to resolve host", true) ||
                        msg.contains("Failed to connect", true) ||
                        msg.contains("timeout", true) -> "Sin conexión a internet. Verifica tu red."
                else -> "Error: $msg"
            }
            scope.launch {
                snackbarHostState.showSnackbar(readable, withDismissAction = true)
            }
        }
    }

    // 🔹 Obtener userId del Room (de forma segura)
    val userId by produceState<String?>(initialValue = null) {
        val id: String? = withContext(Dispatchers.IO) {
            AppDatabase.getInstance(context).authTokenDao().getToken()?.userId
        }
        value = id
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de Aventuras") },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = true,
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
                    selected = false,
                    onClick = {navController.navigate("perfil")}
                )
            }
        },
        floatingActionButton = {

            FloatingActionButton(
                onClick = {
                    if (userId != null)
                        onCreateClick(userId!!)
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva publicación")
            }
        }
    ) { padding ->

        SwipeRefresh(
            state = swipeState,
            onRefresh = {
                scope.launch {
                    val token: String? = withContext(Dispatchers.IO) {
                        AppDatabase.getInstance(context).authTokenDao().getToken()?.token
                    }
                    if (!token.isNullOrEmpty()) {
                        vm.refresh(token)
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                // Estado cargando sin datos
                state.loading && state.items.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                // Error sin datos
                state.error != null && state.items.isEmpty() -> {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = {
                            scope.launch {
                                val token: String? = withContext(Dispatchers.IO) {
                                    AppDatabase.getInstance(context).authTokenDao().getToken()?.token
                                }
                                if (!token.isNullOrEmpty()) {
                                    vm.refresh(token)
                                }
                            }
                        }) {
                            Text("Reintentar")
                        }
                    }
                }

                // Lista de publicaciones
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.items, key = { it.id }) { pub ->
                            PublicationCard(
                                publication = pub,
                                onOpen = { onOpenDetail(pub.id) },
                                onViewMap = {
                                    val lat = pub.latitud.toString()
                                    val lon = pub.longitud.toString()
                                    val name = Uri.encode(pub.location)
                                    navController.navigate("map/$lat/$lon/$name")
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
private fun PublicationCard(
    publication: Publication,
    onOpen: () -> Unit,
    onViewMap: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .shadow(6.dp, shape = RoundedCornerShape(20.dp),clip = false)
                .background(Color.Transparent)
                .border(
                    width = 1.2.dp,
                    color = Color(0xFFBFAE94).copy(alpha = 0.8f), // 💡 Borde arena claro con transparencia
                    shape = RoundedCornerShape(20.dp)
                ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B1C)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            // Imagen principal con overlay de título
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                AsyncImage(
                    model = publication.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )

                // Gradiente oscuro inferior
                Box(
                    Modifier
                        .matchParentSize()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.55f)
                                ),
                                startY = 150f
                            )
                        )
                )

                // Título sobre la imagen
                Text(
                    text = publication.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
            }

            // Contenido textual
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = publication.description,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Usuario + fecha
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text(
                                text = publication.userId.ifEmpty { "Usuario desconocido" },
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = publication.createdAt.formatAsDate(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    // Ubicación con botón
                    TextButton(onClick = onViewMap) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription = "Ver en mapa",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Ver ubicación",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

/** Utilidad para formatear fecha ISO → dd/MM/yyyy */
private fun String.formatAsDate(): String = try {
    val odt = OffsetDateTime.parse(this)
    odt.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
} catch (_: Exception) {
    this
}
