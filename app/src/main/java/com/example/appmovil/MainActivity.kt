package com.example.appmovil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.appmovil.data.SessionManager
import com.example.appmovil.ui.screens.HomeScreen
import com.example.appmovil.ui.screens.LoginScreen
import com.example.appmovil.ui.theme.AppMovilTheme
import com.example.appmovil.ui.viewmodels.LoginViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = SessionManager(applicationContext)

        setContent {
            AppMovilTheme {
                var isLoggedIn by remember {
                    mutableStateOf(!sessionManager.getToken().isNullOrBlank())
                }
                var currentUsername by remember {
                    mutableStateOf(if (isLoggedIn) sessionManager.getUsername() else "")
                }
                var currentRole by remember {
                    mutableStateOf(if (isLoggedIn) sessionManager.getUserRole().name else "")
                }

                // US02 - Escenario 2: Bloqueo de retroceso a vistas protegidas
                // Si el usuario ya está en Login (no logueado) y da Atrás, la app se cierra.
                // Si está en HomeScreen y da Atrás, también se cierra para evitar regresar a Login sin cerrar sesión.
                BackHandler(enabled = true) {
                    finish()
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (isLoggedIn) {
                            HomeScreen(
                                username = currentUsername,
                                role = currentRole,
                                onLogoutClick = {
                                    // US02 - Escenarios 1 y 3: Cierre de sesión y limpieza profunda
                                    lifecycleScope.launch {
                                        sessionManager.clearSession()
                                        loginViewModel.resetState()
                                        currentUsername = ""
                                        currentRole = ""
                                        isLoggedIn = false
                                    }
                                }
                            )
                        } else {
                            LoginScreen(
                                viewModel = loginViewModel,
                                onLoginSuccess = { user, role ->
                                    currentUsername = user
                                    currentRole = role
                                    isLoggedIn = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}