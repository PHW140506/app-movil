package com.example.appmovil.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovil.data.ProductCatalogItem
import com.example.appmovil.data.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ProductCatalogUiState {
    object Loading : ProductCatalogUiState
    data class Success(val products: List<ProductCatalogItem>) : ProductCatalogUiState
    data class Error(val message: String) : ProductCatalogUiState
}

class ProductCatalogViewModel(
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductCatalogUiState>(ProductCatalogUiState.Loading)
    val uiState: StateFlow<ProductCatalogUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = ProductCatalogUiState.Loading
            repository.fetchCatalog()
                .onSuccess { _uiState.value = ProductCatalogUiState.Success(it) }
                .onFailure { _uiState.value = ProductCatalogUiState.Error(it.localizedMessage ?: "Fallo de conexión al cargar productos.") }
        }
    }
}