package com.example.appmovil.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appmovil.ui.viewmodels.DeleteProductUiState
import com.example.appmovil.ui.viewmodels.DeleteProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteProductScreen(
    productId: Int,
    viewModel: DeleteProductViewModel,
    onBack: () -> Unit,
    onDeleteSuccess: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Escenario 1: Mostrar Snackbar al confirmar la eliminación y redirigir al catálogo
    LaunchedEffect(uiState) {
        if (uiState is DeleteProductUiState.Success) {
            snackbarHostState.showSnackbar((uiState as DeleteProductUiState.Success).message)
            viewModel.resetState()
            onDeleteSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Artículo #$productId") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("← Volver", color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Zona de Administración",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Producto seleccionado para gestión de inventario: ID #$productId.\n" +
                                    "Esta acción enviará una petición HTTP DELETE hacia la Fake Store API.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Escenario 3: La interfaz solo renderiza el botón de eliminación si el usuario es Administrador
                if (viewModel.isAdmin) {
                    Button(
                        onClick = { showConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState !is DeleteProductUiState.Deleting
                    ) {
                        if (uiState is DeleteProductUiState.Deleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onError,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Procesando eliminación...")
                        } else {
                            Text("Eliminar este producto")
                        }
                    }
                } else {
                    Text(
                        text = "No tienes permisos de Administrador para eliminar productos.",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Escenario 2 y Regla de Negocio: Alert Dialog de confirmación obligatoria
            if (showConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmDialog = false },
                    title = { Text("Confirmar eliminación") },
                    text = { Text("¿Estás seguro de eliminar este producto? Esta acción no se puede deshacer.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showConfirmDialog = false
                                viewModel.confirmAndDelete(productId)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Eliminar")
                        }
                    },
                    dismissButton = {
                        // Cancelación: Cierra el diálogo sin hacer peticiones a la red
                        OutlinedButton(onClick = { showConfirmDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // Manejo de errores
            if (uiState is DeleteProductUiState.Error) {
                val errorMsg = (uiState as DeleteProductUiState.Error).message
                AlertDialog(
                    onDismissRequest = { viewModel.resetState() },
                    title = { Text("Error") },
                    text = { Text(errorMsg) },
                    confirmButton = {
                        Button(onClick = { viewModel.resetState() }) {
                            Text("Aceptar")
                        }
                    }
                )
            }
        }
    }
}