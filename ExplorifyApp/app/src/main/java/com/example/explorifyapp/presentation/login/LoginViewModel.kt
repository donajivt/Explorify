package com.example.explorifyapp.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explorifyapp.domain.repository.LoginRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val repository = LoginRepository()

    private val _loginResult = MutableStateFlow<String>("")
    val loginResult: StateFlow<String> = _loginResult

    var token: String? = null
        private set

    var userName: String = ""
        private set

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val response = repository.login(username, password)
                if (response.isSuccess && response.result != null) {
                    val user = response.result.user
                    userName = response.result.user.name
                    token = response.result.token
                    //Aqui utilizar el navigation para entrar a la pagina inicial
                    _loginResult.value = "Bienvenido ${user.name} (${user.email})\nToken: ${token?.take(20)}..."
                } else {
                    _loginResult.value = "Error: ${response.message ?: "No se pudo iniciar sesión"}"
                }
            } catch (e: Exception) {
                _loginResult.value = "Error de red: ${e.localizedMessage}"
            }
        }
    }
}