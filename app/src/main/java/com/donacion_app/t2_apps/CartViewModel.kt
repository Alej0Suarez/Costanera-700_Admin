package com.donacion_app.t2_apps

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CartViewModel(
    private val repository: OrderRepository = FirebaseOrderRepository()
) : ViewModel() {

    var orders by mutableStateOf<List<Order>>(emptyList())
        private set

    var cancelNotifications by mutableStateOf<List<CancelNotification>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    fun loadData() {
        viewModelScope.launch {
            isLoading = true
            val orderResult = repository.getPendingOrders()
            val cancelResult = repository.getCancelNotifications()

            orders = orderResult.getOrElse { emptyList() }
            cancelNotifications = cancelResult.getOrElse { emptyList() }
            isLoading = false
        }
    }

    fun updateOrderStatus(
        uid: String,
        orderId: String,
        newStatus: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.updateOrderStatus(uid, orderId, newStatus)
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { onError(it.message ?: "Error desconocido") }
            )
        }
    }
}