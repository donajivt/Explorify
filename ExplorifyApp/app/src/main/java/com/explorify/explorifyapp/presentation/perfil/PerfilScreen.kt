package com.explorify.explorifyapp.presentation.perfil

import androidx.compose.ui.text.input.VisualTransformation
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
import androidx.compose.material.icons.filled.ExitToApp
import com.explorify.explorifyapp.data.remote.users.RetrofitUserInstance
import com.explorify.explorifyapp.domain.repository.UserRepository
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.BorderColor
import coil.compose.AsyncImage
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(navController: NavController,
                 viewModel: LoginViewModel = viewModel()) {
    val userApi = RetrofitUserInstance.api
    val userRepository = remember { UserRepository(userApi) }
    val perfilViewModel = remember { PerfilViewModel(userRepository) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    // 🔐 Validar si hay sesión
   /* LaunchedEffect(Unit) {
        viewModel.getUserData()
        val isLoggedIn = viewModel.isLoggedIn()
        if (!isLoggedIn) {
            navController.navigate("login") {
                popUpTo("perfil") { inclusive = true }
            }
        }
    }*/
    // Collect user data from the ViewModel
    val userData by viewModel.userData.collectAsState()
    val userId = userData?.userId ?:""
    val token= userData?.token ?:""
    val userName = userData?.username ?: "Usuario"
    if (token.isNotEmpty() && userId.isNotEmpty()) {
        Log.d("entro al if","de token y id no nulos")
        perfilViewModel.getUserById(token, userId)
    }
    val userEmail = userData?.userEmail ?: "correo@ejemplo.com"
    Log.d("PerfilScreen", "Perfildatos: ${userName}+ ${userEmail}")
    LaunchedEffect(Unit) {
        viewModel.getUserData()

        val isLoggedIn = viewModel.isLoggedIn()
        if (!isLoggedIn) {
            navController.navigate("login") {
                popUpTo("perfil") { inclusive = true }
            }
        }

        val token = viewModel.userData.value?.token ?: ""
        val userId = viewModel.userData.value?.userId ?: ""

        if (token.isNotEmpty() && userId.isNotEmpty()) {
            Log.d("entro al if","de token y id no nulos")
            perfilViewModel.getUserById(token, userId)
        }
    }
    val userApiData by perfilViewModel.user.collectAsState()
    val isLoading by perfilViewModel.isLoading.collectAsState()
    val imageUrl = userApiData?.profileImageUrl
    var profileImageUrl by remember { mutableStateOf("") }
    LaunchedEffect(userApiData) {
        userApiData?.profileImageUrl?.let {
            profileImageUrl = it
            Log.d("PerfilScreen", "Imagen actualizada: $it")
        }
    }
    val finalName = userApiData?.name ?: userData?.username ?: "Usuario"
    val finalEmail = userApiData?.email ?: userData?.userEmail ?: "correo@ejemplo.com"
    val finalImageUrl = userApiData?.profileImageUrl
    val isAdmin = userData?.role == "ADMIN"
    Log.d("rol: ","${userData?.role}")
    val context = LocalContext.current

    val bottomBarContent: @Composable () -> Unit = {
        if (isAdmin) {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Inicio") },
                    selected = false,
                    onClick = { navController.navigate("adminDashboard") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.BorderColor, contentDescription = "Reportes") },
                    label = { Text("Reportes") },
                    selected = false,
                    onClick = { navController.navigate("reportes") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil Admin") },
                    label = { Text("Perfil") },
                    selected = true,
                    onClick = { navController.navigate("perfilAdmin") }
                )
            }
        } else {
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
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
        bottomBar = bottomBarContent
    /*{
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = true,
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
                    onClick = { navController.navigate("reportes") } //
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = false,
                    onClick = { navController.navigate("perfilAdmin")}//
                )
            }
        } */
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
            ) { //!imageUrl.isNullOrEmpty()
                if (profileImageUrl.isNotEmpty()) {
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
                }
                /*Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(60.dp),
                    tint = Color(0xFF355031)
                )*/
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nombre
            Text(
                text = finalName,//userName,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            // Email
            Text(
                text = finalEmail,//userEmail,
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
                icon= Icons.Default.Edit,
                text="Editar Contraseña") {
                showPasswordDialog=true
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

    if(showPasswordDialog){
        var oldPass by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }
        var confirmPass by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf("") }
        var oldPassVisible by remember { mutableStateOf(false) }
        var newPassVisible by remember { mutableStateOf(false) }
        var confirmPassVisible by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Cambiar Contraseña") },
            text = {
                Column {
                    OutlinedTextField(
                        value = oldPass,
                        onValueChange = { oldPass = it },
                        label = { Text("Contraseña actual") },
                        //visualTransformation = PasswordVisualTransformation(),
                        visualTransformation = if (oldPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { oldPassVisible = !oldPassVisible }) {
                                Icon(
                                    imageVector = if (oldPassVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("Nueva contraseña") },
                        //visualTransformation = PasswordVisualTransformation(),
                        visualTransformation = if (newPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { newPassVisible = !newPassVisible }) {
                                Icon(
                                    imageVector = if (newPassVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it },
                        label = { Text("Confirmar nueva contraseña") },
                        //visualTransformation = PasswordVisualTransformation(),
                        visualTransformation = if (confirmPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmPassVisible = !confirmPassVisible }) {
                                Icon(
                                    imageVector = if (confirmPassVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        singleLine = true
                    )

                    if (errorMsg.isNotEmpty()) {
                        Text(errorMsg, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        errorMsg = ""

                        if (oldPass.isBlank() || newPass.isBlank() || confirmPass.isBlank()) {
                            errorMsg = "Todos los campos son obligatorios"
                            return@TextButton
                        }

                        if (newPass != confirmPass) {
                            errorMsg = "Las contraseñas no coinciden"
                            return@TextButton
                        }

                        perfilViewModel.changePassword(token, oldPass, newPass) { success, message ->
                            if (success) {
                                errorMsg = ""
                                showPasswordDialog = false

                                Toast.makeText(
                                    context,
                                    "Contraseña actualizada correctamente",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                errorMsg = message
                            }
                        }
                    }
                ) {
                    Text("Actualizar", color = Color(0xFF355031))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPasswordDialog = false }
                ) {
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

