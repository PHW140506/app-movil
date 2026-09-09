package com.example.appmovil.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovil.data.CreateProductRequestDto
import com.example.appmovil.data.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FormErrors(
    val titleError: String? = null,
    val priceError: String? = null,
    val descriptionError: String? = null,
    val imageError: String? = null,
    val categoryError: String? = null
)

sealed interface AddProductUiState {
    object Idle : AddProductUiState
    object Submitting : AddProductUiState
    data class Success(val generatedId: Int) : AddProductUiState
    data class Error(val message: String) : AddProductUiState
}

class AddProductViewModel(
    private val userRole: String,
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {

    // Regla de negocio: Validación del perfil estrictamente local
    val isAdmin: Boolean = userRole.equals("Administrador", ignoreCase = true)

    var title = MutableStateFlow("")
    var price = MutableStateFlow("")
    var description = MutableStateFlow("")
    var image = MutableStateFlow("")
    var category = MutableStateFlow("")

    private val _errors = MutableStateFlow(FormErrors())
    val errors: StateFlow<FormErrors> = _errors.asStateFlow()

    private val _uiState = MutableStateFlow<AddProductUiState>(AddProductUiState.Idle)
    val uiState: StateFlow<AddProductUiState> = _uiState.asStateFlow()

    fun submitForm() {
        if (!isAdmin) return

        val currentTitle = title.value.trim()
        val currentPrice = price.value.trim()
        val currentDesc = description.value.trim()
        val currentImg = image.value.trim()
        val currentCat = category.value.trim()

        // Validaciones Frontend locales antes de realizar solicitud HTTP
        val titleErr = if (currentTitle.isBlank()) "El título es obligatorio" else null
        val priceVal = currentPrice.toDoubleOrNull()
        val priceErr = when {
            currentPrice.isBlank() -> "El precio es obligatorio"
            priceVal == null || priceVal <= 0.0 -> "Ingresa un precio numérico válido"
            else -> null
        }
        val descErr = if (currentDesc.isBlank()) "La descripción es obligatoria" else null
        val imgErr = when {
            currentImg.isBlank() -> "La URL de la imagen es obligatoria"
            !currentImg.startsWith("http://") && !currentImg.startsWith("https://") -> "Debe ser una URL válida (http/https)"
            else -> null
        }
        val catErr = if (currentCat.isBlank()) "La categoría es obligatoria" else null

        val hasErrors = titleErr != null || priceErr != null || descErr != null || imgErr != null || catErr != null
        _errors.value = FormErrors(titleErr, priceErr, descErr, imgErr, catErr)

        // Detener petición si hay campos incorrectos
        if (hasErrors) return

        viewModelScope.launch {
            _uiState.value = AddProductUiState.Submitting
            val payload = CreateProductRequestDto(
                title = currentTitle,
                price = priceVal!!,
                description = currentDesc,
                image = currentImg,
                category = currentCat
            )

            repository.createProduct(payload)
                .onSuccess { created ->
                    clearForm()
                    _uiState.value = AddProductUiState.Success(created.id)
                }
                .onFailure {
                    _uiState.value = AddProductUiState.Error(it.localizedMessage ?: "Error al registrar el producto")
                }
        }
    }

    fun dismissSuccessDialog() {
        _uiState.value = AddProductUiState.Idle
    }

    private fun clearForm() {
        title.value = ""
        price.value = ""
        description.value = ""
        image.value = ""
        category.value = ""
        _errors.value = FormErrors()
    }
}