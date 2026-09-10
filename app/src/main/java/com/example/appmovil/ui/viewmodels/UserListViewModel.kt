package com.example.appmovil.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovil.data.UserRepository
import com.example.appmovil.data.UserUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UserListUiState {
    object Loading : UserListUiState
    data class Success(val users: List<UserUiModel>) : UserListUiState
    data class Error(val message: String) : UserListUiState
    object Unauthorized : UserListUiState
}

class UserListViewModel(
    private val repository: UserRepository = UserRepository(),
    private val userRole: String = "Auditor"
) : ViewModel() {

    private val _uiState = MutableStateFlow<UserListUiState>(UserListUiState.Loading)
    val uiState: StateFlow<UserListUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        val hasAccess = userRole.equals("Auditor", ignoreCase = true) ||
                userRole.equals("Administrador", ignoreCase = true)

        if (!hasAccess) {
            _uiState.value = UserListUiState.Unauthorized
            return
        }

        viewModelScope.launch {
            _uiState.value = UserListUiState.Loading
            repository.fetchUsers()
                .onSuccess { _uiState.value = UserListUiState.Success(it) }
                .onFailure { _uiState.value = UserListUiState.Error(it.localizedMessage ?: "Fallo de conexión al cargar usuarios.") }
        }
    }
}