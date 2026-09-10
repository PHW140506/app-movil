package com.example.appmovil.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovil.data.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DeleteProductUiState {
    object Idle : DeleteProductUiState
    object Deleting : DeleteProductUiState
    data class Success(val message: String) : DeleteProductUiState
    data class Error(val message: String) : DeleteProductUiState
}

class DeleteProductViewModel(
    private val userRole: String,
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {

    // Escenario 3: Verificación de rol Administrador para autorización de borrado
    val isAdmin: Boolean = userRole.equals("Administrador", ignoreCase = true)

    private val _uiState = MutableStateFlow<DeleteProductUiState>(DeleteProductUiState.Idle)
    val uiState: StateFlow<DeleteProductUiState> = _uiState.asStateFlow()

    fun confirmAndDelete(productId: Int) {
        // Escenario 3: Bloqueo a nivel de código si no es Administrador
        if (!isAdmin) {
            _uiState.value = DeleteProductUiState.Error("Acción denegada: permisos insuficientes")
            return
        }

        viewModelScope.launch {
            _uiState.value = DeleteProductUiState.Deleting
            repository.deleteProduct(productId)
                .onSuccess {
                    // Escenario 1: Respuesta positiva de la API (Fake Store API retorna el objeto)
                    _uiState.value = DeleteProductUiState.Success("Producto eliminado exitosamente (Simulación)")
                }
                .onFailure {
                    _uiState.value = DeleteProductUiState.Error(it.localizedMessage ?: "Error al eliminar el producto")
                }
        }
    }

    fun resetState() {
        _uiState.value = DeleteProductUiState.Idle
    }
}