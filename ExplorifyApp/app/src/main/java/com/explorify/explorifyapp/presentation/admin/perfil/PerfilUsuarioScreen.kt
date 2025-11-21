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
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.Campaign
import com.explorify.explorifyapp.data.remote.users.RetrofitUserInstance
import com.explorify.explorifyapp.domain.repository.UserRepository
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.sp
import com.explorify.explorifyapp.presentation.perfil.PerfilOptionButton
import com.explorify.explorifyapp.data.remote.publications.RetrofitPublicationsInstance
import com.explorify.explorifyapp.presentation.utils.email.buildPublicationDeletedTemplate
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.explorify.explorifyapp.domain.repository.PublicationRepositoryImpl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUsuarioScreen( userId: String?,navController: NavController,
                 viewModel: LoginViewModel = viewModel()) {
    val userApi = RetrofitUserInstance.api
    val userRepository = remember { UserRepository(userApi) }
    val publicationsApi = RetrofitPublicationsInstance.api
    val publicationsRepository = remember { PublicationRepositoryImpl(publicationsApi) }

    // Crear factory
    val factory = remember { PerfilUsuarioViewModelFactory(userRepository, publicationsRepository) }
    // Crear ViewModel con factory
    val userProfileViewModel: PerfilUsuarioViewModel = viewModel(factory = factory)

    //val perfilViewModel = remember { PerfilViewModel(userRepository) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    Log.d("userid","${userId}")
    val userData by viewModel.userData.collectAsState()
    //val userId = userData?.userId ?:""
    val token= userData?.token ?:""
    val userEmail = userData?.userEmail ?: "correo@ejemplo.com"
    /*LaunchedEffect(userId) {
        if (userId != null) {
            userProfileViewModel.getUserById(token, userId)
        }
    }*/
    val user by userProfileViewModel.user.collectAsState()
    Log.d("Usuario","${user}")
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
    Log.d("token","${userData?.token}")
    // Collect user data from the ViewModel
    /*val userData by viewModel.userData.collectAsState()
    val userId = userData?.userId ?:""
    val token= userData?.token ?:""
    val userName = userData?.username ?: "Usuario"
    val userEmail = userData?.userEmail ?: "correo@ejemplo.com"
    Log.d("PerfilScreen", "Perfildatos: ${userName}+ ${userEmail}")
    */
    /*LaunchedEffect(userId, userData) {
        Log.d("launchedeffect","entro a la funcion")
        val validToken = userData?.token
        if (userId != null && !validToken.isNullOrBlank()) {
            Log.d("no son nulos","no nulos")
            userProfileViewModel.getUserById(validToken, userId)
        }
    }*/
    val vtoken = userData?.token
    val uid = userId
    /*LaunchedEffect(vtoken, uid) {
        Log.d("launchedeffect", "token=$vtoken userId=$uid")

        if (!token.isNullOrBlank() && !uid.isNullOrBlank()) {
            Log.d("launchedeffect", "Entró a getUserById")
            userProfileViewModel.getUserById(vtoken, uid)
        } else {
            Log.d("launchedeffect", "token o uid inválidos")
        }
    }*/
    LaunchedEffect(vtoken, uid) {
        Log.d("launched", "token=$vtoken uid=$uid")

        val safeToken = vtoken
        val safeUserId = uid

        if (!safeToken.isNullOrBlank() && !safeUserId.isNullOrBlank()) {
            Log.d("GETUSERBYID", "Llamando a getUserById()")
            userProfileViewModel.getUserById(
                safeToken,
                safeUserId
            )
        } else {
            Log.d("GETUSERBYID", "token o userId nulos, no se llama API")
        }
    }

    var shouldNavigate by remember { mutableStateOf(false) }

    val emailResult by userProfileViewModel.emailResult.observeAsState("")
    var showSendEmailDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil de ") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
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
                    onClick = { navController.navigate("reportes") } //
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
        if (user == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }else {
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
                    /*  Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(60.dp),
                    tint = Color(0xFF355031)
                )*/
                    if (user?.profileImageUrl?.isNotBlank() == true) {
                        Log.d("imagen url:", " ${user!!.profileImageUrl}")
                        AsyncImage( //imageUrl

                            model = user!!.profileImageUrl + "?t=" + System.currentTimeMillis(),
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
                }

                Spacer(modifier = Modifier.height(12.dp))
                /*
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
                */
                if (user != null) {
                    Text(
                        text = user!!.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    Text(
                        text = user!!.email,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                    )

                    /*Text(
                    text = "ID: ${user!!.id}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                )*/
                    Spacer(modifier = Modifier.height(32.dp))
                    // Botones de opciones
                    /*PerfilOptionButton(
                    icon = Icons.Default.List,
                    text = "Mi Lista de Aventuras"
                ) {
                    navController.navigate("mispublicaciones")
                } */

                    PerfilOptionButton(
                        icon = Icons.Default.Campaign,
                        text = "Mandar Notificación"
                    ) {
                        showSendEmailDialog = true
                        // navController.navigate("")
                    }

                    /*
                PerfilOptionButton(
                    icon = Icons.Default.ExitToApp,
                    text = "Cerrar Sesión"
                ) {
                    showLogoutDialog = true
                }
*/

                    PerfilOptionButton(
                        icon = Icons.Default.Delete,
                        text = "Eliminar Usuario",
                        textColor = Color.Red
                    ) {
                        showDeleteDialog = true
                        // Lógica para eliminar cuenta
                    }
                } else {
                    Text("Cargando usuario...")
                }
            }
        }
    }

    // 🔔 Diálogo de confirmación para cerrar sesión
    /*
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
*/
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
        //val userEmail = userData?.userEmail ?: ""

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
                        /*viewModel.verifyCredentials(userEmail, passwordInput) { success, msg ->
                            Log.d("entro en el verificar","${userEmail} +${passwordInput}")
                            if (success) {
                                val currentToken = userData?.token ?: ""
                                Log.d("DeleteAccount", "Token: $currentToken")
                                // 2️⃣ Si login correcto → eliminar cuenta
                                userProfileViewModel.deleteAccount("$currentToken","$user!!.id") { ok, message ->
                                    if (ok) {
                                        deleteMessage = "Cuenta eliminada correctamente"
                                        showDeleteDialog = false
                                        // 3️⃣ Cerrar sesión y volver al login
                                      /*  viewModel.logout {
                                            navController.navigate("login") {
                                                popUpTo("inicio") { inclusive = true }
                                            }
                                        }*/
                                    } else {
                                        deleteMessage = "Error al eliminar: $message"
                                        Log.d("error en eliminar  screen","${message}")
                                    }
                                }
                            } else {
                                deleteMessage = msg
                                Log.d("error en verificar","${msg}")
                            }
                        }
                        */
                        val currentToken = userData?.token ?: ""
                        Log.d("DeleteAccount", "Token: $currentToken")
                        Log.d("id del usuario","${user!!.id}")
                        val idusuario= user!!.id
                        // 2️⃣ Si login correcto → eliminar cuenta
                        userProfileViewModel.deleteAccount("$currentToken","$idusuario") { ok, message ->
                            if (ok) {
                                deleteMessage = "Cuenta eliminada correctamente"
                                showDeleteDialog = false
                                shouldNavigate=true
                                //navController.navigate("userList")

                            } else {
                                deleteMessage = "Error al eliminar: $message"
                                Log.d("error en eliminar  screen","${message}")
                            }
                        }
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
    if (shouldNavigate) {
        LaunchedEffect(Unit) {
            navController.navigate("userList") {
                popUpTo("perfilAdmin") { inclusive = true }
            }
            shouldNavigate = false
        }
    }
    if (showSendEmailDialog && user != null) {

        AlertDialog(
            onDismissRequest = { showSendEmailDialog = false },
            title = { Text("Enviar notificación") },
            text = { Text("¿Seguro que quieres notificar al usuario?") },
            confirmButton = {
                TextButton(onClick = {
                    val emailBody = buildPublicationDeletedTemplate(
                        username = user!!.name,
                        publicationTitle = "Una de tus publicaciones",
                        reason = "Incumple las normas de la comunidad"
                    )

                    userProfileViewModel.sendEmail(
                        to = user!!.email,
                        subject = "Notificación sobre tu publicación",
                        body = emailBody
                    )

                    showSendEmailDialog = false
                }) {
                    Text("Enviar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSendEmailDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

}

/*
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
 */