package com.example.appmovil.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovil.data.LoginRequest
import com.example.appmovil.data.NetworkUtils
import com.example.appmovil.data.RetrofitClient
import com.example.appmovil.data.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successRole: String? = null,
    val isLoginSuccessful: Boolean = false,
    val authenticatedUsername: String = ""
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun clearError() {
        if (_uiState.value.errorMessage != null) {
            _uiState.value = _uiState.value.copy(errorMessage = null)
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState()
    }

    fun login(usernameInput: String, passwordInput: String) {
        val cleanUser = usernameInput.trim()
        val cleanPass = passwordInput.trim()

        // US01 - Escenario 3: Verificación previa de conexión a Internet
        if (!NetworkUtils.hasInternetConnection(getApplication())) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Sin conexión a internet. Verifique su red antes de continuar."
            )
            return
        }

        if (cleanUser.isBlank() || cleanPass.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Por favor ingrese usuario y contraseña."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                // Paso 1: Autenticar
                val loginResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.login(LoginRequest(cleanUser, cleanPass))
                }

                if (loginResponse.isSuccessful && loginResponse.body()?.token != null) {
                    val token = loginResponse.body()!!.token!!

                    // Paso 2: Descargar información de usuarios para resolver el ID real
                    val usersResponse = withContext(Dispatchers.IO) {
                        RetrofitClient.apiService.getUsers()
                    }

                    val resolvedUserId = if (usersResponse.isSuccessful) {
                        usersResponse.body()?.find { it.username.equals(cleanUser, ignoreCase = true) }?.id ?: 4
                    } else {
                        4 // ID por defecto -> Rol CLIENTE
                    }

                    // US01 - Escenario 1: Guardar sesión y mapear rol
                    sessionManager.saveSession(token, resolvedUserId, cleanUser)
                    val assignedRole = sessionManager.getUserRole().name

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successRole = assignedRole,
                        isLoginSuccessful = true,
                        authenticatedUsername = cleanUser
                    )
                } else {
                    // US01 - Escenario 2: Credenciales incorrectas
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Usuario o contraseña inválidos"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error de comunicación con el servidor: ${e.localizedMessage ?: "desconocido"}"
                )
            }
        }
    }
}