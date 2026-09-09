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
import com.example.appmovil.ui.screens.DeleteProductScreen
import com.example.appmovil.ui.screens.HomeScreen
import com.example.appmovil.ui.screens.LoginScreen
import com.example.appmovil.ui.screens.UserListScreen
import com.example.appmovil.ui.theme.AppMovilTheme
import com.example.appmovil.ui.viewmodels.AuditCartsViewModel
import com.example.appmovil.ui.viewmodels.DeleteProductViewModel
import com.example.appmovil.ui.viewmodels.LoginViewModel
import com.example.appmovil.ui.viewmodels.UserListViewModel
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
                var showUsersScreen by remember { mutableStateOf(false) }
                var deletingProductId by remember { mutableStateOf<Int?>(null) }

                val performLogout: () -> Unit = {
                    lifecycleScope.launch {
                        sessionManager.clearSession()
                        loginViewModel.resetState()
                        currentUsername = ""
                        currentRole = ""
                        showAuditScreen = false
                        showUsersScreen = false
                        deletingProductId = null
                        isLoggedIn = false
                    }
                }

                BackHandler(enabled = true) {
                    when {
                        deletingProductId != null -> deletingProductId = null
                        showAuditScreen -> showAuditScreen = false
                        showUsersScreen -> showUsersScreen = false
                        else -> finish()
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (isLoggedIn) {
                            when {
                                deletingProductId != null -> {
                                    val deleteViewModel = remember(currentRole) {
                                        DeleteProductViewModel(userRole = currentRole)
                                    }
                                    DeleteProductScreen(
                                        productId = deletingProductId!!,
                                        viewModel = deleteViewModel,
                                        onBack = { deletingProductId = null },
                                        onDeleteSuccess = { deletingProductId = null }
                                    )
                                }
                                showAuditScreen -> {
                                    val auditViewModel = remember(currentRole) {
                                        AuditCartsViewModel(userRole = currentRole)
                                    }
                                    AuditCartsScreen(
                                        viewModel = auditViewModel,
                                        onBackClick = { showAuditScreen = false },
                                        onLogoutClick = performLogout
                                    )
                                }
                                showUsersScreen -> {
                                    val userViewModel = remember(currentRole) {
                                        UserListViewModel(userRole = currentRole)
                                    }
                                    UserListScreen(
                                        viewModel = userViewModel,
                                        onBackClick = { showUsersScreen = false },
                                        onLogoutClick = performLogout
                                    )
                                }
                                else -> {
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            HomeScreen(
                                                username = currentUsername,
                                                role = currentRole,
                                                onLogoutClick = performLogout
                                            )
                                        }

                                        val isAdmin = currentRole.equals("Administrador", ignoreCase = true)
                                        val isAuditorOrAdmin = isAdmin || currentRole.equals("Auditor", ignoreCase = true)

                                        if (isAuditorOrAdmin) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                // Escenario 3: Botón para eliminar exclusivo de Administrador
                                                if (isAdmin) {
                                                    Button(
                                                        onClick = { deletingProductId = 1 }, // Producto #1 para pruebas
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = MaterialTheme.colorScheme.error
                                                        )
                                                    ) {
                                                        Text("Eliminar Producto #1 (US08)")
                                                    }
                                                }
                                                Button(
                                                    onClick = { showAuditScreen = true },
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text("Ver Histórico de Carritos (US12)")
                                                }
                                                Button(
                                                    onClick = { showUsersScreen = true },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.secondary
                                                    )
                                                ) {
                                                    Text("Listar Directorio de Usuarios (US11)")
                                                }
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
                                    showUsersScreen = false
                                    deletingProductId = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}