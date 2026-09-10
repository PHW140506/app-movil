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

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    // Criterio 3: null o "Todas" representa la opción "Ver todos"
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // Cargar categorías disponibles
            repository.fetchCategories().onSuccess {
                _categories.value = it
            }
            // Cargar catálogo general inicial
            loadProducts(null)
        }
    }

    fun selectCategory(category: String?) {
        if (_selectedCategory.value == category) return
        _selectedCategory.value = category
        loadProducts(category)
    }

    fun retryCurrentSelection() {
        loadProducts(_selectedCategory.value)
    }

    private fun loadProducts(category: String?) {
        viewModelScope.launch {
            // Regla de negocio (Gestión de memoria & Consistencia de carga):
            // Limpiar la lista previa y activar spinner de carga antes de la petición HTTP
            _uiState.value = ProductCatalogUiState.Loading

            val result = if (category.isNullOrBlank()) {
                repository.fetchCatalog() // US04 - Escenario 3: "Ver todos"
            } else {
                repository.fetchProductsByCategory(category) // US04 - Escenario 2: Filtrado
            }

            result
                .onSuccess { _uiState.value = ProductCatalogUiState.Success(it) }
                .onFailure { _uiState.value = ProductCatalogUiState.Error(it.localizedMessage ?: "Error de red.") }
        }
    }
}