package com.example.explorifyapp.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.example.explorifyapp.presentation.login.LoginScreen
import androidx.navigation.navArgument
import com.example.explorifyapp.presentation.inicio.HomeScreen
import com.example.explorifyapp.presentation.register.RegisterScreen
import com.example.explorifyapp.presentation.buscar.BuscarScreen
import com.example.explorifyapp.presentation.perfil.PerfilScreen
import com.example.explorifyapp.presentation.login.SplashScreen
import com.example.explorifyapp.presentation.publicaciones.MyPublicationsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") {
            SplashScreen(navController)
        }

        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("register") {
            RegisterScreen(navController = navController)
        }

        composable(
            "inicio/{userName}",
            arguments = listOf(navArgument("userName") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("userName") ?: ""
            HomeScreen(userName = userName,navController= navController)
        }


        composable("perfil") {
            PerfilScreen( navController = navController)
        }
        composable("buscar") {
            BuscarScreen(navController = navController)
        }

        composable("mispublicaciones"){
            MyPublicationsScreen(navController =navController)
        }

    }
}