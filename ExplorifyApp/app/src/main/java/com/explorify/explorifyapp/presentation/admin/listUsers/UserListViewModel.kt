package com.explorify.explorifyapp.presentation.admin.listUsers

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.explorify.explorifyapp.data.remote.auth.RetrofitInstance
import com.explorify.explorifyapp.data.remote.dto.User
import android.util.Log

class UserListViewModel : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    fun getUsers() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getUsers()
                if (response.isSuccess) {
                    _users.value = response.result
                    Log.d("UserListViewModel", "Usuarios cargados: ${_users.value.size}")
                } else {
                    Log.e("UserListViewModel", "Error al obtener usuarios: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e("UserListViewModel", "Error de red al traer usuarios: ${e.localizedMessage}")
            }
        }
    }
}
