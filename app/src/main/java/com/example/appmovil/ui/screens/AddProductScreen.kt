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
import com.example.appmovil.ui.viewmodels.AddProductUiState
import com.example.appmovil.ui.viewmodels.AddProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    viewModel: AddProductViewModel,
    onBack: () -> Unit
) {
    // Escenario 3: Bloqueo de acceso si no es Administrador
    LaunchedEffect(viewModel.isAdmin) {
        if (!viewModel.isAdmin) {
            onBack()
        }
    }

    if (!viewModel.isAdmin) return

    val title by viewModel.title.collectAsState()
    val price by viewModel.price.collectAsState()
    val description by viewModel.description.collectAsState()
    val image by viewModel.image.collectAsState()
    val category by viewModel.category.collectAsState()

    val errors by viewModel.errors.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Producto (US06)") },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Título
                OutlinedTextField(
                    value = title,
                    onValueChange = { viewModel.title.value = it },
                    label = { Text("Título del producto *") },
                    isError = errors.titleError != null,
                    supportingText = errors.titleError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                // Precio
                OutlinedTextField(
                    value = price,
                    onValueChange = { viewModel.price.value = it },
                    label = { Text("Precio *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = errors.priceError != null,
                    supportingText = errors.priceError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                // Categoría
                OutlinedTextField(
                    value = category,
                    onValueChange = { viewModel.category.value = it },
                    label = { Text("Categoría (ej: electronics) *") },
                    isError = errors.categoryError != null,
                    supportingText = errors.categoryError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                // URL de la Imagen
                OutlinedTextField(
                    value = image,
                    onValueChange = { viewModel.image.value = it },
                    label = { Text("URL de la imagen (http/https) *") },
                    isError = errors.imageError != null,
                    supportingText = errors.imageError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                // Descripción
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

                // Botón Guardar
                Button(
                    onClick = { viewModel.submitForm() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is AddProductUiState.Submitting
                ) {
                    if (uiState is AddProductUiState.Submitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardando...")
                    } else {
                        Text("Guardar Producto")
                    }
                }
            }

            // Escenario 1: Confirmación de nuevo ID generado
            if (uiState is AddProductUiState.Success) {
                val newId = (uiState as AddProductUiState.Success).generatedId
                AlertDialog(
                    onDismissRequest = { viewModel.dismissSuccessDialog() },
                    title = { Text("Producto Registrado") },
                    text = { Text("El artículo fue procesado exitosamente por la API con el ID: #$newId.") },
                    confirmButton = {
                        Button(onClick = { viewModel.dismissSuccessDialog() }) {
                            Text("Aceptar")
                        }
                    }
                )
            }

            // Manejo de error de red
            if (uiState is AddProductUiState.Error) {
                val msg = (uiState as AddProductUiState.Error).message
                AlertDialog(
                    onDismissRequest = { viewModel.dismissSuccessDialog() },
                    title = { Text("Error al registrar") },
                    text = { Text(msg) },
                    confirmButton = {
                        Button(onClick = { viewModel.dismissSuccessDialog() }) {
                            Text("Cerrar")
                        }
                    }
                )
            }
        }
    }
}