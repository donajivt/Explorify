package com.explorify.explorifyapp.presentation.publications.list.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.explorify.explorifyapp.data.remote.model.Publication
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.explorify.explorifyapp.data.remote.room.AppDatabase
import com.explorify.explorifyapp.presentation.login.LoginViewModel
import com.explorify.explorifyapp.presentation.publications.list.PublicationsListModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import com.explorify.explorifyapp.data.remote.publications.RetrofitUsersInstance
import com.explorify.explorifyapp.domain.repository.UserRepositoryImpl

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

    // 🧠 Mapa de usuarios (id → nombre)
    val userRepo = remember { UserRepositoryImpl(RetrofitUsersInstance.api) }
    var userMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    // 🔹 Obtener token para cargar publicaciones y usuarios
    var token by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val isLoggedIn = viewModel.isLoggedIn()
        if (!isLoggedIn) {
            navController.navigate("login") {
                popUpTo("publicaciones") { inclusive = true }
            }
        }
    }

    // 🔹 Carga inicial de publicaciones y usuarios
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(context).authTokenDao()
            token = dao.getToken()?.token
        }
        token?.let {
            vm.load(it)
            try {
                val users = userRepo.getAllUsers(it)
                userMap = users.associate { u -> u.id to u.name }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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

    // 🔹 Obtener userId del Room (para crear)
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
                    onClick = { navController.navigate("perfil") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (userId != null)
                        onCreateClick(userId!!)
                },
                containerColor = Color(0xFF355E3B)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva publicación", tint = Color.White)
            }
        }
    ) { padding ->
        SwipeRefresh(
            state = swipeState,
            onRefresh = {
                scope.launch {
                    if (!token.isNullOrEmpty()) {
                        vm.refresh(token!!)
                        try {
                            val users = userRepo.getAllUsers(token!!)
                            userMap = users.associate { u -> u.id to u.name }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.loading && state.items.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
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
                                if (!token.isNullOrEmpty()) {
                                    vm.refresh(token!!)
                                }
                            }
                        }) {
                            Text("Reintentar")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                },
                                onViewProfile = {
                                    navController.navigate("perfilUsuario/${pub.userId}")
                                },
                                // ✅ Aquí la navegación hacia comentarios:
                                onViewComments = {
                                    println("🟡 Navegando a comentarios/${pub.id}")
                                    navController.navigate("comentarios/${pub.id}")
                                },
                                authorName = userMap[pub.userId] ?: "Usuario desconocido"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PublicationCard(
    publication: Publication,
    onOpen: () -> Unit,
    onViewMap: () -> Unit,
    onViewProfile: () -> Unit,
    onViewComments: () -> Unit,
    authorName: String
) {
    val isDark = isSystemInDarkTheme()

    val backgroundColor = if (isDark) Color(0xFF2B2F2D) else Color(0xFFF7F8F5)
    val textPrimary = if (isDark) Color.White else Color(0xFF1A1A1A)
    val textSecondary = if (isDark) Color(0xFFDADADA) else Color(0xFF4F4F4F)
    val locationBg = if (isDark) Color(0xFF374038) else Color(0xFFE8F5E9)
    val locationText = if (isDark) Color(0xFFC8FACC) else Color(0xFF1B5E20)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape = RoundedCornerShape(20.dp), clip = false),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // 👤 Header con autor
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onViewProfile() }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(color = Color(0xFF3C9D6D), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = authorName,
                        color = textPrimary,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = publication.createdAt.formatAsDate(),
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                }

                // Botón más opciones (futuro menú)
                IconButton(onClick = { /* TODO: Menú */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Más opciones",
                        tint = textSecondary
                    )
                }
            }

            // 🖼️ Imagen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clickable { onOpen() }
            ) {
                AsyncImage(
                    model = publication.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )

                // Sombra suave inferior
                Box(
                    Modifier
                        .matchParentSize()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                            )
                        )
                )

                Text(
                    text = publication.title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
            }

            // 📝 Descripción
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = publication.description,
                    color = textPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4f,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(16.dp))

                // 📍 Ubicación
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(locationBg)
                        .clickable { onViewMap() }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF80E89B) else Color(0xFF388E3C),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = publication.location,
                        style = MaterialTheme.typography.bodyMedium.copy(color = locationText),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF9E9E9E) else Color(0xFF4E4E4E),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.height(14.dp))

                TextButton(onClick = onViewComments) {
                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = Color(0xFF3C9D6D))
                    Spacer(Modifier.width(6.dp))
                    Text("Comentarios", color = Color(0xFF3C9D6D))
                }
            }
        }
    }
}

private fun String.formatAsDate(): String = try {
    val odt = OffsetDateTime.parse(this)
    odt.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
} catch (_: Exception) {
    this
}