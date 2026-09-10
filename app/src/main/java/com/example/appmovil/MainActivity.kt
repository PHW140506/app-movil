package com.example.appmovil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.appmovil.data.SessionManager
import com.example.appmovil.ui.screens.AddProductScreen
import com.example.appmovil.ui.screens.AuditCartsScreen
import com.example.appmovil.ui.screens.CartManagementScreen
import com.example.appmovil.ui.screens.DeleteProductScreen
import com.example.appmovil.ui.screens.EditProductScreen
import com.example.appmovil.ui.screens.HomeScreen
import com.example.appmovil.ui.screens.LoginScreen
import com.example.appmovil.ui.screens.ProductDetailScreen
import com.example.appmovil.ui.screens.UserListScreen
import com.example.appmovil.ui.theme.AppMovilTheme
import com.example.appmovil.ui.viewmodels.AddProductViewModel
import com.example.appmovil.ui.viewmodels.AuditCartsViewModel
import com.example.appmovil.ui.viewmodels.CartManagementViewModel
import com.example.appmovil.ui.viewmodels.DeleteProductViewModel
import com.example.appmovil.ui.viewmodels.EditProductViewModel
import com.example.appmovil.ui.viewmodels.LoginViewModel
import com.example.appmovil.ui.viewmodels.ProductDetailViewModel
import com.example.appmovil.ui.viewmodels.UserListViewModel
import kotlinx.coroutines.launch

sealed interface AppRoute {
    object Home : AppRoute
    data class ProductDetail(val productId: Int) : AppRoute
    object AddProduct : AppRoute
    data class EditProduct(val productId: Int) : AppRoute
    data class DeleteProduct(val productId: Int) : AppRoute
    object MyCart : AppRoute
    object AuditCarts : AppRoute
    object UserList : AppRoute
}

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

                var currentRoute by remember { mutableStateOf<AppRoute>(AppRoute.Home) }

                val performLogout: () -> Unit = {
                    lifecycleScope.launch {
                        sessionManager.clearSession()
                        loginViewModel.resetState()
                        currentUsername = ""
                        currentRole = ""
                        currentRoute = AppRoute.Home
                        isLoggedIn = false
                    }
                }

                BackHandler(enabled = isLoggedIn && currentRoute !is AppRoute.Home) {
                    currentRoute = AppRoute.Home
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (!isLoggedIn) {
                            LoginScreen(
                                viewModel = loginViewModel,
                                onLoginSuccess = { user, role ->
                                    currentUsername = user
                                    currentRole = role
                                    isLoggedIn = true
                                    currentRoute = AppRoute.Home
                                }
                            )
                        } else {
                            val isAdmin = currentRole.equals("Administrador", ignoreCase = true)
                            val isAuditor = currentRole.equals("Auditor", ignoreCase = true)

                            when (val route = currentRoute) {
                                is AppRoute.Home -> {
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            HomeScreen(
                                                username = currentUsername,
                                                role = currentRole,
                                                onLogoutClick = performLogout,
                                                onProductClick = { productId ->
                                                    currentRoute = AppRoute.ProductDetail(productId)
                                                }
                                            )
                                        }

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 8.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            if (!isAuditor) {
                                                Button(
                                                    onClick = { currentRoute = AppRoute.MyCart },
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text("Mi Carrito de Compras (US10)")
                                                }
                                            }

                                            if (isAdmin) {
                                                Button(
                                                    onClick = { currentRoute = AppRoute.AddProduct },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.secondary
                                                    )
                                                ) {
                                                    Text("Agregar Nuevo Producto (US06)")
                                                }
                                            }

                                            if (isAdmin || isAuditor) {
                                                Button(
                                                    onClick = { currentRoute = AppRoute.AuditCarts },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.tertiary
                                                    )
                                                ) {
                                                    Text("Histórico Global de Carritos (US12)")
                                                }

                                                Button(
                                                    onClick = { currentRoute = AppRoute.UserList },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.outline
                                                    )
                                                ) {
                                                    Text("Directorio de Usuarios (US11)")
                                                }
                                            }
                                        }
                                    }
                                }

                                is AppRoute.ProductDetail -> {
                                    val detailVm = remember(route.productId, currentRole) {
                                        ProductDetailViewModel(route.productId, currentRole)
                                    }
                                    ProductDetailScreen(
                                        viewModel = detailVm,
                                        onBack = { currentRoute = AppRoute.Home }
                                    )
                                }

                                is AppRoute.AddProduct -> {
                                    val addVm = remember(currentRole) {
                                        AddProductViewModel(userRole = currentRole)
                                    }
                                    AddProductScreen(
                                        viewModel = addVm,
                                        onBack = { currentRoute = AppRoute.Home }
                                    )
                                }

                                is AppRoute.EditProduct -> {
                                    val editVm = remember(route.productId, currentRole) {
                                        EditProductViewModel(
                                            productId = route.productId,
                                            userRole = currentRole
                                        )
                                    }
                                    EditProductScreen(
                                        viewModel = editVm,
                                        onBack = { currentRoute = AppRoute.Home },
                                        onUpdateSuccess = { currentRoute = AppRoute.Home }
                                    )
                                }

                                is AppRoute.DeleteProduct -> {
                                    val deleteVm = remember(currentRole) {
                                        DeleteProductViewModel(userRole = currentRole)
                                    }
                                    DeleteProductScreen(
                                        viewModel = deleteVm,
                                        productId = route.productId,
                                        onBack = { currentRoute = AppRoute.Home },
                                        onDeleteSuccess = { currentRoute = AppRoute.Home }
                                    )
                                }

                                is AppRoute.MyCart -> {
                                    val cartMgmtVm = remember { CartManagementViewModel() }
                                    CartManagementScreen(
                                        viewModel = cartMgmtVm,
                                        onBack = { currentRoute = AppRoute.Home }
                                    )
                                }

                                is AppRoute.AuditCarts -> {
                                    val auditVm = remember(currentRole) {
                                        AuditCartsViewModel(userRole = currentRole)
                                    }
                                    AuditCartsScreen(
                                        viewModel = auditVm,
                                        onBackClick = { currentRoute = AppRoute.Home },
                                        onLogoutClick = performLogout
                                    )
                                }

                                is AppRoute.UserList -> {
                                    val userVm = remember(currentRole) {
                                        UserListViewModel(userRole = currentRole)
                                    }
                                    UserListScreen(
                                        viewModel = userVm,
                                        onBackClick = { currentRoute = AppRoute.Home },
                                        onLogoutClick = performLogout
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}