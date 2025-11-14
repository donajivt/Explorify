package com.explorify.explorifyapp.presentation.publications.list.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.explorify.explorifyapp.data.remote.publications.RetrofitPublicationsInstance
import com.explorify.explorifyapp.domain.repository.PublicationRepositoryImpl
import com.explorify.explorifyapp.presentation.login.LoginViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import com.explorify.explorifyapp.domain.repository.MediaRepositoryImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.explorify.explorifyapp.data.remote.publications.MediaApi
import com.explorify.explorifyapp.data.remote.publications.prepareFilePart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPublicationScreen(
    publicationId: String,
    navController: NavController,
    token: String,
    userId: String,
    loginViewModel: LoginViewModel = viewModel()
) {
    val repo = remember { PublicationRepositoryImpl(RetrofitPublicationsInstance.api) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var latitud by remember { mutableStateOf<String?>(null) }
    var longitud by remember { mutableStateOf<String?>(null) }
    var imageUrl by rememberSaveable { mutableStateOf("") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadedImageUrl by remember { mutableStateOf<String?>(null) }
    var publicId by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    var hasLoadedOnce by rememberSaveable { mutableStateOf(false) }

    val mediaRepo = remember {
        MediaRepositoryImpl(
            Retrofit.Builder()
                .baseUrl("http://explorify.somee.com/") // ⚠️ HTTPS!
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MediaApi::class.java)
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            coroutineScope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar("Debes permitir acceso a la galería")
            }
        }
    }

    // 🚀 Abrir cámara
    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) cameraImageUri.value?.let { selectedImageUri = it }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val photoFile = java.io.File(
                navController.context.getExternalFilesDir(null),
                "photo_${System.currentTimeMillis()}.jpg"
            )
            val uri = androidx.core.content.FileProvider.getUriForFile(
                navController.context,
                "${navController.context.packageName}.provider",
                photoFile
            )
            cameraImageUri.value = uri
            cameraLauncher.launch(uri)
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Permiso de cámara denegado")
            }
        }
    }


    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> uri?.let { selectedImageUri = it } }
    )

    LaunchedEffect(Unit) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        permissionLauncher.launch(permission)
    }


    // 🔹 Cargar datos solo una vez
    LaunchedEffect(publicationId) {
        if (!hasLoadedOnce && token.isNotEmpty() && userId.isNotEmpty()) {
            hasLoadedOnce = true
            isLoading = true
            try {
                val pub = repo.getUserPublications(userId, token).find { it.id == publicationId }
                pub?.let {
                    title = it.title
                    description = it.description
                    location = it.location
                    latitud = it.latitud
                    longitud = it.longitud
                    imageUrl = it.imageUrl
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Error al cargar la publicación")
            } finally {
                isLoading = false
            }
        }
    }

    // 🔁 Escuchar cambios del mapa (cuando regresa de MapPickerScreen)
    LaunchedEffect(Unit) {
        val handle = navController.currentBackStackEntry?.savedStateHandle
        handle?.getLiveData<String>("selected_location_name")?.observeForever { name ->
            val lat = handle.get<String>("selected_latitude")
            val lon = handle.get<String>("selected_longitude")

            if (!name.isNullOrEmpty() && !lat.isNullOrEmpty() && !lon.isNullOrEmpty()) {
                location = name
                latitud = lat
                longitud = lon
                handle.remove<String>("selected_location_name")
                handle.remove<String>("selected_latitude")
                handle.remove<String>("selected_longitude")
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { focusManager.clearFocus() },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Editar publicación",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color(0xFF2E473B),
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color(0xFF3C9D6D)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF2F0EC)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        if (isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF3C9D6D))
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(
                                Color(0xFFF6F4EF),
                                Color(0xFFDDF5E3)
                            )
                        )
                    )
                    .padding(padding)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {

                // 🖼️ Imagen previa
                Card(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(MaterialTheme.shapes.large)
                        .clickable { showBottomSheet = true },
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F0EC))
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            when {
                                selectedImageUri != null -> selectedImageUri
                                imageUrl.isNotEmpty() -> imageUrl
                                else -> "https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885__480.jpg"
                            }
                        ),
                        contentDescription = "Vista previa de imagen",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

// 🧭 Modal inferior con opciones
                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
                        containerColor = Color.White,
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Seleccionar imagen", fontWeight = FontWeight.Bold, color = Color(0xFF2E473B))
                            Spacer(Modifier.height(12.dp))
                            Divider()
                            Spacer(Modifier.height(12.dp))

                            TextButton(onClick = {
                                showBottomSheet = false
                                val permission = Manifest.permission.CAMERA
                                if (ContextCompat.checkSelfPermission(navController.context, permission)
                                    == PackageManager.PERMISSION_GRANTED
                                ) {
                                    val photoFile = java.io.File(
                                        navController.context.getExternalFilesDir(null),
                                        "photo_${System.currentTimeMillis()}.jpg"
                                    )
                                    val uri = androidx.core.content.FileProvider.getUriForFile(
                                        navController.context,
                                        "${navController.context.packageName}.provider",
                                        photoFile
                                    )
                                    cameraImageUri.value = uri
                                    cameraLauncher.launch(uri)
                                } else {
                                    cameraPermissionLauncher.launch(permission)
                                }
                            }) {
                                Text("📸 Tomar foto", color = Color(0xFF3C9D6D), fontWeight = FontWeight.SemiBold)
                            }

                            Spacer(Modifier.height(8.dp))

                            TextButton(onClick = {
                                showBottomSheet = false
                                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                                    Manifest.permission.READ_MEDIA_IMAGES
                                else Manifest.permission.READ_EXTERNAL_STORAGE
                                if (ContextCompat.checkSelfPermission(navController.context, permission)
                                    == PackageManager.PERMISSION_GRANTED
                                ) {
                                    imagePickerLauncher.launch("image/*")
                                } else {
                                    permissionLauncher.launch(permission)
                                }
                            }) {
                                Text("🖼️ Elegir desde galería", color = Color(0xFF3C9D6D), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Campos
                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 50) title = it },
                    label = { Text("Título") },
                    supportingText = { Text("${title.length}/50", color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFDDF5E3),
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color(0xFF2B2B2B),
                        unfocusedTextColor = Color(0xFF2B2B2B),
                        cursorColor = Color(0xFF3C9D6D),
                        focusedBorderColor = Color(0xFF3C9D6D),
                        unfocusedBorderColor = Color(0xFFBFAE94),
                        focusedLabelColor = Color(0xFF3C9D6D),
                        unfocusedLabelColor = Color(0xFF6B4F3B)
                    )
                )

                Spacer(Modifier.height(14.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= 200) description = it },
                    label = { Text("Descripción") },
                    supportingText = { Text("${description.length}/200", color = Color.Gray) },
                    maxLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFDDF5E3),
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color(0xFF2B2B2B),
                        unfocusedTextColor = Color(0xFF2B2B2B),
                        cursorColor = Color(0xFF3C9D6D),
                        focusedBorderColor = Color(0xFF3C9D6D),
                        unfocusedBorderColor = Color(0xFFBFAE94),
                        focusedLabelColor = Color(0xFF3C9D6D),
                        unfocusedLabelColor = Color(0xFF6B4F3B)
                    )
                )

                Spacer(Modifier.height(14.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = {},
                    label = { Text("Ubicación") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = if (location.isNotBlank()) Color(0xFF3C9D6D) else Color(
                                0xFF6B4F3B
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("map_picker") },
                    enabled = false,
                    readOnly = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (location.isNotBlank()) Color(0xFFDDF5E3) else Color.White,
                        unfocusedContainerColor = if (location.isNotBlank()) Color(0xFFDDF5E3) else Color.White,
                        disabledContainerColor = if (location.isNotBlank()) Color(0xFFDDF5E3) else Color.White,
                        focusedTextColor = Color(0xFF2B2B2B),
                        unfocusedTextColor = Color(0xFF2B2B2B),
                        disabledTextColor = Color(0xFF2B2B2B),
                        cursorColor = Color(0xFF3C9D6D),
                        focusedBorderColor = Color(0xFF3C9D6D),
                        unfocusedBorderColor = Color(0xFFBFAE94),
                        disabledBorderColor = Color(0xFFBFAE94),
                        focusedLabelColor = Color(0xFF3C9D6D),
                        unfocusedLabelColor = Color(0xFF6B4F3B),
                        disabledLabelColor = Color(0xFF6B4F3B)
                    )
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                var finalImageUrl = imageUrl

                                if (selectedImageUri != null) {
                                    isUploading = true
                                    snackbarHostState.showSnackbar("⏳ Subiendo nueva imagen...")

                                    val filePart = navController.context.prepareFilePart("file", selectedImageUri!!)
                                    val uploadResult = retrySuspend(times = 3) {
                                        mediaRepo.uploadImage(token, filePart)
                                    }

                                    val uploadedUrl = uploadResult.result?.secureUrl
                                    if (!uploadedUrl.isNullOrEmpty()) {
                                        finalImageUrl = uploadedUrl
                                        uploadedImageUrl = uploadedUrl

                                        publicId?.let {
                                            mediaRepo.deleteImage(token, it)
                                        }

                                        snackbarHostState.showSnackbar("Imagen actualizada correctamente")
                                    } else {
                                        snackbarHostState.showSnackbar("Error al subir la imagen")
                                    }

                                    isUploading = false
                                }

                                isSaving = true
                                repo.update(
                                    id = publicationId,
                                    imageUrl = finalImageUrl ?: imageUrl,
                                    title = title,
                                    description = description,
                                    location = location,
                                    latitud = latitud,
                                    longitud = longitud,
                                    userId = userId,
                                    token = token
                                )
                                snackbarHostState.showSnackbar("Publicación actualizada correctamente")
                                delay(1000)
                                navController.popBackStack()
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Error al actualizar publicación")
                            } finally {
                                isSaving = false
                                isUploading = false
                            }
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(MaterialTheme.shapes.large),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C9D6D))
                ) {
                    if (isSaving || isUploading)
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    else
                        Text(
                            "Guardar cambios",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                }
            }
        }
    }
}