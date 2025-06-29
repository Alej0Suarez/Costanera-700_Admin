package com.donacion_app.t2_apps

data class OrderItem(
    val dishName: String = "",
    val quantity: Int = 0,
    val note: String = "",
    val subtotal: Double = 0.0
)

data class Order(
    val items: List<OrderItem> = emptyList(),
    val orderId: String? = null,
    val deliveryOption: String = "",         // "Tienda" o "Delivery"
    val userLocation: String = "",           // solo si es Delivery
    val estimatedTime: String = "",          // solo si es Delivery
    val estimatedCost: Double = 0.0,         // solo si es Delivery
    val cashAmount: String = "",             // solo si es Delivery
    val totalPrice: Double = 0.0,
    val uid: String? = null,
    val estado: String = "pendiente",
    val timestamp: Any? = null,
    val createdAt: Long = System.currentTimeMillis()
)