package com.explorify.explorifyapp.presentation.publications.list.screens

import android.net.Uri
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.explorify.explorifyapp.data.remote.room.AppDatabase
import com.explorify.explorifyapp.presentation.publications.list.CreatePublicationViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.platform.LocalFocusManager
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.explorify.explorifyapp.data.remote.publications.prepareFilePart
import com.explorify.explorifyapp.domain.repository.MediaRepositoryImpl
import retrofit2.converter.gson.GsonConverterFactory
import com.explorify.explorifyapp.data.remote.publications.MediaApi
import java.io.File
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import java.io.IOException
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePublicationScreen(
    vm: CreatePublicationViewModel,
    navController: NavController,
    onBack: () -> Unit,
    onPublishDone: () -> Unit,
    userId: String
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var token by remember { mutableStateOf<String?>(null) }
    var userIdState by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(context).authTokenDao()
            token = dao.getToken()?.token
            userIdState = dao.getToken()?.userId
        }
    }

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var latitud by rememberSaveable { mutableStateOf<String?>(null) }
    var longitud by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var uploadedImageUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    val mediaRepo = remember {
        val okHttp = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .callTimeout(25, TimeUnit.SECONDS)
            .dispatcher(
                okhttp3.Dispatcher().apply {
                    maxRequests = 64
                    maxRequestsPerHost = 10
                }
            )
            .build()

        MediaRepositoryImpl(
            Retrofit.Builder()
                .baseUrl("https://explorify-publications.runasp.net/")
                .client(okHttp)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MediaApi::class.java)
        )
    }

    val ui = vm.uiState
    var isPublishing by remember { mutableStateOf(false) }

    LaunchedEffect(navController.currentBackStackEntry?.savedStateHandle?.get<String>("selected_location_name")) {
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
        savedStateHandle?.get<String>("selected_location_name")?.let {
            location = it
            latitud = savedStateHandle.get<String>("selected_latitude")
            longitud = savedStateHandle.get<String>("selected_longitude")
        }
    }

    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) scope.launch { snackbarHostState.showSnackbar("Permiso de galería denegado") }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) cameraImageUri?.let { selectedImageUri = it }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val photoFile = File(context.getExternalFilesDir(null), "photo_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        } else {
            scope.launch { snackbarHostState.showSnackbar("Permiso de cámara denegado") }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> uri?.let { selectedImageUri = it } }
    )

    LaunchedEffect(Unit) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
        galleryPermissionLauncher.launch(permission)
    }



    Scaffold(
        modifier = Modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { focusManager.clearFocus() },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Nueva Aventura",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E473B)
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Volver", tint = Color(0xFF3C9D6D))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF6F4EF)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        if (isPublishing || ui.loading) return@Button

                        scope.launch(Dispatchers.IO) {

                            snackbarHostState.currentSnackbarData?.dismiss()

                            // 🛑 Validaciones iniciales
                            if (selectedImageUri == null) {
                                withContext(Dispatchers.Main) {
                                    snackbarHostState.showSnackbar("Debes seleccionar una imagen antes de publicar")
                                }
                                return@launch
                            }

                            if (description.isBlank() || location.isBlank()) {
                                withContext(Dispatchers.Main) {
                                    snackbarHostState.showSnackbar("Debes llenar descripción y ubicación")
                                }
                                return@launch
                            }

                            if (userIdState.isNullOrEmpty()) {
                                withContext(Dispatchers.Main) {
                                    snackbarHostState.showSnackbar("No se encontró el usuario autenticado")
                                }
                                return@launch
                            }

                            try {

                                // ================================
                                // 📤 SUBIR IMAGEN
                                // ================================
                                isUploading = true
                                withContext(Dispatchers.Main) {
                                    snackbarHostState.showSnackbar("⏳ Subiendo imagen...")
                                }

                                val compressedFile = compressImage(context, selectedImageUri!!)

                                val filePart = context.prepareFilePart("file", Uri.fromFile(compressedFile))


                                val uploadResult = retrySuspend(times = 3) {
                                    mediaRepo.uploadImage(token ?: "", filePart)
                                }

                                // ================================
                                // ✔ OBTENER URL DE CLOUDINARY
                                // ================================
                                val finalImageUrl = uploadResult.result?.secureUrl

                                if (finalImageUrl.isNullOrBlank()) {
                                    withContext(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar("Error: la imagen no se pudo procesar")
                                    }
                                    return@launch
                                }

                                // ================================
                                // 📝 CREAR PUBLICACIÓN
                                // ================================
                                isUploading = false
                                isPublishing = true

                                withContext(Dispatchers.Main) {
                                    snackbarHostState.showSnackbar("📤 Creando publicación...")
                                }

                                vm.createPublication(
                                    context = context,
                                    imageUrl = finalImageUrl,
                                    title = title,
                                    description = description,
                                    location = location,
                                    latitud = latitud,
                                    longitud = longitud,
                                    userId = userIdState!!,
                                    onDone = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Aventura publicada con éxito 🎉")
                                            onPublishDone()
                                        }
                                    }
                                )

                            } catch (e: Exception) {
                                val msg = e.message ?: "Error desconocido"

                                withContext(Dispatchers.Main) {
                                    snackbarHostState.showSnackbar("Error: $msg")
                                }
                            } finally {
                                isUploading = false
                                isPublishing = false
                            }
                        }
                    },
                            enabled = !ui.loading && !isPublishing && !isUploading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C9D6D))
                ) {
                    when {
                        ui.loading || isUploading -> CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )
                        else -> Text(
                            "Publicar aventura",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(Color(0xFFF6F4EF), Color(0xFFDDF5E3))
                    )
                )
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showBottomSheet = true },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                AsyncImage(
                    model = selectedImageUri
                        ?: "https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885__480.jpg",
                    contentDescription = "Vista previa",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Modal con opciones
            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Seleccionar imagen", fontWeight = FontWeight.Bold, color = Color(0xFF2E473B))
                        Spacer(Modifier.height(12.dp))
                        Divider()
                        Spacer(Modifier.height(12.dp))

                        // Opción cámara
                        TextButton(onClick = {
                            showBottomSheet = false
                            val permission = Manifest.permission.CAMERA
                            if (ContextCompat.checkSelfPermission(context, permission)
                                == PackageManager.PERMISSION_GRANTED
                            ) {
                                val photoFile = File(context.getExternalFilesDir(null),
                                    "photo_${System.currentTimeMillis()}.jpg")
                                val uri = FileProvider.getUriForFile(
                                    context, "${context.packageName}.provider", photoFile
                                )
                                cameraImageUri = uri
                                cameraLauncher.launch(uri)
                            } else {
                                cameraPermissionLauncher.launch(permission)
                            }
                        }) {
                            Text("📸 Tomar foto", color = Color(0xFF3C9D6D), fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(Modifier.height(8.dp))

                        // Opción galería
                        TextButton(onClick = {
                            showBottomSheet = false
                            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                                Manifest.permission.READ_MEDIA_IMAGES
                            else Manifest.permission.READ_EXTERNAL_STORAGE
                            if (ContextCompat.checkSelfPermission(context, permission)
                                == PackageManager.PERMISSION_GRANTED
                            ) {
                                imagePickerLauncher.launch("image/*")
                            } else {
                                galleryPermissionLauncher.launch(permission)
                            }
                        }) {
                            Text("🖼️ Elegir desde galería", color = Color(0xFF3C9D6D), fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            Spacer(Modifier.height(26.dp))

            // 🎯 Título
            OutlinedTextField(
                value = title,
                onValueChange = {
                    val clean = sanitizeSafeInput(it)
                    if (clean.length <= 50) title = clean
                },
                label = { Text("Título (opcional)") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color(0xFF3C9D6D))
                },
                supportingText = { Text("${title.length}/50", color = Color.Gray) }, // 🔹 Contador visual
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFDDF5E3),
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF3C9D6D),
                    unfocusedBorderColor = Color(0xFFBFAE94),
                    focusedLabelColor = Color(0xFF3C9D6D),
                    unfocusedLabelColor = Color(0xFF6B4F3B),
                    cursorColor = Color(0xFF3C9D6D),
                    focusedTextColor = Color(0xFF2B2B2B),
                    unfocusedTextColor = Color(0xFF2B2B2B),
                    disabledTextColor = Color(0xFFB0B0B0)
                )
            )

            Spacer(Modifier.height(14.dp))

            // 📝 Descripción
            OutlinedTextField(
                value = description,
                onValueChange = {
                    val clean = sanitizeSafeInput(it)
                    if (clean.length <= 200) description = clean
                },
                label = { Text("Descripción") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color(0xFF3C9D6D))
                },
                supportingText = { Text("${description.length}/200", color = Color.Gray) }, // 🔹 Contador visual
                maxLines = 6,
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFDDF5E3),
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF3C9D6D),
                    unfocusedBorderColor = Color(0xFFBFAE94),
                    focusedLabelColor = Color(0xFF3C9D6D),
                    unfocusedLabelColor = Color(0xFF6B4F3B),
                    cursorColor = Color(0xFF3C9D6D),
                    focusedTextColor = Color(0xFF2B2B2B),
                    unfocusedTextColor = Color(0xFF2B2B2B),
                    disabledTextColor = Color(0xFFB0B0B0)
                )
            )

            Spacer(Modifier.height(14.dp))

            // 📍 Ubicación (con color dinámico)
            OutlinedTextField(
                value = location,
                onValueChange = {},
                label = { Text("Ubicación") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = if (location.isNotBlank()) Color(0xFF3C9D6D) else Color(0xFF6B4F3B)
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
                    focusedLabelColor = if (location.isNotBlank()) Color(0xFF3C9D6D) else Color(0xFF6B4F3B),
                    unfocusedLabelColor = Color(0xFF6B4F3B),
                    disabledLabelColor = Color(0xFF6B4F3B),
                    focusedBorderColor = if (location.isNotBlank()) Color(0xFF3C9D6D) else Color(0xFFBFAE94),
                    unfocusedBorderColor = if (location.isNotBlank()) Color(0xFF3C9D6D) else Color(0xFFBFAE94),
                    disabledBorderColor = if (location.isNotBlank()) Color(0xFF3C9D6D) else Color(0xFFBFAE94)
                )
            )

            Spacer(Modifier.height(30.dp))
        }
    }
}


class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val initialDelayMs: Long = 1500L
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var tryCount = 0
        var lastException: IOException? = null
        var delay = initialDelayMs

        while (tryCount <= maxRetries) {
            try {
                return chain.proceed(chain.request())
            } catch (e: IOException) {
                lastException = e
                if (tryCount == maxRetries) break
                tryCount++
                try { Thread.sleep(delay) } catch (_: InterruptedException) {}
                delay = (delay * 2).coerceAtMost(8000L) // backoff exponencial
            }
        }
        throw lastException ?: IOException("Upload failed with unknown error")
    }
}

suspend fun <T> retrySuspend(
    times: Int = 3,
    initialDelayMs: Long = 1500L,
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelayMs
    repeat(times - 1) {
        try {
            return block()
        } catch (e: Exception) {
            val msg = e.message?.lowercase() ?: ""
            val retryable = e is java.net.SocketTimeoutException ||
                    e is java.io.IOException ||
                    "timeout" in msg || "failed to connect" in msg || "host" in msg
            if (!retryable) throw e
        }
        kotlinx.coroutines.delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(8000L)
    }
    return block()
}

fun sanitizeSafeInput(input: String): String {
    var clean = input

    val forbiddenChars = listOf('<', '>', '/', '\\', '{', '}', '`', '=', '"', '\'')
    forbiddenChars.forEach { char ->
        clean = clean.replace(char.toString(), "")
    }

    clean = clean.replace(Regex("<[^>]*>"), "")
    clean = clean.replace(Regex("on\\w+\\s*=\\s*['\"].*?['\"]", RegexOption.IGNORE_CASE), "")
    clean = clean.replace(Regex("(javascript:|vbscript:|data:)", RegexOption.IGNORE_CASE), "")
    clean = clean.replace(Regex("&#\\d+;"), "")
    clean = clean.replace(Regex("&#x[0-9a-fA-F]+;"), "")
    clean = clean.replace(Regex("[\\u0000-\\u001F\\u007F]"), "")

    val forbiddenWords = listOf(
        "script", "iframe", "object", "embed", "form", "svg",
        "link", "style", "meta", "head", "body", "onerror", "onload"
    )
    forbiddenWords.forEach {
        clean = clean.replace(it, "", ignoreCase = true)
    }

    return clean
}

suspend fun compressImage(context: Context, uri: Uri): File = withContext(Dispatchers.IO) {
    val inputStream = context.contentResolver.openInputStream(uri)
        ?: throw IllegalStateException("No se pudo leer la imagen")

    val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)

    // Reducir resolución a 1080px (suficiente para móvil)
    val scaled = Bitmap.createScaledBitmap(
        bitmap,
        1080,
        (bitmap.height * (1080f / bitmap.width)).toInt(),
        true
    )

    val file = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
    file.outputStream().use { out ->
        scaled.compress(Bitmap.CompressFormat.JPEG, 70, out)
    }

    file
}