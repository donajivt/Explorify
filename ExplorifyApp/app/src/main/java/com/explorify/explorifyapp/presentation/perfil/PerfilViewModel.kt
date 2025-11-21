package com.explorify.explorifyapp.presentation.perfil

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.explorify.explorifyapp.data.remote.dto.users.User
import com.explorify.explorifyapp.domain.repository.UserRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.io.File
import com.explorify.explorifyapp.data.remote.dto.users.UserRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.explorify.explorifyapp.data.remote.dto.users.Emails

class PerfilViewModel(
    private val repository: UserRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun deleteAccount(token: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.deleteCurrentUser(token)
                if (response.isSuccessful) {
                    onResult(true, "Cuenta eliminada correctamente")
                } else {
                    onResult(false, "Error de deleteAccount: ${response.message()}")
                }
            } catch (e: IOException) {
                onResult(false, "Error de conexión")
            } catch (e: HttpException) {
                onResult(false, "Error del servidor: ${e.message}")
            } catch (e: Exception) {
                onResult(false, "Error inesperado: ${e.message}")
            }
        }
    }

    // 🔹 Actualizar datos del usuario
    fun updateUser(
        token: String,
        username: String,
        email: String,
        newImageFile: File?,
        //userRequest: UserRequest,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                /*
                     val response = if (newImageFile != null) {
                //  Caso 1: Subir imagen nueva (multipart)
                repository.editCurrentUser(
                    token,
                    username,
                    email,
                    newImageFile
                )
            } else {
                //  Caso 2: Mantener imagen actual (no enviamos archivo)
                repository.editCurrentUserWithoutImage(
                    token,
                    username,
                    email
                )
            }
                */
                //val response = repository.editCurrentUser(token, userRequest)
                val response = repository.editCurrentUser(token, username, email, newImageFile)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    onResult(true, response.body()?.result ?: "Perfil actualizado correctamente")
                } else {
                    onResult(false, "Error al actualizar: ${response.code()}")
                }
            } catch (e: IOException) {
                onResult(false, "Error de red")
            } catch (e: HttpException) {
                onResult(false, "Error del servidor")
            } catch (e: Exception) {
                onResult(false, "Error inesperado: ${e.message}")
            }
        }
    }

    fun getUserById(token: String, userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = repository.getUserById(token, userId)
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

    fun changePassword(token: String, oldPass: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.changePassword(token, Emails(oldPass, newPass))
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    onResult(true, "Contraseña actualizada correctamente")
                } else {
                    onResult(false, response.body()?.message ?: "Error al cambiar la contraseña")
                }
            } catch (e: Exception) {
                onResult(false, "Error inesperado: ${e.message}")
            }
        }
    }

}
