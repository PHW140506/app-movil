package com.example.appmovil.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovil.data.CartAuditItem
import com.example.appmovil.data.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuditUiState {
    object Loading : AuditUiState
    data class Success(val carts: List<CartAuditItem>) : AuditUiState
    data class Error(val message: String) : AuditUiState
    object Unauthorized : AuditUiState
}

class AuditCartsViewModel(
    private val repository: CartRepository = CartRepository(),
    private val userRole: String = "Auditor" // Simulación de rol por defecto
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuditUiState>(AuditUiState.Loading)
    val uiState: StateFlow<AuditUiState> = _uiState.asStateFlow()

    init {
        checkPermissionAndLoad()
    }

    private fun checkPermissionAndLoad() {
        val isAuthorized = userRole.equals("Auditor", ignoreCase = true) ||
                userRole.equals("Administrador", ignoreCase = true)

        if (!isAuthorized) {
            _uiState.value = AuditUiState.Unauthorized
            return
        }

        viewModelScope.launch {
            _uiState.value = AuditUiState.Loading
            repository.fetchAuditedCarts()
                .onSuccess { _uiState.value = AuditUiState.Success(it) }
                .onFailure { _uiState.value = AuditUiState.Error(it.localizedMessage ?: "Error al obtener carritos.") }
        }
    }
}