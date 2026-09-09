package com.example.appmovil.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    // Regla de negocio: Validación del perfil estrictamente local
    val canManageProduct: Boolean = userRole.equals("Administrador", ignoreCase = true)

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
}