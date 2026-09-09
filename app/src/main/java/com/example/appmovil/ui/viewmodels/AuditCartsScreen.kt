package com.example.appmovil.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appmovil.data.CartAuditItem
import com.example.appmovil.data.CartProductDetail
import com.example.appmovil.ui.viewmodels.AuditCartsViewModel
import com.example.appmovil.ui.viewmodels.AuditUiState

@Composable
fun AuditCartsScreen(
    viewModel: AuditCartsViewModel,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    AuditCartsContent(
        state = state,
        onBackClick = onBackClick,
        onLogoutClick = onLogoutClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditCartsContent(
    state: AuditUiState,
    onBackClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Auditoría") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("← Volver", color = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    TextButton(onClick = onLogoutClick) {
                        Text("Cerrar Sesión", color = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (state) {
                is AuditUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is AuditUiState.Unauthorized -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ACCESO DENEGADO",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Solo auditores y administradores pueden consultar este historial.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                is AuditUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                is AuditUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.carts) { cart ->
                            CartItemRow(cart = cart)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(cart: CartAuditItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Carrito ID: #${cart.id}", fontWeight = FontWeight.Bold)
                    Text(text = "Propietario (User ID): ${cart.userId}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Fecha: ${cart.date}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Text(
                    text = if (expanded) "▲" else "▼",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Artículos en el carrito:",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    cart.products.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "• ${item.title} (ID: ${item.productId})",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "x${item.quantity}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuditCartsScreenPreview() {
    AuditCartsContent(
        state = AuditUiState.Success(
            carts = listOf(
                CartAuditItem(
                    id = 1,
                    userId = 2,
                    date = "2026-03-02",
                    products = listOf(
                        CartProductDetail(1, "Fjallraven Backpack", 2),
                        CartProductDetail(2, "Mens Casual Premium Slim Fit", 1)
                    )
                )
            )
        )
    )
}