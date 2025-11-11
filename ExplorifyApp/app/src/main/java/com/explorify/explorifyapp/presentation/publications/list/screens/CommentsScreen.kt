package com.explorify.explorifyapp.presentation.publications.list.screens

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
import androidx.compose.runtime.saveable.rememberSaveable
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    navController: NavController,
    publicacionId: String,
    viewModel: CommentsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var token by remember { mutableStateOf<String?>(null) }
    var myUserId by remember { mutableStateOf<String?>(null) }
    var newComment by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var confirmDeleteId by remember { mutableStateOf<String?>(null) }

    // Cache estable de IDs temporales (evita claves duplicadas)
    val keyCache = remember { mutableStateMapOf<String, String>() }
    // Mapa de usuarios
    val userRepo = remember {
        com.explorify.explorifyapp.domain.repository.UserRepositoryImpl(
            com.explorify.explorifyapp.data.remote.publications.RetrofitUsersInstance.api
        )
    }
    var userMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

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
            try {
                val users = withContext(Dispatchers.IO) { userRepo.getAllUsers(tk) }
                userMap = users.associate { u -> u.id to u.name }
            } catch (e: Exception) {
                println("⚠️ Error al obtener usuarios: ${e.message}")
            }
        }
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
            .imePadding(),
        topBar = {
            TopAppBar(
                title = { Text("Comentarios") },
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
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = newComment,
                        onValueChange = { newComment = it },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF1F1F1))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        decorationBox = { innerTextField ->
                            if (newComment.isEmpty()) {
                                Text("Escribe un comentario...", color = Color.Gray)
                            }
                            innerTextField()
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

                                            // Pequeño delay para que se perciba natural (sin parpadeo)
                                            delay(1000)

                                            // Recargar los comentarios desde el backend
                                            viewModel.load(publicacionId, token!!)
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                .animateContentSize(animationSpec = spring())
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = Color(0xFF3C9D6D),
                                modifier = Modifier.size(40.dp)
                            )

                            Spacer(Modifier.width(8.dp))

                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = if (comentario.userId == myUserId) "Tú"
                                    else userMap[comentario.userId] ?: "Usuario desconocido",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2B2B2B)
                                )
                                Text(
                                    text = comentario.text,
                                    color = Color(0xFF444444),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Text(
                                    text = comentario.createdAt.take(10),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }

                            if (comentario.userId == myUserId) {
                                IconButton(onClick = { confirmDeleteId = comentario.id }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = Color.Red
                                    )
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
                            scope.launch {
                                try {
                                    token?.let { tk ->
                                        viewModel.deleteComentario(idToDelete, tk)
                                    }
                                } catch (e: Exception) {
                                    println("Error al eliminar: ${e.message}")
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
