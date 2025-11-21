package com.explorify.explorifyapp.presentation.admin.listUsers

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.foundation.lazy.items
import com.explorify.explorifyapp.data.remote.dto.User
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.navigation.NavController
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(navController: NavController,
    userListViewModel: UserListViewModel = viewModel()
) {
    val users by userListViewModel.users.collectAsState() // Observamos el flujo de usuarios
    var searchQuery by remember { mutableStateOf("") }

    // Filtrar los usuarios por nombre según la búsqueda
    val filteredUsers = if (searchQuery.isEmpty()) {
        users
    } else {
        users.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    // Llamamos a getUsers() cuando se entra por primera vez a la pantalla
    LaunchedEffect(Unit) {
        userListViewModel.getUsers()
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Lista de Usuarios") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    },
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar usuario...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    },
                    singleLine = true
                )
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = false,
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
                    onClick = {  } //navController.navigate("reportesList")
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = true,
                    onClick = { navController.navigate("perfilAdmin")}//
                )
            }
        }
    ) { paddingValues ->
        if (filteredUsers.isEmpty()) {
            // Si no hay usuarios, mostramos un texto
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay usuarios disponibles")
            }
        } else {
            // Lista de usuarios
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(filteredUsers) { user ->
                    UserItem(user){
                        navController.navigate("perfilAdminUsuario/${user.id}")
                    }
                    Divider() // separa visualmente los usuarios
                }
            }
        }
    }
}



@Composable
fun UserItem(user: User,onClick:()->Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable{ onClick()}
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono al lado del nombre
        /*
          Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            if (user.imageUrl?.isNotBlank() == true) {
                AsyncImage(
                    model = user.imageUrl,
                    contentDescription = "Foto de ${user.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Usuario sin foto",
                    tint = Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))
        */
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
           Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Usuario",
                tint =Color.Gray, // MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 12.dp)
            )
            /*if (user.imageUrl.isNotEmpty()) {
                Log.d("imagen url:"," ${finalImageUrl}")
                AsyncImage( //imageUrl

                    model = finalImageUrl + "?t=" + System.currentTimeMillis(),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                /* Image(
                     painter = rememberAsyncImagePainter(imageUrl),
                     contentDescription = "Foto de perfil",
                     modifier = Modifier.fillMaxSize(),
                     contentScale = ContentScale.Crop
                 )*/
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(60.dp),
                    tint = Color(0xFF355031)
                )
            }*/
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = user.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}
