package com.example.appmovil.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovil.data.CartRepository
import com.example.appmovil.data.LocalCartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

sealed interface CartManagementUiState {
    object Idle : CartManagementUiState
    object Loading : CartManagementUiState
    data class Success(val message: String) : CartManagementUiState
    data class Error(val message: String) : CartManagementUiState
}

class CartManagementViewModel(
    private val repository: CartRepository = CartRepository()
) : ViewModel() {

    val cartItems: StateFlow<List<LocalCartItem>> = CartRepository.localCartItems

    // Regla de Negocio: Cálculo iterativo sobre el arreglo local redondeado a 2 decimales
    val totalAmount: StateFlow<Double> = cartItems.map { list ->
        val total = list.sumOf { it.price * it.quantity }
        String.format(Locale.US, "%.2f", total).toDoubleOrNull() ?: 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _uiState = MutableStateFlow<CartManagementUiState>(CartManagementUiState.Idle)
    val uiState: StateFlow<CartManagementUiState> = _uiState.asStateFlow()

    // Escenario 1: Incrementar o decrementar cantidad y ejecutar petición PUT
    fun changeQuantity(productId: Int, delta: Int) {
        val item = cartItems.value.find { it.productId == productId } ?: return
        val newQuantity = item.quantity + delta

        if (newQuantity <= 0) {
            removeItem(productId)
        } else {
            repository.updateItemQuantity(productId, newQuantity)
            viewModelScope.launch {
                _uiState.value = CartManagementUiState.Loading
                repository.updateCartApiCall(cartId = 1, userId = 1, items = cartItems.value)
                    .onSuccess {
                        _uiState.value = CartManagementUiState.Success("Cantidad actualizada")
                    }
                    .onFailure {
                        _uiState.value = CartManagementUiState.Idle
                    }
            }
        }
    }

    // Escenario 2: Remoción y petición DELETE
    fun removeItem(productId: Int) {
        repository.removeLocalItem(productId)
        viewModelScope.launch {
            _uiState.value = CartManagementUiState.Loading
            repository.deleteCartApiCall(cartId = 1)
                .onSuccess {
                    _uiState.value = CartManagementUiState.Success("Producto eliminado del carrito")
                }
                .onFailure {
                    _uiState.value = CartManagementUiState.Idle
                }
        }
    }

    fun resetState() {
        _uiState.value = CartManagementUiState.Idle
    }
}