package com.explorify.explorifyapp.presentation.admin


import com.explorify.explorifyapp.presentation.login.LoginViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.explorify.explorifyapp.presentation.publications.list.PublicationsListModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import com.explorify.explorifyapp.data.remote.publications.RetrofitUsersInstance
import com.explorify.explorifyapp.domain.repository.UserRepositoryImpl
import com.explorify.explorifyapp.presentation.admin.AdminDashboard
import com.explorify.explorifyapp.presentation.admin.listUsers.UserListViewModel
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard( vm: PublicationsListModel,
                    navController: NavController,
                    //onCreateClick: (String) -> Unit,
                    onOpenDetail: (String) -> Unit,
                 viewModel: LoginViewModel = viewModel(),
                    userListVM: UserListViewModel = viewModel()
                    ) {
    var menuExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val state = vm.uiState
    val swipeState = rememberSwipeRefreshState(isRefreshing = state.loading)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val usuarios by userListVM.usuarios.collectAsState()
    //val userMap = usuarios.associate { it.id to it.name }
    val userMap = usuarios.associate { user ->
        user.id to (user.name to user.profileImageUrl)
    }

    // 🧠 Mapa de usuarios (id → nombre)
    //val userRepo = remember { UserRepositoryImpl(RetrofitUsersInstance.api) }
    //var userMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    // 🔹 Obtener token para cargar publicaciones y usuarios
    var token by remember { mutableStateOf<String?>(null) }
    // 🔐 Validar si hay sesión
    LaunchedEffect(Unit) {
        val isLoggedIn = viewModel.isLoggedIn()
        if (!isLoggedIn) {
            navController.navigate("login") {
                popUpTo("adminDashboard") { inclusive = true }
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
                userListVM.getUsers(it)
                // val users = userRepo.getAllUsers(it)
               // userMap = users.associate { u -> u.id to u.name }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    println("🧭 MAP: ${userMap.keys}")
    println("📋 POSTS: ${state.items.map { it.userId }}")

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
                title = { Text("Panel Lista de Aventuras") },
                actions = {
                    // Menú de perfil con logout
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
                                    viewModel.logout {
                                        navController.navigate("login") {
                                            popUpTo("mypublications") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = true,
                    onClick = { navController.navigate("adminDashboard") }
                )
               /* NavigationBarItem(
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Buscar") },
                    label = { Text("Estadisticas") },
                    selected = false,
                    onClick = { } //navController.navigate("buscar")
                )*/
                NavigationBarItem(
                    icon = { Icon(Icons.Default.BorderColor, contentDescription = "Buscar") },
                    label = { Text("Reportes") },
                    selected = false,
                    onClick = { navController.navigate("reportes") } //
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = false,
                    onClick = { navController.navigate("perfilAdmin")}//
                )
            }
        }
    ) { innerPadding ->
        Box(/*modifier = Modifier.padding(innerPadding)*/) {
            SwipeRefresh(
                state = swipeState,
                onRefresh = {
                    scope.launch {
                        if (!token.isNullOrEmpty()) {
                            vm.refresh(token!!)
                            try {
                                userListVM.getUsers(token!!)
                                //val users = userRepo.getAllUsers(token!!)
                                //userMap = users.associate { u -> u.id to u.name }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
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
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.items, key = { it.id }) { pub ->
                                val userData = userMap[pub.userId]
                                val authorName = userData?.first ?: "Usuario desconocido"
                                val authorImage = userData?.second
                                PublicationCard(
                                    publication = pub,
                                    onOpen = { onOpenDetail(pub.id) },
                                    onViewMap = {
                                        val lat = pub.latitud.toString()
                                        val lon = pub.longitud.toString()
                                        val name = Uri.encode(pub.location)
                                        navController.navigate("map/$lat/$lon/$name")
                                    },
                                    authorName = authorName,
                                    authorImage = authorImage
                                    //authorName = userMap[pub.userId] ?: "Usuario desconocido"
                                )
                            }
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
    onViewMap: () -> Unit,
    authorName: String,
    authorImage:String?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .shadow(6.dp, shape = RoundedCornerShape(20.dp), clip = false)
            .background(Color.Transparent)
            .border(
                width = 1.2.dp,
                color = Color(0xFFBFAE94).copy(alpha = 0.8f),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B1C)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
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
                Text(
                    text = publication.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
            }

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!authorImage.isNullOrBlank()) {
                            Log.d("imagen url:"," ${authorImage}")
                            AsyncImage( //imageUrl
                                model = authorImage+ "?t=" + System.currentTimeMillis(),
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .size(40.dp)        // 👈 más pequeño
                                    .clip(CircleShape)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    ),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(40.dp)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    ),
                                tint = Color(0xFF355031)
                            )
                        }
                        /*Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )*/
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text(
                                text = authorName,
                                style = MaterialTheme.typography.labelLarge.copy(color = Color.White)
                            )
                            Text(
                                text = publication.createdAt.formatAsDate(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

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

private fun String.formatAsDate(): String = try {
    val odt = OffsetDateTime.parse(this)
    odt.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
} catch (_: Exception) {
    this
}
