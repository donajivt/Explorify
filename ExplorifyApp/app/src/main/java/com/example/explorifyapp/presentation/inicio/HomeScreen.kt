package com.example.explorifyapp.presentation.inicio

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.BottomAppBar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavController
import com.example.explorifyapp.presentation.login.LoginViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun HomeScreen(userName: String,navController: NavController, viewModel: LoginViewModel = viewModel()) {
    LaunchedEffect(Unit) {
        val isLoggedIn = viewModel.isLoggedIn()
        if (!isLoggedIn) {
            navController.navigate("login") {
                popUpTo("home/{$userName}") { inclusive = true }
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Título de la página") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("perfil")
                    }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Perfil")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                IconButton(onClick = { navController.navigate("home") }) {
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
        // Aquí va el contenido principal
        Box(modifier = Modifier.padding(it)) {
            Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Bienvenido a la Home",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Hola, $userName 👋", style = MaterialTheme.typography.bodyLarge)
                }
        }
    }

}