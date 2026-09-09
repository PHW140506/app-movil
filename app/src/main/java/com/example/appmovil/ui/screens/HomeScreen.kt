package com.example.appmovil.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    username: String,
    role: String,
    onLogoutClick: () -> Unit,
    onProductClick: (Int) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Bienvenido, $username", fontWeight = FontWeight.Bold)
                Text(text = "Rol: $role", style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onLogoutClick) {
                Text("Salir", color = MaterialTheme.colorScheme.error)
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Box(modifier = Modifier.fillMaxSize()) {
            ProductCatalogScreen(onProductClick = onProductClick)
        }
    }
}