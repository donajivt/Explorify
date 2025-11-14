package com.explorify.explorifyapp.presentation.admin.perfil


import android.util.Log
import com.explorify.explorifyapp.presentation.login.LoginViewModel
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
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.ExitToApp
import com.explorify.explorifyapp.data.remote.users.RetrofitUsersInstance
import com.explorify.explorifyapp.domain.repository.UserRepository
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.sp
import com.explorify.explorifyapp.presentation.perfil.PerfilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilAdminScreen(navController: NavController,
                      viewModel: LoginViewModel = viewModel()) {
    val userApi = RetrofitUsersInstance.api
    val userRepository = remember { UserRepository(userApi) }
    val perfilViewModel = remember { PerfilViewModel(userRepository) }
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
    val token= userData?.token ?:""
    val userName = userData?.username ?: "Usuario"
    val userEmail = userData?.userEmail ?: "correo@ejemplo.com"
    val userRole=userData?.role ?:"Desconocido"
    Log.d("PerfilScreen", "Perfildatos: ${userName}+ ${userEmail}")


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
               /* navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },*/
            )
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
            //Role
            Text(
                text = userRole,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Botones de opciones
            PerfilOptionButton(
                icon = Icons.Default.List,
                text = "Lista de Usuarios"
            ) {
                navController.navigate("userList")
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
    /*if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false

                    perfilViewModel.deleteAccount(token) { success, message ->
                        if (success) {
                            // Redirigir al login y limpiar sesión
                            viewModel.logout {
                                navController.navigate("login") {
                                    popUpTo("inicio") { inclusive = true }
                                }
                            }
                        } else {
                            println("❌ Error al eliminar: $message")
                        }
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
    */

    // 🗑️ Diálogo de eliminar cuenta con validación de contraseña
    if (showDeleteDialog) {
        var passwordInput by remember { mutableStateOf("") }
        var deleteMessage by remember { mutableStateOf("") }
        val userEmail = userData?.userEmail ?: ""

        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar eliminación") },
            text = {
                Column {
                    Text("Para confirmar la eliminación, ingresa tu contraseña:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    if (deleteMessage.isNotEmpty()) {
                        Text(
                            deleteMessage,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (passwordInput.isBlank()) {
                            deleteMessage = "Por favor, ingresa tu contraseña"
                            return@TextButton
                        }

                        // 1️⃣ Verificar credenciales sin guardar token
                        viewModel.verifyCredentials(userEmail, passwordInput) { success, msg ->

                            if (success) {
                                val currentToken = userData?.token ?: ""
                                Log.d("DeleteAccount", "Token: $currentToken")
                                // 2️⃣ Si login correcto → eliminar cuenta
                                perfilViewModel.deleteAccount("$currentToken") { ok, message ->
                                    if (ok) {
                                        deleteMessage = "Cuenta eliminada correctamente"
                                        showDeleteDialog = false
                                        // 3️⃣ Cerrar sesión y volver al login
                                        viewModel.logout {
                                            navController.navigate("login") {
                                                popUpTo("inicio") { inclusive = true }
                                            }
                                        }
                                    } else {
                                        deleteMessage = "Error al eliminar: $message"
                                    }
                                }
                            } else {
                                deleteMessage = msg
                            }
                        }

                        /* viewModel.verifyCredentials(userEmail, passwordInput) { success, msg, tokenTemp ->
                             Log.d("DeleteAccount", "Token temporal: $tokenTemp")
                             if (success && tokenTemp != null) {
                                 // 2️⃣ Si login correcto → eliminar cuenta
                                 perfilViewModel.deleteAccount("Bearer $tokenTemp") { ok, message ->
                                     if (ok) {
                                         deleteMessage = "Cuenta eliminada correctamente"
                                         showDeleteDialog = false
                                         // 3️⃣ Cerrar sesión y volver al login
                                         viewModel.logout {
                                             navController.navigate("login") {
                                                 popUpTo("inicio") { inclusive = true }
                                             }
                                         }
                                     } else {
                                         deleteMessage = "Error al eliminar: $message"
                                     }
                                 }
                             } else {
                                 deleteMessage = msg
                             }
                         }
                         */
                    }
                ) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
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