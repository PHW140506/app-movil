package com.example.appmovil.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovil.data.CartRepository
import com.example.appmovil.data.LocalCartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AddToCartUiState {
    object Idle : AddToCartUiState
    object Loading : AddToCartUiState
    data class Success(val message: String) : AddToCartUiState
    data class Error(val message: String) : AddToCartUiState
}

class AddToCartViewModel(
    private val userRole: String,
    private val repository: CartRepository = CartRepository()
) : ViewModel() {

    // Escenario 3: Ocultar completamente la función al perfil Auditor (solo lectura)
    val canAddToCart: Boolean = !userRole.equals("Auditor", ignoreCase = true)

    val cartItems: StateFlow<List<LocalCartItem>> = CartRepository.localCartItems

    private val _uiState = MutableStateFlow<AddToCartUiState>(AddToCartUiState.Idle)
    val uiState: StateFlow<AddToCartUiState> = _uiState.asStateFlow()

    // Escenario 1 y 2: Validación, consumo HTTP POST y persistencia en memoria local
    fun addToCart(productId: Int, title: String, price: Double, imageUrl: String, quantity: Int) {
        if (!canAddToCart) {
            _uiState.value = AddToCartUiState.Error("Perfil Auditor en modo solo lectura.")
            return
        }

        if (quantity <= 0) {
            _uiState.value = AddToCartUiState.Error("Selecciona una cantidad mayor a cero.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AddToCartUiState.Loading

            val result = repository.postCartToApi(userId = 1, productId = productId, quantity = quantity)

            result.onSuccess {
                // Guarda o acumula en memoria local
                repository.addOrUpdateLocalCart(
                    productId = productId,
                    title = title,
                    price = price,
                    imageUrl = imageUrl,
                    quantity = quantity
                )
                _uiState.value = AddToCartUiState.Success("¡$quantity artículo(s) añadido(s) al carrito!")
            }.onFailure {
                // En caso de fallo de red, igualmente mantiene el estado local
                repository.addOrUpdateLocalCart(
                    productId = productId,
                    title = title,
                    price = price,
                    imageUrl = imageUrl,
                    quantity = quantity
                )
                _uiState.value = AddToCartUiState.Success("Artículo guardado en tu carrito local.")
            }
        }
    }

    fun resetState() {
        _uiState.value = AddToCartUiState.Idle
    }
}