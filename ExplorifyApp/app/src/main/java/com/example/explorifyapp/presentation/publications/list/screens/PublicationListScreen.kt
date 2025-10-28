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
                                text = { Text("Mis Publicaciones") },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate("mispublicaciones")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Cerrar sesión") },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.logout {
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
                                onOpen = { onOpenDetail(pub.id) }
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
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
    ) {
        Column(Modifier.padding(14.dp)) {

            // Encabezado: usuario y fecha
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(Color(0xFFE0E0E0))
                )
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = publication.userId.ifEmpty { "Usuario desconocido" },
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = publication.createdAt.formatAsDate(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Imagen principal
            AsyncImage(
                model = publication.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(Color.LightGray)
            )

            Spacer(Modifier.height(10.dp))
            Text(publication.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                publication.description,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(6.dp))
                Text(publication.location, style = MaterialTheme.typography.labelMedium)
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
