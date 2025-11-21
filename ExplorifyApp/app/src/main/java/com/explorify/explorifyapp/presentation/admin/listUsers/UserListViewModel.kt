package com.explorify.explorifyapp.presentation.admin.listUsers

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.explorify.explorifyapp.data.remote.auth.RetrofitInstance
//import com.explorify.explorifyapp.data.remote.dto.User
import com.explorify.explorifyapp.data.remote.users.RetrofitUserInstance
import android.util.Log
import com.explorify.explorifyapp.data.remote.dto.users.User
import com.explorify.explorifyapp.domain.repository.UserRepository

class UserListViewModel(
    private val repository: UserRepository = UserRepository(RetrofitUserInstance.api)
) : ViewModel() {

    private val _users = MutableStateFlow<List<com.explorify.explorifyapp.data.remote.dto.User>>(emptyList())
    val users: StateFlow<List<com.explorify.explorifyapp.data.remote.dto.User>> = _users
    private val _usuarios = MutableStateFlow<List<User>>(emptyList())
    val usuarios: StateFlow<List<User>> = _usuarios
    /*fun getUsers() {
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
        */

    fun getUsers(token: String) {
        viewModelScope.launch {
            try {
                val response = repository.getUsers(token)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _usuarios.value = response.body()!!.result
                    Log.d("Usuarios","${response.body()!!.result}")
                } else {
                    Log.e("UserListVM", "Error: ${response.body()?.message}")
                }

            } catch (e: Exception) {
                Log.e("UserListVM", "Error excepción: ${e.message}")
            }
        }
    }
  /*  fun getUsuarios() {
        viewModelScope.launch {
            try {
                val response = RetrofitUserInstance.api.getAll()
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
*/
}
