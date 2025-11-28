package com.explorify.explorifyapp.presentation.perfil

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.explorify.explorifyapp.presentation.login.LoginViewModel
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.collectAsState
import com.explorify.explorifyapp.domain.repository.UserRepository
import com.explorify.explorifyapp.data.remote.users.RetrofitUserInstance
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.platform.LocalFocusManager
import java.io.File
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Context
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import android.graphics.BitmapFactory
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import android.util.Log
import androidx.compose.material.icons.filled.BorderColor
import coil.compose.AsyncImage
import java.io.FileOutputStream
import android.graphics.Bitmap

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EditProfileScreen(navController: NavController, loginViewModel: LoginViewModel = viewModel(),
) {
    val userRepository = remember { UserRepository(RetrofitUserInstance.api) }
    val perfilViewModel = remember { PerfilViewModel(userRepository) }

    var profileName = remember { mutableStateOf("") }
    var email = remember { mutableStateOf("") }
    val userData by loginViewModel.userData.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    var menuExpanded by remember { mutableStateOf(false) }
    var selectedImage by remember { mutableStateOf<File?>(null) }
    val context = LocalContext.current
    var imageToUpload by remember { mutableStateOf<File?>(null) }
    // 🔐 Validar si hay sesión
    /*LaunchedEffect(Unit) {

        val isLoggedIn = loginViewModel.isLoggedIn()
        if (!isLoggedIn) {
            navController.navigate("login") {
                popUpTo("editprofile") { inclusive = true }
            }
        }
        loginViewModel.loadUserData()
        //permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
       // profileName.value= userData?.username ?: "Usuario"//loginViewModel.userEmail // ✅ use .value
       // email.value =userData?.userEmail?: "correo@ejemplo.com" //loginViewModel.userName
    } */

    LaunchedEffect(Unit) {
        loginViewModel.getUserData()

        val isLoggedIn = loginViewModel.isLoggedIn()
        if (!isLoggedIn) {
            navController.navigate("login") {
                popUpTo("perfil") { inclusive = true }
            }
        }
        loginViewModel.loadUserData()
        /*val token = userData?.token ?: ""
        val userId = userData?.userId ?: ""

        if (token.isNotEmpty() && userId.isNotEmpty()) {
            perfilViewModel.getUserById(token, userId)
        } */
    }

    val userApiData by perfilViewModel.user.collectAsState()
    Log.d("datos del usuario","${userApiData}")
    /*val profileImageUrl by remember(userApiData) {
        mutableStateOf(userApiData?.profileImageUrl ?: "")
    }*/
    //var profileImageUrl by remember { mutableStateOf(userApiData?.profileImageUrl ?: "") }
    //val isLoading by perfilViewModel.isLoading.collectAsState()
    //val imageUrl = userApiData?.profileImageUrl
    LaunchedEffect(userData) {
        val token = userData?.token ?: ""
        val userId = userData?.userId ?: ""

        if (token.isNotEmpty() && userId.isNotEmpty()) {
            perfilViewModel.getUserById(token, userId)
        }
        userData?.let {
            profileName.value = it.username ?: ""
            email.value = it.userEmail ?: ""
            //profileImageUrl = it.profileImageUrl ?: ""
        }
        /*userApiData?.let{
            profileName.value = it.name ?: ""
            email.value = it.email ?: ""
            profileImageUrl = it.profileImageUrl ?: ""
        }*/
        Log.d("userapidata","${userApiData}")
    }
    var profileImageUrl by remember { mutableStateOf("") }
    //var oldprofileImageUrl by remember { mutableStateOf("") }
    var cloudinarypublicid by remember { mutableStateOf("") }

    LaunchedEffect(userApiData) {
        userApiData?.let {
            profileImageUrl = it.profileImageUrl ?: ""
            profileName.value = it.name ?: ""
            email.value = it.email ?: ""
            cloudinarypublicid = it.cloudinaryPublicId ?: ""
            Log.d("profileImageUrl actualizado", profileImageUrl)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = uri.toFile(context)
            selectedImage = file
        }
    }

    val isAdmin = userData?.role == "ADMIN"
    Log.d("rol: ","${userData?.role}")

    val bottomBarContent: @Composable () -> Unit = {
        if (isAdmin) {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
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
                    onClick = { navController.navigate("perfil")}
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
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
                    selected = false,
                    onClick = { navController.navigate("publicaciones") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    label = { Text("Buscar") },
                    selected = true,
                    onClick = { navController.navigate("buscar") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = false,
                    onClick = {navController.navigate("perfil")}
                )
            }
        }*/
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    focusManager.clearFocus()
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            // Header
            Text(
                text = "Editar Perfil",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF6A7C52), // soft green
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Profile photo
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .clickable { launcher.launch("image/*") }
                    .background(Color(0xFFD8E6D0)),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                if (selectedImage != null) {
                    Image(
                        bitmap = BitmapFactory.decodeFile(selectedImage!!.path).asImageBitmap(),
                        contentDescription = "Imagen seleccionada",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }else if (profileImageUrl.isNotEmpty()) {
                    // Imagen del servidor usando Coil
                    Log.d("url imagen: ","${profileImageUrl}")
                    AsyncImage(
                        model = "$profileImageUrl?t=${System.currentTimeMillis()}", // timestamp para evitar cache
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = Color(0xFF355031),
                        modifier = Modifier.size(60.dp)
                    )
                }
                /*Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(60.dp),
                    tint = Color(0xFF355031)
                )*/
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Form fields
            ProfileField(
                label = "Nombre Completo",
                value = profileName.value,
                icon = Icons.Default.Person,
                onValueChange = { newValue ->
                    profileName.value = sanitizeProfileInput(newValue)
                }
            )
            ProfileField(
                label = "Correo Electrónico",
                value = email.value,
                icon = Icons.Default.Email,
                onValueChange = { newValue ->
                    email.value = sanitizeProfileInput(newValue)
                }
            )

         /*   ProfileField(
                label = "Cambiar Contraseña",
                value = "********",
                icon = Icons.Default.Lock,
                onValueChange = {}
            )*/

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                  /*  val token = loginViewModel.userData.value?.token ?: ""
                    val updatedUser = UserRequest(
                        email = email.value,
                        username = profileName.value,
                    )
                    perfilViewModel.updateUser(token, updatedUser) { success, message ->
                        if (success) {
                            // ✅ Actualizamos localmente en LoginViewModel y Room
                            loginViewModel.updateUserData(
                                username = profileName.value,
                                userEmail = email.value
                            )
                            dialogMessage = "✅ Perfil actualizado correctamente."
                            showDialog = true
                            // Navegar de vuelta al perfil
                           /* navController.navigate("perfil") {
                                popUpTo("editprofile") { inclusive = true }
                            }*/
                        } else {
                            dialogMessage = "❌ Error al actualizar: $message"
                            showDialog = true
                            //println("❌ $message")
                        }
                    }
                */
                    if (profileName.value.isBlank() || email.value.isBlank()) {
                        dialogMessage = "Por favor, completa todos los campos para actualizar tu perfil."
                        showDialog = true
                    } else {
                        // 2. LÓGICA DE ACTUALIZACIÓN (SOLO SI LOS CAMPOS NO ESTÁN VACÍOS)
                        val token = loginViewModel.userData.value?.token ?: ""
                        /*val updatedUser = UserRequest(
                            email = email.value,
                            username = profileName.value,
                        )

                        perfilViewModel.updateUser(token, updatedUser) { success, message ->
                            if (success) {
                                // ✅ Actualizamos localmente en LoginViewModel y Room
                                loginViewModel.updateUserData(
                                    username = profileName.value,
                                    userEmail = email.value
                                )

                                // Configurar diálogo para éxito
                                dialogMessage = "Perfil actualizado correctamente."
                                showDialog = true
                            } else {
                                // Configurar diálogo para fallo
                                dialogMessage = "Error al actualizar: $message"
                                showDialog = true
                            }
                        }
                        */
                        Log.d("EDIT", "FILE: $selectedImage")

                        var imageToUpload = selectedImage
                        if(imageToUpload != null){
                            val sizeMB = imageToUpload.length() / (1024 * 1024)
                            Log.d("tamaño de imagen","${sizeMB}")
                            if (sizeMB > 1) {
                                val compressed = compressImage(imageToUpload, context)
                                val compressedMB = compressed.length() / (1024 * 1024)

                                if (compressedMB > 1) {
                                    dialogMessage = "La imagen sigue pesando demasiado incluso después de comprimirla."
                                    showDialog = true
                                    return@Button
                                }
                                Log.d("tamaño de imagen comprimido","${compressedMB}")
                                imageToUpload = compressed
                            }
                            perfilViewModel.updateUser(
                                token,
                                profileName.value,
                                email.value,
                                profileImageUrl,
                                cloudinarypublicid,
                                imageToUpload//selectedImage
                            ) { success, message ->

                                if (success) {
                                    loginViewModel.updateUserData(
                                        username = profileName.value,
                                        userEmail = email.value
                                    )

                                    dialogMessage = "Perfil actualizado correctamente."
                                    showDialog = true
                                } else {
                                    dialogMessage = "Error: $message"
                                    showDialog = true
                                }
                            }
                        }else{
                            perfilViewModel.updateUser(
                                token,
                                profileName.value,
                                email.value,
                                profileImageUrl,
                                cloudinarypublicid,
                                selectedImage
                            ) { success, message ->

                                if (success) {
                                    loginViewModel.updateUserData(
                                        username = profileName.value,
                                        userEmail = email.value
                                    )

                                    dialogMessage = "Perfil actualizado correctamente."
                                    showDialog = true
                                } else {
                                    dialogMessage = "Error: $message"
                                    showDialog = true
                                }
                            }

                        }


                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E4D2E)),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                // Se llama si el usuario hace clic fuera, pero aquí queremos forzar el botón
                showDialog = false
            },
            title = {
                Text(if (dialogMessage.startsWith("P")) "Éxito" else "Error")
            },
            text = {
                Text(dialogMessage)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false // Cerrar diálogo
                        // Si la actualización fue exitosa, navegamos de vuelta al perfil
                        if (dialogMessage.startsWith("P")) {
                            navController.navigate("perfil") {
                                popUpTo("editprofile") { inclusive = true }
                            }
                        }
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
}



@Composable
fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        trailingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF3E4D2E)
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,

    )
}



fun Uri.toFile(context: Context): File {
    val inputStream = context.contentResolver.openInputStream(this)!!
    val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.png")
    file.outputStream().use { output -> inputStream.copyTo(output) }
    return file
}

fun compressImage(file: File, context: Context): File {
    val bitmap = BitmapFactory.decodeFile(file.path)

    val compressedFile = File(context.cacheDir, "compressed_${file.name}")

    val output = FileOutputStream(compressedFile)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, output) // calidad 70%
    output.flush()
    output.close()

    return compressedFile
}


fun sanitizeProfileInput(text: String): String {
    // Lista de caracteres peligrosos SIN incluir el espacio
    val forbidden = listOf('<', '>', '/', '\\', '"', '\'', '{', '}', '`', '=', ';')

    var clean = text
    forbidden.forEach { char ->
        clean = clean.replace(char.toString(), "")
    }

    return clean
}

/*
@Composable
fun ProfileField(
    label: String,
    value: String,
    icon: ImageVector,
    onValueChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6EFD2)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontWeight = FontWeight.Bold)
                Text(value, color = Color.Gray, fontSize = 13.sp)
            }
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF3E4D2E),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
-------------------------------------------
colors = OutlinedTextFieldDefaults.colors(
            focusedIndicatorColor = Color(0xFF6A7C52),
            unfocusedIndicatorColor = Color(0xFFE6EFD2),
            cursorColor = Color(0xFF6A7C52),
            focusedLabelColor = Color(0xFF6A7C52),
            unfocusedLabelColor = Color.Gray
        )

*/