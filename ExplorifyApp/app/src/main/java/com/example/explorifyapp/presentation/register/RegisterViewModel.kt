package com.example.explorifyapp.presentation.register


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explorifyapp.data.remote.dto.RegisterRequest
import com.example.explorifyapp.data.remote.auth.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class RegisterViewModel : ViewModel() {
    private val _registerResult = MutableStateFlow<String>("")
    val registerResult: StateFlow<String> = _registerResult

    fun register(email: String, name: String, password: String) {
        viewModelScope.launch {
            try {
                val request = RegisterRequest(email, name, password)
                val response = RetrofitInstance.api.register(request)

                if (response.isSuccess) {
                    _registerResult.value = "Registro exitoso. ¡Inicia sesión!"
                } else {
                    _registerResult.value = "Error: ${response.message ?: "No se pudo registrar"}"
                }
            } catch (e: Exception) {
                _registerResult.value = "Error de red: ${e.localizedMessage}"
            }
        }
    }
}