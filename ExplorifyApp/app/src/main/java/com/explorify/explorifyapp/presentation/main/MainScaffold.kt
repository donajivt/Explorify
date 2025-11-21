package com.explorify.explorifyapp.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.explorify.explorifyapp.presentation.buscar.BuscarScreen
import com.explorify.explorifyapp.presentation.perfil.PerfilScreen
import com.explorify.explorifyapp.presentation.publications.list.screens.PublicationListScreen
import com.explorify.explorifyapp.data.remote.publications.RetrofitPublicationsInstance
import com.explorify.explorifyapp.domain.repository.PublicationRepositoryImpl
import com.explorify.explorifyapp.domain.usecase.publications.PublicationUseCases
import com.explorify.explorifyapp.presentation.publicaciones.MyPublicationsScreen
import com.explorify.explorifyapp.presentation.publications.list.PublicationsListModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(parentNavController: NavHostController) {
    // 🔹 Nav interno
    val navController = rememberNavController()

    // --- ViewModel de publicaciones
    val api = remember { RetrofitPublicationsInstance.api }
    val repo = remember { PublicationRepositoryImpl(api) }
    val getAllUC = remember { PublicationUseCases.GetPublicationsUseCase(repo) }
    val getByIdUC = remember { PublicationUseCases.GetPublicationByIdUseCase(repo) }
    val deleteUC = remember { PublicationUseCases.DeletePublicationUseCase(repo) }
    val publicationsVM = remember { PublicationsListModel(getAllUC, getByIdUC, deleteUC) }

    // --- Scaffold global
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Explorify") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            BottomAppBar {
                IconButton(onClick = {
                    navController.navigate("publicaciones") {
                        popUpTo("publicaciones") { inclusive = false }
                    }
                }) {
                    Icon(Icons.Default.Home, contentDescription = "Inicio")
                }
                IconButton(onClick = {
                    navController.navigate("buscar") {
                        popUpTo("publicaciones") { inclusive = false }
                    }
                }) {
                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                }
                IconButton(onClick = {
                    navController.navigate("perfil") {
                        popUpTo("publicaciones") { inclusive = false }
                    }
                }) {
                    Icon(Icons.Default.Person, contentDescription = "Perfil")
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = "publicaciones"
            ) {
                composable("publicaciones") {
                    PublicationListScreen(
                        vm = publicationsVM,
                        navController = parentNavController,
                        onCreateClick = { userId ->
                            parentNavController.navigate("crear_publicacion/$userId")
                        },
                        onOpenDetail = { /* abrir detalle */ }
                    )
                }
                composable("buscar") {
                    BuscarScreen(navController = parentNavController)
                }
                composable("perfil") {
                    PerfilScreen(navController = navController)
                }

                composable("mispublicaciones") {
                    MyPublicationsScreen(navController = parentNavController)
                }

            }
        }
    }
}
