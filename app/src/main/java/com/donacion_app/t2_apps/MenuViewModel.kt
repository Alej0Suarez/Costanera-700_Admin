package com.donacion_app.t2_apps

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MenuViewModel(private val repo: DishRepository = FirebaseDishRepository()) : ViewModel() {
    var dishes by mutableStateOf<List<DishModel>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun loadDishes() {
        viewModelScope.launch {
            isLoading = true
            val result = repo.getAllDishes()
            isLoading = false
            result.fold(
                onSuccess = { dishes = it },
                onFailure = { errorMessage = it.message }
            )
        }
    }

    fun deleteDish(id: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repo.deleteDish(id)
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { onError(it.message ?: "Error al eliminar") }
            )
        }
    }
}