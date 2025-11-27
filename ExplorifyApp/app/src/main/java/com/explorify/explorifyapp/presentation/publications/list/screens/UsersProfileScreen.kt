package com.explorify.explorifyapp.presentation.publications.list.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.explorify.explorifyapp.data.remote.model.Publication
import com.explorify.explorifyapp.data.remote.publications.RetrofitUsersInstance
import com.explorify.explorifyapp.data.remote.room.AppDatabase
import com.explorify.explorifyapp.domain.repository.PublicationRepositoryImpl
import com.explorify.explorifyapp.domain.repository.UserRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.explorify.explorifyapp.data.remote.model.User
import java.time.OffsetDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersProfileScreen(
    navController: NavController,
    userId: String
) {
    val context = LocalContext.current
    val userRepo = remember { UserRepositoryImpl(RetrofitUsersInstance.api) }
    val publicationRepo = remember {
        PublicationRepositoryImpl(
            com.explorify.explorifyapp.data.remote.publications.RetrofitPublicationsInstance.api
        )
    }

    var token by remember { mutableStateOf<String?>(null) }
    var userName by remember { mutableStateOf<String?>(null) }
    var userPhoto by remember { mutableStateOf<String?>(null) }
    var userData by remember { mutableStateOf<User?>(null) }
    var showPhotoFullScreen by remember { mutableStateOf(false) }
    var publications by remember { mutableStateOf<List<Publication>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    // 🧩 Obtener token guardado
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(context).authTokenDao()
            token = dao.getToken()?.token
        }
        delay(200)
        token?.let { tk ->
            try {
                userData = userRepo.getAllUsers(tk).find { it.id == userId }

                userName = userData?.name ?: "Usuario desconocido"
                userPhoto = userData?.profileImageUrl
            } catch (e: Exception) {
                userName = "Usuario desconocido"
            }

            try {
                val pubs = publicationRepo.getUserPublications(userId, tk)

                publications = pubs.sortedByDescending { pub ->
                    try {
                        OffsetDateTime.parse(pub.createdAt).toEpochSecond()
                    } catch (_: Exception) {
                        0L
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(userName ?: "Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // --- HEADER PERFIL ---
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(
                                        color = Color(0xFF3C9D6D),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (userPhoto != null) {
                                    AsyncImage(
                                        model = userPhoto,
                                        contentDescription = "Foto de perfil",
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(CircleShape)
                                            .clickable { showPhotoFullScreen = true }
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(80.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = userName ?: "Usuario desconocido",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )

                            Spacer(Modifier.height(4.dp))
                            Divider(
                                color = Color(0xFF3C9D6D),
                                modifier = Modifier
                                    .padding(horizontal = 80.dp, vertical = 8.dp)
                                    .height(2.dp)
                            )
                        }
                    }

                    // --- PUBLICACIONES DEL USUARIO ---
                    if (publications.isEmpty()) {
                        item {
                            Text(
                                text = "Este usuario aún no ha publicado aventuras.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 60.dp)
                            )
                        }
                    } else {
                        items(publications, key = { it.id }) { pub ->
                            PublicationCard(
                                publication = pub,
                                onOpen = { /* Navegar a detalle si se desea */ },
                                onViewMap = {
                                    val lat = pub.latitud.toString()
                                    val lon = pub.longitud.toString()
                                    val name = Uri.encode(pub.location)
                                    navController.navigate("map/$lat/$lon/$name")
                                },
                                onViewProfile = { /* ya estamos en perfil */ },
                                onViewComments = {
                                    navController.navigate("comentarios/${pub.id}")
                                },
                                user = userData,
                                navController = navController,
                                commentsMap = emptyMap()
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
                // 🔥 Visor a pantalla completa con zoom de foto de perfil
                if (showPhotoFullScreen && !userPhoto.isNullOrEmpty()) {
                    Dialog(
                        onDismissRequest = { showPhotoFullScreen = false },
                        properties = DialogProperties(usePlatformDefaultWidth = false)
                    ) {
                        var scale by remember { mutableStateOf(1f) }
                        var offsetX by remember { mutableStateOf(0f) }
                        var offsetY by remember { mutableStateOf(0f) }

                        val transformState = rememberTransformableState { zoomChange, panChange, _ ->
                            val newScale = (scale * zoomChange).coerceIn(1f, 4f)
                            val maxOffset = (newScale - 1f) * 350f

                            offsetX = (offsetX + panChange.x).coerceIn(-maxOffset, maxOffset)
                            offsetY = (offsetY + panChange.y).coerceIn(-maxOffset, maxOffset)
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
                                model = userPhoto,
                                contentDescription = "Foto perfil grande",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offsetX,
                                        translationY = offsetY
                                    ),
                                contentScale = ContentScale.Fit
                            )

                            IconButton(
                                onClick = { showPhotoFullScreen = false },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp)
                                    .size(40.dp)
                                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
