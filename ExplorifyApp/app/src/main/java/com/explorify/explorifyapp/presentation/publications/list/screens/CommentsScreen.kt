package com.explorify.explorifyapp.presentation.publications.list.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import com.explorify.explorifyapp.presentation.publications.list.CommentsViewModel
import com.explorify.explorifyapp.data.remote.room.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.explorify.explorifyapp.data.remote.model.User
import com.explorify.explorifyapp.messaging.RetrofitNotificationInstance
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    navController: NavController,
    publicacionId: String,
    ownerId: String,
    viewModel: CommentsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {

    Log.e("DEBUG_NOTIF", "OWNER_ID = $ownerId")
    val uiState = viewModel.uiState
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var token by remember { mutableStateOf<String?>(null) }
    var myUserId by remember { mutableStateOf<String?>(null) }
    var newComment by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var confirmDeleteId by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current
    var commentsCount by remember { mutableStateOf(0) }

    // Cache estable de IDs temporales (evita claves duplicadas)
    val keyCache = remember { mutableStateMapOf<String, String>() }
    // Mapa de usuarios
    val userRepo = remember {
        com.explorify.explorifyapp.domain.repository.UserRepositoryImpl(
            com.explorify.explorifyapp.data.remote.publications.RetrofitUsersInstance.api
        )
    }
    var userMap by remember { mutableStateOf<Map<String, User>>(emptyMap()) }

    // Cargar token e info de usuario actual
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(context).authTokenDao()
            val tokenData = dao.getToken()
            token = tokenData?.token
            myUserId = tokenData?.userId
        }
        token?.let { tk ->
            viewModel.load(publicacionId, tk)
            commentsCount = viewModel.uiState.comentarios.size
            try {
                val users = withContext(Dispatchers.IO) { userRepo.getAllUsers(tk) }
                userMap = users.associateBy { u -> u.id }
            } catch (e: Exception) {
                println("⚠️ Error al obtener usuarios: ${e.message}")
            }
        }
    }

    LaunchedEffect(uiState.comentarios) {
        commentsCount = uiState.comentarios.size
    }

    // Simular "tiempo real": refrescar lista cada 5 segundos
    LaunchedEffect(token) {
        token?.let { tk ->
            while (true) {
                delay(5000)
                viewModel.load(publicacionId, tk)
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.ime)
            .navigationBarsPadding()
            .clickable(
                // 👇 al tocar fuera, cierra teclado
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { focusManager.clearFocus() },
        topBar = {
            TopAppBar(
                title = { Text("Comentarios ($commentsCount)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 4.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 8.dp, top = 14.dp), // 🔥 aquí
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = newComment,
                        onValueChange = {
                            val clean = sanitizeSafeInputComments(it)
                            if (clean.length <= 500) newComment = clean
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 40.dp, max = 80.dp) // 🔥 más pequeño
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF1F1F1))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        decorationBox = { innerTextField ->
                            Box(
                                Modifier
                                    .fillMaxWidth()
                            ) {
                                if (newComment.isEmpty()) {
                                    Text("Escribe un comentario...", color = Color.Gray)
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 60.dp)
                                ) {
                                    innerTextField()
                                }
                            }
                        }
                    )

                    Spacer(Modifier.width(8.dp))

                    if (sending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 3.dp,
                            color = Color(0xFF3C9D6D)
                        )
                    } else {
                        Button(
                            onClick = {
                                if (newComment.isNotBlank() && token != null) {
                                    val commentText = newComment
                                    newComment = ""
                                    sending = true

                                    scope.launch {
                                        try {
                                            // Llamar API para crear comentario
                                            viewModel.addComentario(publicacionId, commentText, token!!)

                                            val ownerUser = userMap[ownerId]

                                            if (ownerUser != null &&
                                                !ownerUser.deviceToken.isNullOrBlank() &&
                                                ownerUser.id != myUserId
                                            ) {
                                                val notificationBody = mapOf(
                                                    "userId" to ownerUser.id,
                                                    "title" to "Nuevo comentario",
                                                    "message" to "Alguien comentó tu publicación",
                                                    "deviceToken" to ownerUser.deviceToken!!,
                                                    "publicacionId" to publicacionId
                                                )

                                                try {
                                                    Log.e("NOTIF_DEBUG", "📤 Enviando notificación con body: $notificationBody")
                                                    val resp = RetrofitNotificationInstance.api.sendNotification(notificationBody)
                                                    Log.e("NOTIF_DEBUG", "Notificación enviada: code=${resp.code()}")
                                                } catch (e: Exception) {
                                                    Log.e("NOTIF_DEBUG", "Error enviando notificación: ${e.message}")
                                                }
                                            } else {
                                                Log.e("NOTIF_DEBUG", "❌ No se envió notificación (sin token o eres tú mismo)")
                                            }
                                            // Pequeño delay para que se perciba natural (sin parpadeo)
                                            delay(1000)

                                            // Recargar los comentarios desde el backend
                                            viewModel.load(publicacionId, token!!)
                                            commentsCount = viewModel.uiState.comentarios.size

                                            // 🔥 Notificar a la pantalla anterior que hay que actualizar el contador
                                            navController.previousBackStackEntry
                                                ?.savedStateHandle
                                                ?.set("refreshComments", true)
                                        } catch (e: Exception) {
                                            println("Error al crear comentario: ${e.message}")
                                        } finally {
                                            sending = false
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C9D6D))
                        ) {
                            Text("Enviar", color = Color.White)
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (uiState.comentarios.isEmpty()) {
                item {
                    Text(
                        text = "💬 No hay comentarios aún. ¡Sé el primero en comentar!",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 50.dp)
                    )
                }
            } else {
                itemsIndexed(
                    uiState.comentarios,
                    key = { index, comentario ->
                        if (comentario.id.isNotBlank()) comentario.id
                        else keyCache.getOrPut("temp_$index") { UUID.randomUUID().toString() }
                    }
                ) { _, comentario ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            val userImage = userMap[comentario.userId]?.profileImageUrl

                            // --- FOTO DE PERFIL ---
                            AsyncImage(
                                model = userImage ?: "https://ui-avatars.com/api/?name=${
                                    (userMap[comentario.userId]?.name ?: "U")
                                }&background=3C9D6D&color=fff&size=128",
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(50))
                                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(50))
                            )

                            Spacer(Modifier.width(10.dp))

                            // --- BURBUJA DEL COMENTARIO ---
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 16.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF8F8F8)) // gris suave
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFFE2E2E2),  // borde gris clarito
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {

                                // Nombre + fecha
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (comentario.userId == myUserId) "Tú"
                                        else userMap[comentario.userId]?.name ?: "Usuario",
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF202020)
                                    )

                                    Spacer(Modifier.width(8.dp))

                                    Text(
                                        text = comentario.createdAt.take(10),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    text = comentario.text,
                                    color = Color(0xFF3A3A3A),
                                    maxLines = 2,                         // 🔥 solo 2 líneas
                                    overflow = TextOverflow.Ellipsis,     // agrega "..."
                                    modifier = Modifier.padding(end = 4.dp)
                                )

                                Spacer(Modifier.height(6.dp))

                                // ACCIONES
                                Row {
                                    if (comentario.userId == myUserId) {
                                        Spacer(Modifier.width(14.dp))

                                        Text(
                                            text = "Eliminar",
                                            color = Color.Red,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.clickable {
                                                confirmDeleteId = comentario.id
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal de confirmación
    if (confirmDeleteId != null) {
        AlertDialog(
            onDismissRequest = { confirmDeleteId = null },
            title = { Text("Eliminar comentario") },
            text = { Text("¿Seguro que deseas eliminar este comentario?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val idToDelete = confirmDeleteId
                        confirmDeleteId = null
                        if (idToDelete != null) {
                            val id = idToDelete
                            confirmDeleteId = null

                            scope.launch {
                                val originalList = uiState.comentarios

                                // 🔥 1. BORRAR INSTANTÁNEAMENTE
                                uiState.comentarios = originalList.filter { it.id != id }
                                commentsCount = uiState.comentarios.size

                                try {
                                    // 🔥 2. MANDAR LA API (background)
                                    token?.let { tk ->
                                        viewModel.deleteComentario(id, tk)
                                        navController.previousBackStackEntry
                                            ?.savedStateHandle
                                            ?.set("refreshComments", true)
                                    }
                                } catch (e: Exception) {
                                    // ❌ 3. SI FALLA → REVERTIR LISTA
                                    uiState.comentarios = originalList
                                    println("❌ Error al eliminar: ${e.message}")
                                }
                            }
                        }
                    }
                ) { Text("Eliminar", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteId = null }) { Text("Cancelar") }
            }
        )
    }
}

fun sanitizeSafeInputComments(input: String): String {
    var clean = input

    val forbiddenChars = listOf('<', '>', '/', '\\', '{', '}', '`', '=', '"', '\'')
    forbiddenChars.forEach { char ->
        clean = clean.replace(char.toString(), "")
    }

    clean = clean.replace(Regex("<[^>]*>"), "")
    clean = clean.replace(Regex("on\\w+\\s*=\\s*['\"].*?['\"]", RegexOption.IGNORE_CASE), "")
    clean = clean.replace(Regex("(javascript:|vbscript:|data:)", RegexOption.IGNORE_CASE), "")
    clean = clean.replace(Regex("&#\\d+;"), "")
    clean = clean.replace(Regex("&#x[0-9a-fA-F]+;"), "")
    clean = clean.replace(Regex("[\\u0000-\\u001F\\u007F]"), "")

    val forbiddenWords = listOf(
        "script", "iframe", "object", "embed", "form", "svg",
        "link", "style", "meta", "head", "body", "onerror", "onload"
    )
    forbiddenWords.forEach {
        clean = clean.replace(it, "", ignoreCase = true)
    }

    return clean
}