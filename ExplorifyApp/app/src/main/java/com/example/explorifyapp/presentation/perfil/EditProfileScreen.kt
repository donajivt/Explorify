package com.example.explorifyapp.presentation.perfil

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.R
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.example.explorifyapp.presentation.login.LoginViewModel
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.OutlinedTextField

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EditProfileScreen(navController: NavController, loginViewModel: LoginViewModel = viewModel(),
) {
    var profileName = remember { mutableStateOf("") }
    var email = remember { mutableStateOf("") }

    var menuExpanded by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("") }

    // 🔐 Validar si hay sesión
    LaunchedEffect(Unit) {
        val isLoggedIn = loginViewModel.isLoggedIn()
        if (!isLoggedIn) {
            navController.navigate("login") {
                popUpTo("editprofile") { inclusive = true }
            }
        }
        //permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        userName=loginViewModel.userName
        email.value = loginViewModel.userEmail // ✅ use .value
        profileName.value = loginViewModel.userName
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
                                    loginViewModel.logout {
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
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
                    .background(Color(0xFFD8E6D0)),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(60.dp),
                    tint = Color(0xFF355031)
                )
            }

          //  Spacer(modifier = Modifier.height(8.dp))


            Spacer(modifier = Modifier.height(24.dp))

            // Form fields
            ProfileField(
                label = "Nombre Completo",
                value = profileName.value,
                icon = Icons.Default.Person,
                onValueChange = { profileName.value = it }
            )

            ProfileField(
                label = "Correo Electrónico",
                value = email.value,
                icon = Icons.Default.Email,
                onValueChange = { email.value = it }
            )

            ProfileField(
                label = "Cambiar Contraseña",
                value = "********",
                icon = Icons.Default.Lock,
                onValueChange = {}
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {},
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

