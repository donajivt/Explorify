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
import com.example.explorifyapp.presentation.admin.AdminDashboard
import com.example.explorifyapp.presentation.perfil.EditProfileScreen
import androidx.compose.runtime.remember
import com.example.explorifyapp.data.remote.publications.RetrofitPublicationsInstance
import com.example.explorifyapp.domain.repository.PublicationRepositoryImpl
import com.example.explorifyapp.domain.usecase.publications.PublicationUseCases
import com.example.explorifyapp.presentation.publications.list.PublicationsListModel
import com.example.explorifyapp.presentation.main.MainScaffold
import com.example.explorifyapp.presentation.publications.list.screens.EditPublicationScreen
import com.example.explorifyapp.presentation.publications.list.screens.PublicationListScreen
import com.example.explorifyapp.presentation.publications.list.CreatePublicationViewModel
import com.example.explorifyapp.presentation.publications.list.screens.CreatePublicationScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // ✅ Inicialización de capa de datos para publicaciones
    val api = remember { RetrofitPublicationsInstance.api }
    val repo = remember { PublicationRepositoryImpl(api) }

    // ✅ Use cases
    val getAllUC = remember { PublicationUseCases.GetPublicationsUseCase(repo) }
    val getByIdUC = remember { PublicationUseCases.GetPublicationByIdUseCase(repo) }
    val deleteUC = remember { PublicationUseCases.DeletePublicationUseCase(repo) }

    // ✅ ViewModel compartido para la lista
    val publicationsVM = remember { PublicationsListModel(getAllUC, getByIdUC, deleteUC) }

    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") {
            SplashScreen(navController)
        }

        composable("main") {
            MainScaffold(parentNavController = navController)
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

        composable("adminDashboard") {
            AdminDashboard( navController = navController)
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

        composable("publicaciones") {
            PublicationListScreen(
                vm = publicationsVM,
                navController = navController,
                onCreateClick = { userId ->
                    navController.navigate("crear_publicacion/$userId")
                },
                onOpenDetail = { /* luego se implementa detalle */ }
            )
        }

        composable(
            "crear_publicacion/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""

            val createVM = remember { CreatePublicationViewModel(repo) }

            CreatePublicationScreen(
                vm = createVM,
                onBack = { navController.popBackStack() },
                onPublishDone = { navController.popBackStack() },
                userId = userId
            )
        }

        composable(
            "editar_publicacion/{id}?token={token}&userId={userId}",
            arguments = listOf(navArgument("id") { type = NavType.StringType },
                navArgument("token") { type = NavType.StringType; defaultValue = "" },
                navArgument("userId") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val pubId = backStackEntry.arguments?.getString("id") ?: ""
            val token = backStackEntry.arguments?.getString("token") ?: ""
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            EditPublicationScreen(
                publicationId = pubId,
                navController = navController,
                token = token,
                userId = userId
            )
        }

        composable("editprofile"){
            EditProfileScreen(navController =navController)
        }

    }
}