package com.explorify.explorifyapp.presentation.login

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.material3.CircularProgressIndicator
import androidx.lifecycle.viewmodel.compose.viewModel
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun SplashScreen(navController: NavController, viewModel: LoginViewModel = viewModel()) {
    /*LaunchedEffect(Unit) {
        val isLoggedIn = viewModel.isLoggedIn()
        val userName=viewModel.userName
        val role=viewModel.getSavedRole()
        Log.d("SplashScreen", "isLoggedIn: $isLoggedIn, userName: $userName")
        delay(1000) // solo para dar tiempo a mostrar el splash

        if (isLoggedIn && !userName.isNullOrEmpty()) {
            when (role) {
                "ADMIN" -> {
                    navController.navigate("adminDashboard") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
                "USER" -> {  //
                    navController.navigate("publicaciones") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
                else -> { // si no hay rol o es desconocidoinicio/${userName}
                    navController.navigate("publicaciones") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }
        } else {
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }
    */

    val userData by viewModel.userData.collectAsState()

    // Lanza la carga de Room al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadUserData()
    }
    Log.d("userData:","${userData}")
    // Este efecto reacciona cuando userData deja de ser null
    LaunchedEffect(userData) {  //
         delay(500)
        if (userData != null) {
           // opcional, solo para mostrar el splash

            val role = userData?.role
            val userName = userData?.username ?: ""
            Log.d("rol:","${role}")
            when (role) {
                "ADMIN" -> { //adminDashboard
                    navController.navigate("publicaciones") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
                "USER" -> {
                    navController.navigate("publicaciones") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
                else -> {
                    // sin rol → login
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
        }
        if(userData==null){
            navController.navigate("login") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
