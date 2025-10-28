package com.example.explorifyapp.presentation.perfil

import android.util.Log
import com.example.explorifyapp.presentation.login.LoginViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ExitToApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(navController: NavController,
                 viewModel: LoginViewModel = viewModel()) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // 🔐 Validar si hay sesión
    LaunchedEffect(Unit) {
        viewModel.getUserData()
        val isLoggedIn = viewModel.isLoggedIn()
        if (!isLoggedIn) {
            navController.navigate("login") {
                popUpTo("perfil") { inclusive = true }
            }
        }
    }
    // Collect user data from the ViewModel
    val userData by viewModel.userData.collectAsState()
    val userId = userData?.userId ?:""
    val userName = userData?.username ?: "Usuario"
    val userEmail = userData?.userEmail ?: "correo@ejemplo.com"
    Log.d("PerfilScreen", "Perfildatos: ${userName}+ ${userEmail}")


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Menú de perfil con logout
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
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = false,
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
                    selected = true,
                    onClick = {}
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar circular
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD8E6D0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(60.dp),
                    tint = Color(0xFF355031)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nombre
            Text(
                text = userName,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            // Email
            Text(
                text = userEmail,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botones de opciones
            PerfilOptionButton(
                icon = Icons.Default.List,
                text = "Mi Lista de Aventuras"
            ) {
                navController.navigate("mispublicaciones")
            }

            PerfilOptionButton(
                icon = Icons.Default.Edit,
                text = "Editar Perfil"
            ) {
                navController.navigate("editprofile")
            }

            PerfilOptionButton(
                icon = Icons.Default.ExitToApp,
                text = "Cerrar Sesión"
            ) {
                showLogoutDialog = true
            }

            PerfilOptionButton(
                icon = Icons.Default.Delete,
                text = "Eliminar Cuenta",
                textColor = Color.Red
            ) {
                showDeleteDialog = true
                // Lógica para eliminar cuenta
            }
        }
    }

    // 🔔 Diálogo de confirmación para cerrar sesión
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout {
                        navController.navigate("login") {
                            popUpTo("inicio") { inclusive = true }
                        }
                    }
                }) {
                    Text("Aceptar", color = Color(0xFF355031))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Seguro que quieres cerrar sesión?") }
        )
    }

    // 🗑️ Diálogo de eliminar cuenta
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    // Aquí agregas tu lógica real para eliminar la cuenta
                    // Ejemplo: viewModel.deleteAccount()
                    navController.navigate("login") {
                        popUpTo("inicio") { inclusive = true }
                    }
                }) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Eliminar cuenta") },
            text = { Text("¿Seguro que deseas eliminar tu cuenta? Esta acción no se puede deshacer.") }
        )
    }
}


// 👇 Aquí va el botón reutilizable
@Composable
fun PerfilOptionButton(
    icon: ImageVector,
    text: String,
    textColor: Color = Color.Black,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFD8E6D0),
            contentColor = textColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, modifier = Modifier.weight(1f))
        }
    }
}