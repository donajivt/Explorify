package com.explorify.explorifyapp.presentation.admin.report

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CardDefaults
import androidx.room.util.TableInfo
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.explorify.explorifyapp.data.remote.room.AppDatabase
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Delete
import android.util.Log
import androidx.lifecycle.viewmodel.compose.viewModel
import com.explorify.explorifyapp.presentation.admin.listUsers.UserListViewModel
import com.explorify.explorifyapp.presentation.login.LoginViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.width
import androidx.compose.material3.TextButton
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog

@ExperimentalMaterial3Api
@Composable
fun ReportListScreen(
    vm: ReportViewModel,
    navController: NavController,
    userListVM: UserListViewModel = viewModel(),
    viewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val items = vm.reportedItems
    val loading = vm.loading
    val error = vm.error
    var token by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        token = AppDatabase.getInstance(context).authTokenDao().getToken()?.token
        //token?.let { tk -> vm.load(tk) }
    // vm.load(token)
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
    LaunchedEffect(Unit) {
        val isLoggedIn = viewModel.isLoggedIn()
        if (!isLoggedIn) {
            navController.navigate("login") {
                popUpTo("adminDashboard") { inclusive = true }
            }
        }
    }
    val usuarios by userListVM.usuarios.collectAsState()
    val userMap = usuarios.associate { it.id to (it.name to it.profileImageUrl) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Publicaciones reportadas") }) }
                ,bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = false,
                    onClick = {navController.navigate("publicaciones") } //navController.navigate("adminDashboard")
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
                    selected = true,
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
    ) { padding ->
        when {
            loading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Text("Error: $error", color = Color.Red)
            }

            /*items.isEmpty() -> {
                Text("No hay publicaciones reportadas", Modifier.padding(20.dp))
            }*/
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        //.padding(padding),
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                if (items.isEmpty()) {
                    Text(
                        "No hay publicaciones reportadas válidas.",
                        Modifier.padding(20.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        items(items) { item ->
                            val userData = userMap[item.publication.userId]
                            Log.d("userData","${userData}")
                            val name = userData?.first ?: "Usuario desconocido"
                            val image = userData?.second
                            ReportedPublicationFullCard(
                                item = item,
                                authorName = name,
                                authorImage = image,
                                onDelete = {

                                    vm.deletePublication(item.publication, token!!) {
                                        vm.load(token!!)
                                    }
                                },
                                navController = navController
                            )
                        }
                    }
                }

            }
        }}
    }
}

@Composable
fun ReportedPublicationFullCard(
    item: ReportedPublicationItem,
    authorName: String,
    authorImage: String?,
    onDelete: () -> Unit,
    navController: NavController
) {
    var showDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var overflowingDesc by remember { mutableStateOf(false) }
    var titleExpanded by remember { mutableStateOf(false) }
    var overflowingTitle by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1B1C) // Fondo dark
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {

        Column {

            // ::::::::::::::::: IMAGEN ::::::::::::::::::
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                AsyncImage(
                    model = item.publication.imageUrl,
                    contentDescription = item.publication.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Difuminado inferior
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                )

                // Etiqueta
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .background(
                            Color.Black.copy(alpha = 0.4f), // Fondo transparente
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        // FOTO DEL AUTOR
                        AsyncImage(
                            model = authorImage,
                            contentDescription = "Autor",
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(Modifier.width(6.dp))

                        // NOMBRE DEL AUTOR
                        Text(
                            authorName,
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                // Botón eliminar
                Row(
                    modifier = Modifier.align(Alignment.TopEnd)
                        .padding(10.dp)
                ) {
                    IconButton(
                        onClick = { showDialog = true },
                        modifier = Modifier
                            .size(46.dp)
                            .background(Color.Red.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Delete, null, tint = Color.White)
                    }
                }
            }

            // ::::::::::::::::: CUERPO ::::::::::::::::::
            Column(modifier = Modifier.padding(12.dp)) {

                // Título
                Text(
                    text = item.publication.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    ),
                    maxLines = if (titleExpanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { layout ->
                        overflowingTitle = if (titleExpanded) {
                            false
                        } else {
                            layout.didOverflowWidth
                        }
                    }
                )

                if (overflowingTitle) {
                    Text(
                        text = if (titleExpanded) "Ver menos ▲" else "Ver más ▼",
                        color = Color(0xFF64B5F6),
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clickable { titleExpanded = !titleExpanded }
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Descripción con expand/collapse
                Text(
                    text = item.publication.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFFDDDDDD)
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
                        color = Color(0xFF64B5F6),
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable { expanded = !expanded }
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Autor

                Spacer(Modifier.height(14.dp))

                // :::::::::::::::::: INFO DE REPORTES ::::::::::::::::::

                Text(
                    "Total reportes: ${item.reports.size}",
                    color = Color(0xFFBFAE94)
                )

                Spacer(Modifier.height(8.dp))

                Text("Reportado por:", color = Color.White)
                item.reporterNames.forEach {
                    Text("• $it", color = Color.LightGray)
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    "Último reporte: " +
                            (item.reports.maxByOrNull { it.createdAt }?.createdAt ?: "—"),
                    color = Color.Gray
                )
            }
        }
    }

    // ::::::::::::::::: DIÁLOGO ELIMINAR ::::::::::::::::::
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



/*
@Composable
fun ReportedPublicationCard(
    item: ReportedPublicationItem,
    authorName: String,
    authorImage: String?,
    onDelete: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .shadow(6.dp, RoundedCornerShape(20.dp), clip = true),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B1C)),
        border = BorderStroke(1.dp, Color(0xFFBFAE94).copy(alpha = 0.4f))
    ) {

        Column {

            // --- Imagen principal ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                AsyncImage(
                    model = item.publication.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(Modifier.padding(16.dp)) {

                // --- Título + botón eliminar ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.publication.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar publicación",
                            tint = Color.Red
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // --- Autor con foto ---
                Row(verticalAlignment = Alignment.CenterVertically) {

                    AsyncImage(
                        model = authorImage,
                        contentDescription = "Foto autor",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = authorName,
                        style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // --- Datos del reporte ---
                Text(
                    "Total reportes: ${item.reports.size}",
                    color = Color(0xFFBFAE94)
                )

                Spacer(Modifier.height(6.dp))

                Text("Reportado por:", color = Color.White)
                item.reporterNames.forEach { name ->
                    Text("• $name", color = Color.LightGray)
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    "Último reporte: " + (item.reports.maxByOrNull { it.createdAt }?.createdAt ?: ""),
                    color = Color.Gray
                )
            }
        }
    }
}

*/
/* Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = authorImage,
                        contentDescription = "Autor",
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.width(10.dp))

                    Text(
                        text = authorName,
                        style = MaterialTheme.typography.labelLarge.copy(color = Color.White)
                    )
                }*/
/*
@Composable
fun ReportedPublicationCard(
    item: ReportedPublicationItem,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row( ) {
                Text(item.publication.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { onDelete() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar reporte",
                        tint = Color.Red,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            AsyncImage(
                model = item.publication.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.height(12.dp))

            Text("Total reportes: ${item.reports.size}")

            Text("Reportado por:")
            item.reporterNames.forEach {
                Text("• $it")
            }
              Log.d("reporterNames:","${item.reporterNames}")
            Spacer(Modifier.height(8.dp))

            Text(
                "Último reporte: " + item.reports.maxByOrNull { it.createdAt }?.createdAt.orEmpty(),
                color = Color.Gray
            )

            Spacer(Modifier.height(16.dp))

            /*
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Eliminar publicación", color = Color.White)
            }
            */
        }
    }
}
*/

