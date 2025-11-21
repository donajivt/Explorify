package com.explorify.explorifyapp.presentation.admin.report

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CardDefaults
import androidx.room.util.TableInfo
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.explorify.explorifyapp.data.remote.room.AppDatabase
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Delete
import android.util.Log
@ExperimentalMaterial3Api
@Composable
fun ReportListScreen(
    vm: ReportViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val items = vm.reportedItems
    val loading = vm.loading
    val error = vm.error
    var token by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        token = AppDatabase.getInstance(context).authTokenDao().getToken()?.token
        token?.let { tk -> vm.load(tk) }
    // vm.load(token)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Publicaciones reportadas") }) }
                ,bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = true,
                    onClick = { navController.navigate("adminDashboard") }
                )
                /* NavigationBarItem(
                     icon = { Icon(Icons.Default.BarChart, contentDescription = "Buscar") },
                     label = { Text("Estadisticas") },
                     selected = false,
                     onClick = { } //navController.navigate("buscar")
                 )*/
                NavigationBarItem(
                    icon = { Icon(Icons.Default.BorderColor, contentDescription = "Buscar") },
                    label = { Text("Reportes") },
                    selected = false,
                    onClick = { navController.navigate("reportes") } //
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = false,
                    onClick = { navController.navigate("perfilAdmin")}//
                )
            }
        }
    ) { padding ->

        when {
            loading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Text("Error: $error", color = Color.Red)
            }

            /*items.isEmpty() -> {
                Text("No hay publicaciones reportadas", Modifier.padding(20.dp))
            }*/

            else -> {
                if (items.isEmpty()) {
                    Text(
                        "No hay publicaciones reportadas válidas (todas fueron eliminadas)",
                        Modifier.padding(20.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        items(items) { item ->
                            ReportedPublicationCard(
                                item = item,
                                onDelete = {

                                    vm.deletePublication(item.publication, token!!) {
                                        vm.load(token!!)
                                    }
                                }
                            )
                        }
                    }
                }

            }
        }
    }
}


@Composable
fun ReportedPublicationCard(
    item: ReportedPublicationItem,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row( ) {
                Text(item.publication.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { onDelete() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar reporte",
                        tint = Color.Red,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            AsyncImage(
                model = item.publication.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.height(12.dp))

            Text("Total reportes: ${item.reports.size}")

            Text("Reportado por:")
            item.reporterNames.forEach {
                Text("• $it")
            }
              Log.d("reporterNames:","${item.reporterNames}")
            Spacer(Modifier.height(8.dp))

            Text(
                "Último reporte: " + item.reports.maxByOrNull { it.createdAt }?.createdAt.orEmpty(),
                color = Color.Gray
            )

            Spacer(Modifier.height(16.dp))

            /*
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Eliminar publicación", color = Color.White)
            }
            */
        }
    }
}

