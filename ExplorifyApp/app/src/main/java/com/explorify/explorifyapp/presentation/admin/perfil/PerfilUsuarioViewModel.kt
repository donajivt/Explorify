package com.explorify.explorifyapp.presentation.admin.perfil

import com.explorify.explorifyapp.domain.repository.UserRepository
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import com.explorify.explorifyapp.data.remote.dto.User
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow

//@HiltViewModel
class PerfilUsuarioViewModel /*@Inject constructor*/(
        private val userRepository: UserRepository
    ) : ViewModel() {

        private val _user = MutableStateFlow<User?>(null)
        val user: StateFlow<User?> = _user

        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading

        private val _errorMessage = MutableStateFlow<String?>(null)
        val errorMessage: StateFlow<String?> = _errorMessage

        fun getUserById(token: String, userId: String) {
            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    val response = userRepository.getUserById(token, userId)

                    if (response.isSuccessful) {
                        //_user.value = response.body()?.data // depende de tu UserResponse
                    } else {
                        _errorMessage.value = "Error ${response.code()}: ${response.message()}"
                    }
                } catch (e: Exception) {
                    _errorMessage.value = e.message ?: "Error desconocido"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }
