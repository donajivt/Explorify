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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPublicationsScreen(navController: NavController,viewModel: LoginViewModel = viewModel()) {

    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val isLoggedIn = viewModel.isLoggedIn()
        if (!isLoggedIn) {
            navController.navigate("login") {
                popUpTo("mypublications") { inclusive = true }
            }
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
                                    viewModel.logout {
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
            // Tu contenido aquí
        }
    }
}
