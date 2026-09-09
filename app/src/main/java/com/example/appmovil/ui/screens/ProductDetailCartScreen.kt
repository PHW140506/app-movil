package com.example.appmovil.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appmovil.ui.viewmodels.AddToCartUiState
import com.example.appmovil.ui.viewmodels.AddToCartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailCartScreen(
    viewModel: AddToCartViewModel,
    onBack: () -> Unit
) {
    var selectedQuantity by remember { mutableIntStateOf(1) }
    val uiState by viewModel.uiState.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val sampleProductId = 1
    val sampleTitle = "Fjallraven - Foldsack No. 1 Backpack"
    val samplePrice = 109.95
    val sampleImage = "https://fakestoreapi.com/img/81fPKd-2AYL._AC_SL1500_.jpg"

    // Escenario 1: Notificación flotante (Snackbar) al agregar
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AddToCartUiState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            is AddToCartUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Artículo (US09)") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("← Volver", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = sampleTitle, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Precio: $$samplePrice", style = MaterialTheme.typography.bodyLarge)
                }
            }

            // Escenario 3: Si es Auditor, oculta completamente los controles de agregar al carrito
            if (viewModel.canAddToCart) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Cantidad:")
                    OutlinedButton(
                        onClick = { if (selectedQuantity > 1) selectedQuantity-- }
                    ) { Text("-") }

                    Text(
                        text = "$selectedQuantity",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedButton(
                        onClick = { selectedQuantity++ }
                    ) { Text("+") }
                }

                Button(
                    onClick = {
                        viewModel.addToCart(
                            productId = sampleProductId,
                            title = sampleTitle,
                            price = samplePrice,
                            imageUrl = sampleImage,
                            quantity = selectedQuantity
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is AddToCartUiState.Loading
                ) {
                    if (uiState is AddToCartUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enviando...")
                    } else {
                        Text("Agregar al carrito")
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Modo Auditor: visualización de solo lectura. La opción de agregar al carrito está inhabilitada.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Divider()

            Text("Contenido de tu carrito local:", style = MaterialTheme.typography.titleSmall)

            if (cartItems.isEmpty()) {
                Text("El carrito está vacío actualmente.", style = MaterialTheme.typography.bodySmall)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(cartItems) { item ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.title, modifier = Modifier.weight(1f))
                                Text("Cant: ${item.quantity}")
                            }
                        }
                    }
                }
            }
        }
    }
}