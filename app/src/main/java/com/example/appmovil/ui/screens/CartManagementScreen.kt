package com.example.appmovil.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appmovil.ui.viewmodels.CartManagementUiState
import com.example.appmovil.ui.viewmodels.CartManagementViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartManagementScreen(
    viewModel: CartManagementViewModel,
    onBack: () -> Unit
) {
    val items by viewModel.cartItems.collectAsState()
    val total by viewModel.totalAmount.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is CartManagementUiState.Success) {
            snackbarHostState.showSnackbar((uiState as CartManagementUiState.Success).message)
            viewModel.resetState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mi Carrito") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("← Volver", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total a pagar:",
                            style = MaterialTheme.typography.titleMedium
                        )
                        // Regla de Negocio: Monto total redondeado a 2 decimales
                        Text(
                            text = String.format(Locale.US, "$%.2f", total),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Escenario 3: Botón inhabilitado si el carrito está vacío
                    Button(
                        onClick = { /* Simulación de compra */ },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = items.isNotEmpty()
                    ) {
                        Text("Proceder al pago")
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Escenario 3: Interfaz gráfica cuando no existen artículos
            if (items.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🛒",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Tu carrito está vacío, explora el catálogo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.productId }) { item ->
                        val subtotal = item.price * item.quantity

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Precio: $${String.format(Locale.US, "%.2f", item.price)} | Subtotal: $${String.format(Locale.US, "%.2f", subtotal)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Escenario 1: Reducir cantidad (elimina si llega a 0)
                                    OutlinedButton(
                                        onClick = { viewModel.changeQuantity(item.productId, -1) },
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Text("-")
                                    }

                                    Text(
                                        text = "${item.quantity}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )

                                    // Escenario 1: Incrementar cantidad
                                    OutlinedButton(
                                        onClick = { viewModel.changeQuantity(item.productId, 1) },
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Text("+")
                                    }

                                    // Escenario 2: Botón de eliminación directa
                                    IconButton(onClick = { viewModel.removeItem(item.productId) }) {
                                        Text(
                                            text = "✕",
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold
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
}