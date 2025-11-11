package com.explorify.explorifyapp.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.explorify.explorifyapp.presentation.login.LoginScreen
import androidx.navigation.navArgument
import com.explorify.explorifyapp.presentation.inicio.HomeScreen
import com.explorify.explorifyapp.presentation.register.RegisterScreen
import com.explorify.explorifyapp.presentation.buscar.BuscarScreen
import com.explorify.explorifyapp.presentation.perfil.PerfilScreen
import com.explorify.explorifyapp.presentation.login.SplashScreen
import com.explorify.explorifyapp.presentation.publicaciones.MyPublicationsScreen
import com.explorify.explorifyapp.presentation.admin.AdminDashboard
import com.explorify.explorifyapp.presentation.perfil.EditProfileScreen
import androidx.compose.runtime.remember
import com.explorify.explorifyapp.data.remote.publications.RetrofitPublicationsInstance
import com.explorify.explorifyapp.domain.repository.PublicationRepositoryImpl
import com.explorify.explorifyapp.domain.usecase.publications.PublicationUseCases
import com.explorify.explorifyapp.presentation.admin.listUsers.UserListScreen
import com.explorify.explorifyapp.presentation.publications.list.PublicationsListModel
import com.explorify.explorifyapp.presentation.main.MainScaffold
import com.explorify.explorifyapp.presentation.publications.list.screens.EditPublicationScreen
import com.explorify.explorifyapp.presentation.publications.list.screens.PublicationListScreen
import com.explorify.explorifyapp.presentation.publications.list.CreatePublicationViewModel
import com.explorify.explorifyapp.presentation.publications.list.screens.CreatePublicationScreen
import com.explorify.explorifyapp.presentation.publications.list.screens.MapPickerScreen
import com.explorify.explorifyapp.presentation.publications.list.screens.PublicationMapScreen
import com.explorify.explorifyapp.presentation.admin.perfil.PerfilAdminScreen

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
            AdminDashboard( vm = publicationsVM,
                navController = navController,
                onOpenDetail = { /* luego se implementa detalle */
                })
        }

        composable("perfilAdmin"){
            PerfilAdminScreen( navController = navController)
        }

        composable("userlist"){
            UserListScreen( navController = navController)
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
                navController = navController,
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

        composable(
            "map/{lat}/{lon}/{name}",
            arguments = listOf(
                navArgument("lat") { type = NavType.StringType },
                navArgument("lon") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType }
            )
        ) { backStack ->
            val lat = backStack.arguments?.getString("lat") ?: "0.0"
            val lon = backStack.arguments?.getString("lon") ?: "0.0"
            val name = backStack.arguments?.getString("name") ?: "Ubicación desconocida"
            PublicationMapScreen(latitud = lat, longitud = lon, locationName = name)
        }

        composable("map_picker") {
            MapPickerScreen(navController = navController)
        }

    }
}