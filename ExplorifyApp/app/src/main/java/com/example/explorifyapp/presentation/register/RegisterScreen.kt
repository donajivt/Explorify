package com.example.explorifyapp.presentation.register

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.explorifyapp.R
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavController) {
    val viewModel: RegisterViewModel = viewModel()

    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var acceptedTerms by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val registerResult by viewModel.registerResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize(),
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

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedTextColor = Color.Gray,
                        focusedTextColor = Color.DarkGray
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedTextColor = Color.Gray,
                        focusedTextColor = Color.DarkGray
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 🔒 Checkbox para aceptar términos
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = acceptedTerms,
                        onCheckedChange = { acceptedTerms = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF355031)
                        )
                    )
                    Text(
                        text = "Acepto los Términos y Condiciones y la Política de Privacidad",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color.DarkGray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // 🔗 Enlaces debajo (más limpios visualmente)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = {
                            // TODO: 🔗 Cambia por tu URL real de Términos y Condiciones
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://explorify2.somee.com/Home/Terminos"))
                            context.startActivity(intent)
                        }
                    ) {
                        Text("Ver Términos", fontSize = 11.sp, color = Color(0xFF355031))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = {
                            // TODO: 🔗 Cambia por tu URL real de Política de Privacidad
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://explorify2.somee.com/Home/Privacidad"))
                            context.startActivity(intent)
                        }
                    ) {
                        Text("Ver Privacidad", fontSize = 11.sp, color = Color(0xFF355031))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (name.isBlank() || password.isBlank() || email.isBlank()) {
                            errorMessage = "Por favor, completa todos los campos"
                        } else if (!acceptedTerms) {
                            errorMessage = "Debes aceptar los Términos y Condiciones"
                        } else {
                            errorMessage = null
                            viewModel.register(email, name, password)
                        }

                        errorMessage?.let {
                            coroutineScope.launch { snackbarHostState.showSnackbar(it) }
                        }
                    },
                    enabled = !isLoading && name.isNotBlank() && password.isNotBlank() && email.isNotBlank() &&  acceptedTerms,
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
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.DarkGray
                    )
                ) {
                    Text("¿Ya tienes cuenta? Inicia Sesión")
                }

                if (registerResult.startsWith("Registro exitoso")) {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
        }
    }
}
