package com.donacion_app.t2_apps

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository = FirebaseAuthRepository()) : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var recoveryEmail by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var showDialog by mutableStateOf(false)

    fun login(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            val result = authRepository.login(email, password)
            isLoading = false
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { onFailure(it.message ?: "Error desconocido") }
            )
        }
    }

    fun sendRecoveryEmail(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.sendRecoveryEmail(recoveryEmail)
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { onFailure(it.message ?: "Error al enviar correo") }
            )
        }
    }
}