package com.example.explorifyapp.presentation.login

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


@Composable
fun SplashScreen(navController: NavController, viewModel: LoginViewModel = viewModel()) {
    LaunchedEffect(Unit) {
        val isLoggedIn = viewModel.isLoggedIn()
        val userName=viewModel.userName
        Log.d("SplashScreen", "isLoggedIn: $isLoggedIn, userName: $userName")
        delay(1000) // solo para dar tiempo a mostrar el splash

        if (isLoggedIn && !userName.isNullOrEmpty()) {
            navController.navigate("inicio/${viewModel.userName}") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
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
