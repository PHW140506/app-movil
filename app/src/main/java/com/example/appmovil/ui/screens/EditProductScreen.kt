package com.example.appmovil.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.appmovil.data.ProductCatalogDto
import com.example.appmovil.ui.viewmodels.EditProductUiState
import com.example.appmovil.ui.viewmodels.EditProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    viewModel: EditProductViewModel,
    onBack: () -> Unit,
    onUpdateSuccess: (ProductCatalogDto) -> Unit
) {
    LaunchedEffect(viewModel.isAdmin) {
        if (!viewModel.isAdmin) {
            onBack()
        }
    }

    if (!viewModel.isAdmin) return

    val title by viewModel.title.collectAsState()
    val price by viewModel.price.collectAsState()
    val description by viewModel.description.collectAsState()
    val category by viewModel.category.collectAsState()
    val image by viewModel.image.collectAsState()

    val errors by viewModel.errors.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Producto #${viewModel.productId}") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("← Cancelar", color = MaterialTheme.colorScheme.primary)
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
            when (uiState) {
                is EditProductUiState.LoadingInitial -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Cargando información del producto...")
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { viewModel.title.value = it },
                            label = { Text("Título *") },
                            isError = errors.titleError != null,
                            supportingText = errors.titleError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = price,
                            onValueChange = { viewModel.price.value = it },
                            label = { Text("Precio *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = errors.priceError != null,
                            supportingText = errors.priceError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = category,
                            onValueChange = { viewModel.category.value = it },
                            label = { Text("Categoría *") },
                            isError = errors.categoryError != null,
                            supportingText = errors.categoryError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = image,
                            onValueChange = { viewModel.image.value = it },
                            label = { Text("URL Imagen") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { viewModel.description.value = it },
                            label = { Text("Descripción completa *") },
                            minLines = 3,
                            isError = errors.descriptionError != null,
                            supportingText = errors.descriptionError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val isSubmitting = uiState is EditProductUiState.Submitting
                        Button(
                            onClick = { viewModel.submitUpdate() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSubmitting
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Guardando cambios...")
                            } else {
                                Text("Guardar Cambios")
                            }
                        }
                    }
                }
            }

            if (uiState is EditProductUiState.Success) {
                val updated = (uiState as EditProductUiState.Success).updatedProduct
                AlertDialog(
                    onDismissRequest = {
                        viewModel.dismissSuccess()
                        onUpdateSuccess(updated)
                    },
                    title = { Text("Actualización Exitosa") },
                    text = { Text("Producto actualizado (Simulación). Los cambios se reflejarán localmente.") },
                    confirmButton = {
                        Button(onClick = {
                            viewModel.dismissSuccess()
                            onUpdateSuccess(updated)
                        }) {
                            Text("Aceptar")
                        }
                    }
                )
            }

            if (uiState is EditProductUiState.Error) {
                val msg = (uiState as EditProductUiState.Error).message
                AlertDialog(
                    onDismissRequest = { viewModel.dismissSuccess() },
                    title = { Text("Error") },
                    text = { Text(msg) },
                    confirmButton = {
                        Button(onClick = { viewModel.dismissSuccess() }) {
                            Text("Cerrar")
                        }
                    }
                )
            }
        }
    }
}