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
import com.example.appmovil.ui.screens.ProductDetailScreen
import com.example.appmovil.ui.screens.UserListScreen
import com.example.appmovil.ui.theme.AppMovilTheme
import com.example.appmovil.ui.viewmodels.AuditCartsViewModel
import com.example.appmovil.ui.viewmodels.LoginViewModel
import com.example.appmovil.ui.viewmodels.ProductDetailViewModel
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
                var selectedProductId by remember { mutableStateOf<Int?>(null) }

                val performLogout: () -> Unit = {
                    lifecycleScope.launch {
                        sessionManager.clearSession()
                        loginViewModel.resetState()
                        currentUsername = ""
                        currentRole = ""
                        showAuditScreen = false
                        showUsersScreen = false
                        selectedProductId = null
                        isLoggedIn = false
                    }
                }

                BackHandler(enabled = true) {
                    when {
                        selectedProductId != null -> selectedProductId = null
                        showAuditScreen -> showAuditScreen = false
                        showUsersScreen -> showUsersScreen = false
                        else -> finish()
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (isLoggedIn) {
                            when {
                                selectedProductId != null -> {
                                    val detailViewModel = remember(selectedProductId, currentRole) {
                                        ProductDetailViewModel(
                                            productId = selectedProductId!!,
                                            userRole = currentRole
                                        )
                                    }
                                    ProductDetailScreen(
                                        viewModel = detailViewModel,
                                        onBack = { selectedProductId = null }
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
                                                onLogoutClick = performLogout,
                                                onProductClick = { productId ->
                                                    selectedProductId = productId
                                                }
                                            )
                                        }

                                        val isAdminOrAuditor = currentRole.equals("Administrador", ignoreCase = true) ||
                                                currentRole.equals("Auditor", ignoreCase = true)

                                        if (isAdminOrAuditor) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
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
                                    selectedProductId = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}