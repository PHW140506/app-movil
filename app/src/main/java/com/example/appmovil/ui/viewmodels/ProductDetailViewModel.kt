package com.example.appmovil.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovil.data.CartRepository
import com.example.appmovil.data.ProductCatalogDto
import com.example.appmovil.data.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ProductDetailUiState {
    object Loading : ProductDetailUiState
    data class Success(val product: ProductCatalogDto) : ProductDetailUiState
    data class Error(val message: String) : ProductDetailUiState
}

class ProductDetailViewModel(
    private val productId: Int,
    private val userRole: String,
    private val repository: ProductRepository = ProductRepository(),
    private val cartRepository: CartRepository = CartRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private val _cartMessage = MutableStateFlow<String?>(null)
    val cartMessage: StateFlow<String?> = _cartMessage.asStateFlow()

    val canManageProduct: Boolean = userRole.equals("Administrador", ignoreCase = true)
    val isAuditor: Boolean = userRole.equals("Auditor", ignoreCase = true)

    init {
        loadProductDetail()
    }

    fun loadProductDetail() {
        viewModelScope.launch {
            _uiState.value = ProductDetailUiState.Loading
            repository.fetchProductDetail(productId)
                .onSuccess { _uiState.value = ProductDetailUiState.Success(it) }
                .onFailure { _uiState.value = ProductDetailUiState.Error("Producto no disponible") }
        }
    }

    fun addToCart(product: ProductCatalogDto) {
        cartRepository.addOrUpdateLocalCart(
            productId = product.id,
            title = product.title.orEmpty(),
            price = product.price ?: 0.0,
            imageUrl = product.image.orEmpty(),
            quantity = 1
        )
        _cartMessage.value = "Producto agregado al carrito"
    }

    fun clearCartMessage() {
        _cartMessage.value = null
    }
}