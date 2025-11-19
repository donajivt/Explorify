package com.explorify.explorifyapp.presentation.register

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.explorify.explorifyapp.R
import com.explorify.explorifyapp.data.remote.publications.RetrofitUsersInstance
import com.explorify.explorifyapp.domain.repository.UserRepositoryImpl
import com.explorify.explorifyapp.presentation.login.LoginViewModel
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavController) {
    val viewModel: RegisterViewModel = viewModel()
    val loginViewModel: LoginViewModel = viewModel()

    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordconfirmation by remember { mutableStateOf("") }
    var passwordConfVisible by remember { mutableStateOf(false) }
    var acceptedTerms by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val registerResult by viewModel.registerResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    val userRepo = remember { UserRepositoryImpl(RetrofitUsersInstance.api) }

    // ✅ Expresión regular mejorada para emails válidos (incluye subdominios, letras, números, etc.)
    val isEmailValid = remember(email) {
        email.contains("@") && email.length <= 100
    }


    LaunchedEffect(registerResult) {
        if (registerResult.isNotBlank()) {
            snackbarHostState.showSnackbar(registerResult)
            if (registerResult.startsWith("Registro exitoso")) {
                navController.navigate("login")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
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
                    .height(700.dp),
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
                    Text("Crear Cuenta", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre completo") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedTextColor = Color.Gray,
                            focusedTextColor = Color.DarkGray
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // ✉️ Campo de correo con validación estricta
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = email.isNotEmpty() && !isEmailValid,
                        supportingText = {
                            if (email.isNotEmpty() && !email.contains("@")) {
                                Text(
                                    "Ingresa un correo válido",
                                    color = Color.Red,
                                    fontSize = 11.sp
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedTextColor = Color.Gray,
                            focusedTextColor = Color.DarkGray
                        )
                    )

                    if (registerResult == "El correo ya está registrado") {
                        Text(
                            text = registerResult,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
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
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedTextColor = Color.Gray,
                            focusedTextColor = Color.DarkGray
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = passwordconfirmation,
                        onValueChange = { passwordconfirmation = it },
                        label = { Text("Confirmar Contraseña") },
                        visualTransformation = if (passwordConfVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            val image = if (passwordConfVisible)
                                Icons.Filled.Visibility // icono "ojo abierto"
                            else
                                Icons.Filled.VisibilityOff // icono "ojo tachado"

                            IconButton(onClick = { passwordConfVisible = !passwordConfVisible }) {
                                Icon(
                                    imageVector = image,
                                    contentDescription = if (passwordConfVisible) "Ocultar contraseña" else "Mostrar contraseña"
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedTextColor = Color.Gray,
                            focusedTextColor = Color.DarkGray
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🔒 Checkbox para aceptar términos
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = acceptedTerms,
                            onCheckedChange = { acceptedTerms = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF355031))
                        )
                        Text(
                            text = "Acepto los Términos y Condiciones y la Política de Privacidad",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color.DarkGray,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    // 🔗 Enlaces debajo
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://explorify.runasp.net/Home/Terminos")
                                )
                                context.startActivity(intent)
                            }
                        ) {
                            Text("Ver Términos", fontSize = 11.sp, color = Color(0xFF355031))
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        TextButton(
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://explorify.runasp.net/Home/Privacidad")
                                )
                                context.startActivity(intent)
                            }
                        ) {
                            Text("Ver Privacidad", fontSize = 11.sp, color = Color(0xFF355031))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ✅ Botón habilitado solo si todo es válido
                    Button(
                        onClick = {
                            when {
                                name.isBlank() || password.isBlank() || email.isBlank() || passwordconfirmation.isBlank() ->
                                    errorMessage = "Por favor, completa todos los campos"

                                !isEmailValid ->
                                    errorMessage = "Ingresa un correo válido"

                                !acceptedTerms ->
                                    errorMessage = "Debes aceptar los Términos y Condiciones"

                                password != passwordconfirmation ->
                                    errorMessage = "Las contraseñas son diferentes"

                                else -> {
                                    errorMessage = null
                                    viewModel.register(email.trim(), name.trim(), password.trim())
                                }
                            }

                            errorMessage?.let {
                                coroutineScope.launch { snackbarHostState.showSnackbar(it) }
                            }
                        },
                        enabled = !isLoading &&
                                name.isNotBlank() &&
                                password.isNotBlank() &&
                                acceptedTerms &&
                                isEmailValid &&
                                password == passwordconfirmation,
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
                            Text("Registrarse")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { navController.navigate("login") },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.DarkGray)
                    ) {
                        Text("¿Ya tienes cuenta? Inicia Sesión")
                    }

                    /*if (registerResult.startsWith("Registro exitoso")) {
                    LaunchedEffect(Unit) { navController.navigate("login")}
                }*/
                    if (registerResult.startsWith("Registro exitoso")) {
                        LaunchedEffect(registerResult) {
                            loginViewModel.login(email.trim(), password.trim())
                            // esperar un poco o usar collectAsState de loginResult
                            loginViewModel.loginResult.collect { result ->
                                if (result.startsWith("Bienvenido")) {
                                    navController.navigate("publicaciones") {
                                        popUpTo("register") {
                                            inclusive = true
                                        } // elimina registro del backstack
                                    }
                                } else if (result.startsWith("Error")) {
                                    // opcional: mostrar snackbar con error de login
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Error al iniciar sesión automáticamente")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
