package com.example.explorifyapp.presentation.login


import androidx.lifecycle.viewModelScope
import com.example.explorifyapp.domain.repository.LoginRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.explorifyapp.data.remote.auth.decodeJwtPayload
import com.example.explorifyapp.data.remote.room.AuthToken

/*class LoginViewModel : ViewModel() {

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
                    //val user = response.result.user
                    userName = response.result.user.name
                    token = response.result.token
                    //Aqui utilizar el navigation para entrar a la pagina inicial //${user.name} (${user.email})
                    _loginResult.value = "Bienvenido Token: ${token?.take(29)}..."
                    Log.i("Token","${token}");
                } else {
                    _loginResult.value = "Error: ${response.message ?: "No se pudo iniciar sesión"}"
                }
            } catch (e: Exception) {
                _loginResult.value = "Error de red: ${e.localizedMessage}"
            }
        }
    }
}*/

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LoginRepository(application)

    private val _loginResult = MutableStateFlow<String>("")
    val loginResult: StateFlow<String> = _loginResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    var token: String? = null
        private set
    var userName: String = ""
        private set
    var userId: String = ""
        private set
    var userEmail:String=""
        private set
    var role:String? =""
        private set

    fun login(username: String, password: String) {
        if (_isLoading.value) return
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = repository.login(username, password)
                if (response.isSuccess && response.result != null) {
                    userName = response.result.user.name
                    token = response.result.token
                    userId = response.result.user.id
                    userEmail=response.result.user.email
                    if (token != null) {
                        val payload = decodeJwtPayload(token!!)
                        role = payload?.optString("http://schemas.microsoft.com/ws/2008/06/identity/claims/role")
                        // Guardar token en Room
                        repository.saveToken(token!!, userName, userId, userEmail,role!!)
                        // maybe also save role in DB here
                    } else {
                        Log.e("LoginViewModel", "Token was null after login")
                    }
                    //val payload = decodeJwtPayload(token!!)
                    //val role = payload?.optString("http://schemas.microsoft.com/ws/2008/06/identity/claims/role")
                        //
                    //
                    _loginResult.value = "Bienvenido Token: ${token?.take(29)}..."
                } else {
                    _loginResult.value = "Error: ${response.message ?: "No se pudo iniciar sesión"}"
                }
            } catch (e: Exception) {
                _loginResult.value = "Error de red: ${e.localizedMessage}"
            }finally {
                _isLoading.value = false // ← vuelve a false cuando termina
            }
        }
    }

    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            repository.clearToken()
            token = null
            userName = ""
            userId=""
            onLogout()
        }
    }

    suspend fun isLoggedIn(): Boolean {
        val authData = repository.getAuthData()
        token = authData?.token
        userName = authData?.username ?: ""
        userId = authData?.userId ?: ""
        Log.d("SplashScreen", "isLoggedIn: ${token != null}, userName: $userName")
        return token != null
    }

    suspend fun checkUserRole(): String? {
        val authData = repository.getAuthData()
        token = authData?.token
        userName = authData?.username ?: ""
        userId = authData?.userId ?: ""
        userEmail = authData?.userEmail ?: ""

        return authData?.role // <- puede ser "USER", "ADMIN", etc.
    }

    suspend fun getSavedRole(): String? {
        val authData = repository.getAuthData()
        return authData?.role
    }

    private val _userData = MutableStateFlow<AuthToken?>(null)
    val userData: StateFlow<AuthToken?> = _userData

    fun loadUserData() {
        viewModelScope.launch {
            _userData.value = repository.getAuthData()
        }
    }

    fun getUserData() = viewModelScope.launch {
        val authData = repository.getAuthData()
        if (authData != null) {
            userName = authData.username
            userEmail = authData.userEmail
            _userData.value = authData // 👈 esto notifica al Composable
        }
    }

}


