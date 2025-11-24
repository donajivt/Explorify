package com.explorify.explorifyapp.presentation.publications.list.screens

import android.content.pm.PackageManager
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
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import com.explorify.explorifyapp.data.remote.publications.RetrofitUsersInstance
import com.explorify.explorifyapp.domain.repository.UserRepositoryImpl
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.res.painterResource
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.async
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.runtime.LaunchedEffect
import android.os.Build
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.explorify.explorifyapp.data.remote.model.User
import com.explorify.explorifyapp.data.remote.publications.RetrofitComentariosInstance
import android.Manifest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicationListScreen(
    vm: PublicationsListModel,
    navController: NavController,
    onCreateClick: (String) -> Unit,
    onOpenDetail: (String) -> Unit,
    viewModel: LoginViewModel = viewModel()
) {

    LaunchedEffect(Unit) {
        Log.e("RUM_DEBUG", ">>> startView PUBLICATION_LIST")
        com.datadog.android.rum.GlobalRumMonitor.get().startView(
            "publication_list",
            "PantallaPublicaciones"
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.e("RUM_DEBUG", ">>> stopView PUBLICATION_LIST")
            com.datadog.android.rum.GlobalRumMonitor.get().stopView("publication_list")
        }
    }


    val context = LocalContext.current
    val state = vm.uiState
    val swipeState = rememberSwipeRefreshState(isRefreshing = state.loading)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var menuExpanded by remember { mutableStateOf(false) }

    // 🧠 Mapa de usuarios (id → nombre)
    val userRepo = remember { UserRepositoryImpl(RetrofitUsersInstance.api) }
    var userMap by remember { mutableStateOf<Map<String, User>>(emptyMap()) }
    val listState = rememberLazyListState()

    // 🔹 Obtener token para cargar publicaciones y usuarios
    var token by remember { mutableStateOf<String?>(null) }

    // 🔥 Lanzador para solicitar permiso POST_NOTIFICATIONS
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Puedes imprimir en log
        Log.d("NOTIF_DEBUG", "Resultado del permiso → isGranted = $isGranted")
        println("NOTIFICATION PERMISSION GRANTED? $isGranted")
    }

// 🔥 Lógica para pedir el permiso una sola vez
    LaunchedEffect(Unit) {
        Log.d("NOTIF_DEBUG", "Entró a LaunchedEffect para pedir permiso")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            Log.d("NOTIF_DEBUG", "Versión Android >= 13, revisando permiso")

            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )

            Log.d("NOTIF_DEBUG", "Estado del permiso: $permissionCheck")

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                Log.d("NOTIF_DEBUG", "Permiso NO otorgado → lanzando ventana")
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Log.d("NOTIF_DEBUG", "Permiso YA otorgado → no se lanzará nada")
            }
        } else {
            Log.d("NOTIF_DEBUG", "Android < 13 → NO requiere permiso de notificaciones.")
        }
    }

    LaunchedEffect(Unit) {
        val isLoggedIn = viewModel.isLoggedIn()
        if (!isLoggedIn) {
            navController.navigate("login") {
                popUpTo("publicaciones") { inclusive = true }
            }
        }
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(context).authTokenDao()
            token = dao.getToken()?.token
        }

        token?.let { tk ->
            try {
                // 🚀 Cargar en paralelo para evitar “Usuario desconocido”
                val usersDeferred = async(Dispatchers.IO) { userRepo.getAllUsers(tk) }
                val pubsDeferred = async(Dispatchers.IO) { vm.load(tk) }
                val usuarios=userRepo.getAllUsers(tk)
                Log.d("datos de usuarios: ","${usuarios}")
                val users = usersDeferred.await()
                pubsDeferred.await()

                // 🔁 Actualiza el mapa de usuarios en Compose
                userMap = users.associateBy { it.id }

                println("✅ Usuarios cargados: ${userMap.keys}")
            } catch (e: Exception) {
                e.printStackTrace()
                scope.launch {
                    snackbarHostState.showSnackbar("Error al cargar usuarios o publicaciones.")
                }
            }
        } ?: run {
            scope.launch {
                snackbarHostState.showSnackbar("Token no encontrado. Inicia sesión nuevamente.")
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
                            userMap = users.associateBy { it.id }
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
                        state = listState,
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
                                    if (pub.userId == userId)
                                        navController.navigate("mispublicaciones")
                                    else
                                        navController.navigate("perfilUsuario/${pub.userId}")
                                },
                                onViewComments = {
                                    navController.navigate("comentarios/${pub.id}")
                                },
                                user = userMap[pub.userId],
                                navController = navController
                            )
                        }
                    }

                    // 🚀 Monitorear el final del scroll (solo para mostrar feedback visual)
                    LaunchedEffect(listState) {
                        snapshotFlow {
                            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                            val total = listState.layoutInfo.totalItemsCount
                            lastVisible to total
                        }.distinctUntilChanged()
                            .collect { (lastVisible, total) ->
                                if (lastVisible != null && total > 0 && lastVisible >= total - 3) {
                                    // No carga más, solo da retroalimentación visual
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Llegaste al final 🏁")
                                    }
                                }
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
    user: User?,
    navController: NavController
) {
    val isDark = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()

    val backgroundColor = if (isDark) Color(0xFF2B2F2D) else Color(0xFFF7F8F5)
    val textPrimary = if (isDark) Color.White else Color(0xFF1A1A1A)
    val textSecondary = if (isDark) Color(0xFFDADADA) else Color(0xFF4F4F4F)
    val locationBg = if (isDark) Color(0xFF374038) else Color(0xFFE8F5E9)
    val locationText = if (isDark) Color(0xFFC8FACC) else Color(0xFF1B5E20)
    var showFullScreen by remember { mutableStateOf(false) }
    var imageAspectRatio by remember { mutableStateOf(1f) }

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
                if (user?.profileImageUrl.isNullOrEmpty()) {

                    // Si NO tiene foto → icono default
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF3C9D6D), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                } else {

                    // Si SÍ tiene foto → mostrar AsyncImage
                    AsyncImage(
                        model = user.profileImageUrl,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                            text = user?.name ?: "Usuario desconocido",
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
                    .clickable { showFullScreen = true } // 👈 Al tocar se abre el visor
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(publication.imageUrl)
                        .crossfade(true)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = if (imageAspectRatio < 1f) ContentScale.Crop else ContentScale.Crop,
                    onSuccess = { success ->
                        success.painter.intrinsicSize.let {
                            if (it.width > 0 && it.height > 0) {
                                imageAspectRatio = it.width / it.height
                            }
                        }
                    },
                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                    error = painterResource(id = android.R.drawable.ic_delete)
                )

                // 🌒 Sombra inferior con título encima de la imagen
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

            // 🪟 Dialog para vista completa con zoom estilo Facebook
            // 🪟 Dialog para vista completa con zoom controlado (como Facebook)
            if (showFullScreen) {
                Dialog(
                    onDismissRequest = { showFullScreen = false },
                    properties = androidx.compose.ui.window.DialogProperties(
                        usePlatformDefaultWidth = false // 🔥 Pantalla completa real
                    )
                ) {
                    var scale by remember { mutableStateOf(1f) }
                    var offsetX by remember { mutableStateOf(0f) }
                    var offsetY by remember { mutableStateOf(0f) }

                    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
                        val newScale = (scale * zoomChange).coerceIn(1f, 4f)

                        // Calcula límites del movimiento
                        val maxOffsetX = (newScale - 1f) * 400f
                        val maxOffsetY = (newScale - 1f) * 600f

                        offsetX = (offsetX + panChange.x).coerceIn(-maxOffsetX, maxOffsetX)
                        offsetY = (offsetY + panChange.y).coerceIn(-maxOffsetY, maxOffsetY)
                        scale = newScale
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                            .transformable(transformState),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(publication.imageUrl)
                                .crossfade(true)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offsetX,
                                    translationY = offsetY
                                )
                                .fillMaxSize()
                        )

                        // ❌ Botón de cerrar
                        IconButton(
                            onClick = { showFullScreen = false },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .size(40.dp)
                                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar imagen",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // 📝 Descripción
            Column(
                Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .animateContentSize()
            ) {

                var expanded by remember { mutableStateOf(false) }
                var overflowingDesc by remember { mutableStateOf(false) }

                Text(
                    text = publication.description,
                    color = textPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4f,
                    maxLines = if (expanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { layout ->
                        if (!expanded) {
                            // Solo medir overflow mientras está colapsado
                            overflowingDesc = layout.hasVisualOverflow
                        }
                    }
                )

                if (overflowingDesc) {
                    Text(
                        text = if (expanded) "Ver menos ▲" else "Ver más ▼",
                        color = Color(0xFF3C9D6D),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clickable { expanded = !expanded }
                    )
                }
            }

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

            var commentsCount by remember(publication.id) { mutableStateOf<Int?>(null) }
            val context = LocalContext.current

            LaunchedEffect(publication.id) {
                val tk = AppDatabase.getInstance(context).authTokenDao().getToken()?.token

                if (!tk.isNullOrEmpty()) {
                    try {
                        val response = RetrofitComentariosInstance.api.getCount(
                            publicacionId = publication.id,
                            token = "Bearer $tk"
                        )

                        if (response.isSuccessful) {
                            commentsCount = response.body()?.count ?: 0
                        } else {
                            commentsCount = 0
                        }

                    } catch (e: Exception) {
                        commentsCount = 0
                    }
                }
            }

            val lifecycleOwner = LocalLifecycleOwner.current

            LaunchedEffect(true) {
                val navEntry = navController.currentBackStackEntry

                navEntry?.savedStateHandle
                    ?.getLiveData<Boolean>("refreshComments")
                    ?.observe(lifecycleOwner) { refresh ->
                        if (refresh == true) {
                            scope.launch {
                                val tk = withContext(Dispatchers.IO) {
                                    AppDatabase.getInstance(context).authTokenDao().getToken()?.token
                                }

                                if (!tk.isNullOrEmpty()) {
                                    try {
                                        val resp = RetrofitComentariosInstance.api.getCount(
                                            publicacionId = publication.id,
                                            token = "Bearer $tk"
                                        )

                                        if (resp.isSuccessful) {
                                            commentsCount = resp.body()?.count ?: commentsCount
                                        }

                                    } catch (_: Exception) {}
                                }
                            }

                            navEntry.savedStateHandle["refreshComments"] = false
                        }
                    }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp)
                    .clickable { onViewComments() }
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.ChatBubbleOutline,
                        contentDescription = null,
                        tint = Color(0xFF3C9D6D),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Comentarios",
                        color = Color(0xFF3C9D6D),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    )
                }

                Text(
                    text = commentsCount?.toString() ?: "...",
                    color = Color(0xFF3C9D6D),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
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