package com.example.appmovil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.appmovil.data.SessionManager
import com.example.appmovil.ui.screens.AuditCartsScreen
import com.example.appmovil.ui.screens.HomeScreen
import com.example.appmovil.ui.screens.LoginScreen
import com.example.appmovil.ui.theme.AppMovilTheme
import com.example.appmovil.ui.viewmodels.AuditCartsViewModel
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
                var showAuditScreen by remember { mutableStateOf(false) }

                val performLogout: () -> Unit = {
                    lifecycleScope.launch {
                        sessionManager.clearSession()
                        loginViewModel.resetState()
                        currentUsername = ""
                        currentRole = ""
                        showAuditScreen = false
                        isLoggedIn = false
                    }
                }

                // Manejo seguro del botón atrás de Android
                BackHandler(enabled = true) {
                    if (showAuditScreen) {
                        showAuditScreen = false // Si está en auditoría, regresa al HomeScreen
                    } else {
                        finish() // Si está en el Home o Login, cierra la aplicación
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (isLoggedIn) {
                            if (showAuditScreen) {
                                val auditViewModel = remember(currentRole) {
                                    AuditCartsViewModel(userRole = currentRole)
                                }
                                AuditCartsScreen(
                                    viewModel = auditViewModel,
                                    onBackClick = { showAuditScreen = false },
                                    onLogoutClick = performLogout
                                )
                            } else {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Vista principal existente (con su botón de logout habitual)
                                    Box(modifier = Modifier.weight(1f)) {
                                        HomeScreen(
                                            username = currentUsername,
                                            role = currentRole,
                                            onLogoutClick = performLogout
                                        )
                                    }

                                    // Criterio de aceptación 2: Bloqueo de ruta para perfiles no autorizados
                                    val isAuthorized = currentRole.equals("Auditor", ignoreCase = true) ||
                                            currentRole.equals("Administrador", ignoreCase = true)

                                    if (isAuthorized) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Button(
                                                onClick = { showAuditScreen = true },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary
                                                )
                                            ) {
                                                Text("Ver Histórico Global de Carritos (US12)")
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            LoginScreen(
                                viewModel = loginViewModel,
                                onLoginSuccess = { user, role ->
                                    currentUsername = user
                                    currentRole = role
                                    isLoggedIn = true
                                    showAuditScreen = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}