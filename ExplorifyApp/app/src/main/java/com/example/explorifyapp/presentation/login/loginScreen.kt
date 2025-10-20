package com.example.explorifyapp.presentation.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.explorifyapp.R
import kotlinx.coroutines.launch
import com.example.explorifyapp.presentation.login.LoginViewModel
import androidx.compose.material3.CheckboxDefaults.colors
import androidx.compose.material3.MaterialTheme



@Composable
fun LoginScreen(navController: NavController) {
    val viewModel: LoginViewModel = viewModel()

    var userName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var userNameError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }


    var errorMessage by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val loginResult by viewModel.loginResult.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent // Para que se vea el fondo con la imagen
    ) { paddingValues ->
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.mountains),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Iniciar Sesión", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = { userName = it
                if (userNameError) userNameError = false
                            },
            label = { Text("Usuario") },
            modifier = Modifier.fillMaxWidth(),
            isError = userNameError, // ← activa borde rojo
            supportingText = {
                if (userNameError) {
                    Text("El usuario es obligatorio", color = MaterialTheme.colorScheme.error)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = Color.Gray,
                focusedTextColor = Color.DarkGray,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error,
                errorSupportingTextColor = MaterialTheme.colorScheme.error
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it
                if (passwordError) passwordError = false
            },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = passwordError,
            supportingText = {
                if (passwordError) {
                    Text("La contraseña es obligatoria", color = MaterialTheme.colorScheme.error)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = Color.Gray,
                focusedTextColor = Color.DarkGray,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error,
                errorSupportingTextColor = MaterialTheme.colorScheme.error
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (userName.isBlank() || password.isBlank()) {
                    errorMessage = "Por favor, completa todos los campos"
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(errorMessage!!)
                    }
                } else {
                    errorMessage = null
                    viewModel.login(userName, password)
                }
            // viewModel.login(userName, password)
                      },
            enabled = userName.isNotBlank() && password.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF355031),
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Entrar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        //Text(text = loginResult)

        Spacer(modifier = Modifier.height(24.dp))

        // Hipervínculo para ir a Register
        TextButton(onClick = {
            navController.navigate("register")

        },
            colors = ButtonDefaults.textButtonColors(
                contentColor = Color.DarkGray
            )
            ) {
            Text("¿No tienes cuenta? Regístrate aquí")
        }

        // Navegar si login fue exitoso
        LaunchedEffect(loginResult) {
            if (loginResult.startsWith("Bienvenido")) {
                // Extraer nombre del usuario desde el mensaje o del ViewModel
                val name = viewModel.userName
                navController.navigate("home/${name}")
            }
        }
    }
        }
    }
}
}