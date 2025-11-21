package com.explorify.explorifyapp.presentation.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import com.explorify.explorifyapp.R
import kotlinx.coroutines.launch
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.platform.LocalFocusManager

@Composable
fun LoginScreen(navController: NavController) {
    val viewModel: LoginViewModel = viewModel()

    var userName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var userNameError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val loginResult by viewModel.loginResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val focusManager = LocalFocusManager.current
    LaunchedEffect(Unit) {
        viewModel.getUserData()
        val isLoggedIn = viewModel.isLoggedIn()
        if (isLoggedIn) {
            navController.navigate("splash") {
                popUpTo("perfil") { inclusive = true }
            }
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent // Para que se vea el fondo con la imagen
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { focusManager.clearFocus() }
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
                    .height(460.dp),
                    //.wrapContentHeight(),
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
                        onValueChange = {
                            userName = it
                            if (userNameError) userNameError = false
                        },
                        label = { Text("Correo Electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = userNameError, // ← activa borde rojo
                        supportingText = {
                            if (userNameError) {
                                Text(
                                    "El usuario es obligatorio",
                                    color = MaterialTheme.colorScheme.error
                                )
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
                        onValueChange = {
                            password = it
                            if (passwordError) passwordError = false
                        },
                        label = { Text("Contraseña") },
                        visualTransformation =  if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        isError = passwordError,
                        trailingIcon = {
                            val image = if (passwordVisible)
                                Icons.Filled.Visibility // icono "ojo abierto"
                            else
                                Icons.Filled.VisibilityOff // icono "ojo tachado"

                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = image,
                                    contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                                )
                            }
                        },
                        supportingText = {
                            if (passwordError) {
                                Text(
                                    "La contraseña es obligatoria",
                                    color = MaterialTheme.colorScheme.error
                                )
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
                            var hasError = false

                            if (userName.isBlank()) {
                                userNameError = true
                                hasError = true
                            }

                            if (password.isBlank()) {
                                passwordError = true
                                hasError = true
                            }

                            if (hasError) {
                                errorMessage = "Por favor, completa todos los campos"
                                coroutineScope.launch {
                                    snackbarHostState.currentSnackbarData?.dismiss()   // 👈 cierra el anterior si existe
                                    snackbarHostState.showSnackbar(
                                        message = errorMessage!!,
                                        duration = SnackbarDuration.Short              // 👈 más rápido
                                    )
                                }
                                /*if (userName.isBlank() || password.isBlank()) {
                    errorMessage = "Por favor, completa todos los campos"
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(errorMessage!!)
                    }*/

                            } else {
                                errorMessage = null
                                viewModel.login(userName, password)
                            }
                        },
                        enabled = !isLoading && userName.isNotBlank() && password.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF355031),
                            contentColor = Color.White,
                            disabledContainerColor = Color.Gray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Entrar")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    //Text(text = loginResult)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Hipervínculo para ir a Register
                    TextButton(
                        onClick = {
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
                        if (loginResult.isNotEmpty()) {
                            if (loginResult.startsWith("Bienvenido")) {
                                navController.navigate("publicaciones")
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.currentSnackbarData?.dismiss()   // 👈 quita el snackbar anterior
                                    snackbarHostState.showSnackbar(
                                        message = "Usuario y/o contraseña incorrectos",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                            viewModel.resetLoginResult()
                        }
                    }
                }
            }
        }
    }
}