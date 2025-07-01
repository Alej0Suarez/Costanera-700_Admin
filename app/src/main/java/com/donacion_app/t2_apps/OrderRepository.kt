package com.donacion_app.t2_apps

import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

interface OrderRepository {
    suspend fun getPendingOrders(): Result<List<Order>>
    suspend fun updateOrderStatus(uid: String, orderId: String, newStatus: String): Result<Unit>
    suspend fun getCancelNotifications(): Result<List<CancelNotification>>
}

data class CancelNotification(
    val uid: String = "",
    val reason: String = "",
    val timestamp: Long = 0L
)

class FirebaseOrderRepository : OrderRepository {
    private val db = Firebase.firestore

    override suspend fun getPendingOrders(): Result<List<Order>> = try {
        val orders = mutableListOf<Order>()
        val usersSnapshot = db.collection("users_ad").get().await()

        for (userDoc in usersSnapshot.documents) {
            val uid = userDoc.id
            val ordersSnapshot = db.collection("users_ad")
                .document(uid)
                .collection("orders")
                .whereEqualTo("estado", "pendiente")
                .get().await()

            for (orderDoc in ordersSnapshot.documents) {
                val order = orderDoc.toObject(Order::class.java)
                if (order != null) {
                    orders.add(order.copy(uid = uid, orderId = orderDoc.id))
                }
            }
        }

        Result.success(orders)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateOrderStatus(uid: String, orderId: String, newStatus: String): Result<Unit> = try {
        db.collection("users_ad")
            .document(uid)
            .collection("orders")
            .document(orderId)
            .update("estado", newStatus)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getCancelNotifications(): Result<List<CancelNotification>> = try {
        val snapshot = db.collection("cancel_notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()

        val list = snapshot.documents.mapNotNull { it.toObject(CancelNotification::class.java) }
        Result.success(list)
    } catch (e: Exception) {
        Result.failure(e)
    }
}