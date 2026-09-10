package com.example.appmovil.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovil.data.CreateProductRequestDto
import com.example.appmovil.data.ProductCatalogDto
import com.example.appmovil.data.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditFormErrors(
    val titleError: String? = null,
    val priceError: String? = null,
    val descriptionError: String? = null,
    val categoryError: String? = null
)

sealed interface EditProductUiState {
    object LoadingInitial : EditProductUiState
    object Idle : EditProductUiState
    object Submitting : EditProductUiState
    data class Success(val updatedProduct: ProductCatalogDto) : EditProductUiState
    data class Error(val message: String) : EditProductUiState
}

class EditProductViewModel(
    val productId: Int,
    private val userRole: String,
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {

    val isAdmin: Boolean = userRole.equals("Administrador", ignoreCase = true)

    var title = MutableStateFlow("")
    var price = MutableStateFlow("")
    var description = MutableStateFlow("")
    var category = MutableStateFlow("")
    var image = MutableStateFlow("")

    private val _errors = MutableStateFlow(EditFormErrors())
    val errors: StateFlow<EditFormErrors> = _errors.asStateFlow()

    private val _uiState = MutableStateFlow<EditProductUiState>(EditProductUiState.LoadingInitial)
    val uiState: StateFlow<EditProductUiState> = _uiState.asStateFlow()

    init {
        loadInitialProductData()
    }

    fun loadInitialProductData() {
        if (!isAdmin) {
            _uiState.value = EditProductUiState.Error("Acceso denegado: permisos insuficientes")
            return
        }

        viewModelScope.launch {
            _uiState.value = EditProductUiState.LoadingInitial
            repository.fetchProductDetail(productId)
                .onSuccess { product ->
                    title.value = product.title.orEmpty()
                    price.value = product.price.toString()
                    description.value = product.description.orEmpty()
                    category.value = product.category.orEmpty()
                    image.value = product.image.orEmpty()
                    _uiState.value = EditProductUiState.Idle
                }
                .onFailure {
                    _uiState.value = EditProductUiState.Error("No se pudo cargar la información del producto")
                }
        }
    }

    fun submitUpdate() {
        if (!isAdmin) return

        val currentTitle = title.value.trim()
        val currentPrice = price.value.trim()
        val currentDesc = description.value.trim()
        val currentCat = category.value.trim()
        val currentImg = image.value.trim()

        val titleErr = if (currentTitle.isBlank()) "El título no puede estar vacío" else null
        val priceVal = currentPrice.toDoubleOrNull()
        val priceErr = when {
            currentPrice.isBlank() -> "El precio es obligatorio"
            priceVal == null || priceVal <= 0.0 -> "Ingresa un número válido para el precio"
            else -> null
        }
        val descErr = if (currentDesc.isBlank()) "La descripción no puede estar vacía" else null
        val catErr = if (currentCat.isBlank()) "La categoría no puede estar vacía" else null

        val hasErrors = titleErr != null || priceErr != null || descErr != null || catErr != null
        _errors.value = EditFormErrors(
            titleError = titleErr,
            priceError = priceErr,
            descriptionError = descErr,
            categoryError = catErr
        )

        if (hasErrors) return

        viewModelScope.launch {
            _uiState.value = EditProductUiState.Submitting
            val payload = CreateProductRequestDto(
                title = currentTitle,
                price = priceVal!!,
                description = currentDesc,
                image = currentImg.ifBlank { "https://fakestoreapi.com/img/default.jpg" },
                category = currentCat
            )

            repository.updateProduct(productId, payload)
                .onSuccess {
                    val finalProduct = ProductCatalogDto(
                        id = productId,
                        title = currentTitle,
                        price = priceVal,
                        description = currentDesc,
                        category = currentCat,
                        image = currentImg
                    )
                    _uiState.value = EditProductUiState.Success(finalProduct)
                }
                .onFailure {
                    _uiState.value = EditProductUiState.Error(it.localizedMessage ?: "Error al actualizar el producto")
                }
        }
    }

    fun dismissSuccess() {
        _uiState.value = EditProductUiState.Idle
    }
}