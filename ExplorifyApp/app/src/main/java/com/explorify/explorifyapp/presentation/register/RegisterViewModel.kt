package com.explorify.explorifyapp.presentation.register


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.explorify.explorifyapp.data.remote.dto.RegisterRequest
import com.explorify.explorifyapp.data.remote.auth.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class RegisterViewModel : ViewModel() {
    private val _registerResult = MutableStateFlow<String>("")
    val registerResult: StateFlow<String> = _registerResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun register(email: String, name: String, password: String) {
        if (_isLoading.value) return
        viewModelScope.launch {
            try {
                _isLoading.value = true
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