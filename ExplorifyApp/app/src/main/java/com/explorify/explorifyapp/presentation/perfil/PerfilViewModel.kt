package com.explorify.explorifyapp.presentation.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.explorify.explorifyapp.domain.repository.UserRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import com.explorify.explorifyapp.data.remote.dto.users.UserRequest

class PerfilViewModel(
    private val repository: UserRepository
) : ViewModel() {

    fun deleteAccount(token: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.deleteCurrentUser(token)
                if (response.isSuccessful) {
                    onResult(true, "Cuenta eliminada correctamente")
                } else {
                    onResult(false, "Error: ${response.code()}")
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
        userRequest: UserRequest,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = repository.editCurrentUser(token, userRequest)
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
}
