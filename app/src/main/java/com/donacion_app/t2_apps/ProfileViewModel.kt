package com.donacion_app.t2_apps

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: UserRepository = FirebaseUserRepository()
) : ViewModel() {

    var user by mutableStateOf<UserModel?>(null)
        private set

    var isLoading by mutableStateOf(true)
        private set

    fun loadCurrentUser() {
        viewModelScope.launch {
            isLoading = true
            val result = repository.getCurrentUser()
            user = result.getOrNull()
            isLoading = false
        }
    }

    fun signOut(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.signOut()
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { onError(it.message ?: "Error al cerrar sesión") }
            )
        }
    }
}