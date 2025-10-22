package com.example.explorifyapp.presentation.publicaciones

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.material3.Scaffold
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.BottomAppBar
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.explorifyapp.presentation.login.LoginViewModel
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import com.example.explorifyapp.data.remote.dto.Publication
import androidx.compose.material3.Card
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.lazy.items
import com.example.explorifyapp.data.remote.publications.RetrofitPublicationsInstance
import com.example.explorifyapp.domain.repository.PublicationsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable //,viewModel: MyPublicationsViewModel = viewModel()
fun MyPublicationsScreen(navController: NavController, loginViewModel: LoginViewModel = viewModel()) {

    var menuExpanded by remember { mutableStateOf(false) }
    //val repo = remember { PublicationsRepository() }
    val repo = remember { PublicationsRepository(RetrofitPublicationsInstance.api) }
    val factory = remember { MyPublicationsViewModelFactory(repo) }
    val viewModel: MyPublicationsViewModel = viewModel(factory = factory)

    LaunchedEffect(Unit) {
        val isLoggedIn = loginViewModel.isLoggedIn()
        if (!isLoggedIn) {
            navController.navigate("login") {
                popUpTo("mypublications") { inclusive = true }
            }
        }
        val userId = loginViewModel.userId
        val token = loginViewModel.token
        // posiblemente también usar userId si lo tienes
        if (!token.isNullOrEmpty() && userId.isNotEmpty()) {
            viewModel.loadPublications(userId, token)
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Lista de Aventuras") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
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
                                    loginViewModel.logout {
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
            BottomAppBar {
                IconButton(onClick = { navController.navigate("inicio") }) {
                    Icon(Icons.Default.Home, contentDescription = "Inicio")
                }
                IconButton(onClick = { navController.navigate("buscar") }) {
                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                }
                IconButton(onClick = { navController.navigate("perfil") }) {
                    Icon(Icons.Default.Person, contentDescription = "Perfil")
                }
            }
        }
    ) {
            paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when {
                viewModel.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                viewModel.errorMessage != null -> {
                    Text(text = viewModel.errorMessage ?: "Error", color = MaterialTheme.colorScheme.error)
                }
                else -> {
                    LazyColumn {
                        items(viewModel.publications) { pub ->
                            PublicationItem(pub)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun PublicationItem(pub: Publication) {
    Card(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
        Column {
            Image(
                painter = rememberAsyncImagePainter(pub.imageUrl),
                contentDescription = pub.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )
            Text(text = pub.title, style = MaterialTheme.typography.titleMedium)
            Text(text = pub.description, style = MaterialTheme.typography.bodyMedium)
            Text(text = pub.location, style = MaterialTheme.typography.bodySmall)
        }
    }
    }

