package com.explorify.explorifyapp.presentation.admin.perfil

import android.util.Log
import com.explorify.explorifyapp.domain.repository.UserRepository
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import com.explorify.explorifyapp.data.remote.dto.users.User
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import com.explorify.explorifyapp.domain.repository.PublicationRepository
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.explorify.explorifyapp.data.remote.dto.publications.EmailData
//@HiltViewModel

class PerfilUsuarioViewModel /*@Inject constructor*/(
        private val userRepository: UserRepository,
        private val publicationsRepository: PublicationRepository
    ) : ViewModel() {

        private val _user = MutableStateFlow<User?>(null)
        val user: StateFlow<User?> = _user

        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading

        private val _errorMessage = MutableStateFlow<String?>(null)
        val errorMessage: StateFlow<String?> = _errorMessage

    private val _emailResult = MutableLiveData<String>()
    val emailResult: LiveData<String> = _emailResult

        fun getUserById(token: String, userId: String) {
            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    val response = userRepository.getUserById(token, userId)
                    if (response.isSuccessful) {
                        _user.value = response.body()?.result // depende de tu UserResponse
                        Log.d("successful","${_user.value}")
                    } else {

                        _errorMessage.value = "Error ${response.code()}: ${response.message()}"
                        Log.d("error","${_errorMessage.value}")
                    }
                } catch (e: Exception) {
                    _errorMessage.value = e.message ?: "Error desconocido"
                    Log.e("EXCEPTION", "Error en getUserById", e)
                } finally {
                    _isLoading.value = false
                }
            }
        }

        fun deleteAccount(token: String, userId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d("try de delete","${token} + ${userId}")
                val response = userRepository.deleteUserById(token, userId)

                if (response.isSuccessful) {
                    onResult(true, "Cuenta eliminada")
                } else {
                    onResult(false, "Error: ${response.code()}")
                    Log.d("error en delete","${response}")
                }

            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
                Log.d("error en exception delete","${e.message}")
            }
        }
    }

        fun sendEmail(to: String, subject: String, body: String) {
        viewModelScope.launch {
            try {
                val emailData = EmailData(to = to, subject = subject, body = body)
                val response = publicationsRepository.sendEmail(emailData)

                if (response.isSuccessful) {
                    _emailResult.value = "Correo enviado correctamente"
                    Log.d("EMAIL", "Correo enviado: ${response.body()}")
                } else {
                    _emailResult.value = "Error: ${response.code()}"
                    Log.e("EMAIL", "Error ${response.code()} - ${response.message()}")
                }

            } catch (e: Exception) {
                _emailResult.value = "Excepción: ${e.message}"
                Log.e("EMAIL", "Excepción: ${e.message}")
            }
        }
    }
}
