package com.explorify.explorifyapp.presentation.register


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.explorify.explorifyapp.data.remote.dto.RegisterRequest
import com.explorify.explorifyapp.data.remote.auth.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.explorify.explorifyapp.data.remote.dto.User
import android.util.Log
import kotlinx.coroutines.delay

class RegisterViewModel : ViewModel() {
    private val _registerResult = MutableStateFlow<String>("")
    val registerResult: StateFlow<String> = _registerResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _users = MutableStateFlow<List<User>>(emptyList())

    fun getUsers() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getUsers()
                if (response.isSuccess) {
                    _users.value = response.result
                    Log.d("RegisterViewModel", "Usuarios cargados: ${_users.value.size}")
                } else {
                    Log.e("RegisterViewModel", "Error al obtener usuarios: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Error de red al traer usuarios: ${e.localizedMessage}")
            }
        }
    }

    // ✅ 2. Verificar si un correo ya existe
    suspend fun isEmailUsed(email: String): Boolean {
        if (_users.value.isEmpty()) {
            getUsers()
            delay(800) // pequeña espera para asegurar que la lista se haya cargado
        }
        return _users.value.any { it.email.equals(email.trim(), ignoreCase = true) }
    }


    fun register(email: String, name: String, password: String) {
        if (_isLoading.value) return
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val emailExists = isEmailUsed(email)
                if (emailExists) {
                    _registerResult.value = "El correo ya está registrado"
                    _isLoading.value = false
                    return@launch
                }

                val request = RegisterRequest(email, name, password)
                val response = RetrofitInstance.api.register(request)

                if (response.isSuccess) {
                    _registerResult.value = "Registro exitoso. ¡Inicia sesión!"
                } else {
                    _registerResult.value = "Error: ${response.message ?: "No se pudo registrar"}"
                }
            } catch (e: Exception) {
                _registerResult.value = "Error de red: ${e.localizedMessage}"
            }finally {
                _isLoading.value = false // ← vuelve a false cuando termina
            }
        }
    }
}