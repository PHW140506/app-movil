package com.example.appmovil.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.appmovil.data.ProductCatalogDto
import com.example.appmovil.ui.viewmodels.ProductDetailUiState
import com.example.appmovil.ui.viewmodels.ProductDetailViewModel

@Composable
fun ProductDetailScreen(
    viewModel: ProductDetailViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val cartMessage by viewModel.cartMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(cartMessage) {
        cartMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearCartMessage()
        }
    }

    ProductDetailContent(
        state = state,
        canManage = viewModel.canManageProduct,
        isAuditor = viewModel.isAuditor,
        snackbarHostState = snackbarHostState,
        onAddToCart = { product -> viewModel.addToCart(product) },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailContent(
    state: ProductDetailUiState,
    canManage: Boolean,
    isAuditor: Boolean,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAddToCart: (ProductCatalogDto) -> Unit = {},
    onBack: () -> Unit = {}
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Producto") },
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
            when (state) {
                is ProductDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is ProductDetailUiState.Error -> {
                    AlertDialog(
                        onDismissRequest = onBack,
                        title = { Text("Error de consulta") },
                        text = { Text(state.message) },
                        confirmButton = {
                            Button(onClick = onBack) {
                                Text("Aceptar y volver")
                            }
                        }
                    )
                }

                is ProductDetailUiState.Success -> {
                    val product = state.product
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        AsyncImage(
                            model = product.image,
                            contentDescription = product.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = product.category.orEmpty().uppercase(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = product.title.orEmpty(),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "$${product.price}",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Descripción",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = product.description.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // US09: Agregar al carrito visible para Cliente y Administrador
                        if (!isAuditor) {
                            Button(
                                onClick = { onAddToCart(product) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Agregar al Carrito")
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // US07 / US08: Acciones exclusivas de Administrador
                        if (canManage) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { /* Navegar a editar */ },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Editar")
                                }

                                Button(
                                    onClick = { /* Navegar a eliminar */ },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}