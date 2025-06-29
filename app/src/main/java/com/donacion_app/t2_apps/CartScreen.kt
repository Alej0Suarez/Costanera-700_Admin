package com.donacion_app.t2_apps

import android.annotation.SuppressLint
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("QueryPermissionsNeeded", "SimpleDateFormat")
@Composable
fun CartScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var cancelNotifications by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            isLoading = true
            orders = getAllPendingOrders()

            db.collection("cancel_notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    cancelNotifications = result.documents.map { it.data ?: emptyMap() }
                }

            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pedidos Pendientes") },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color(0xFFB78018),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFB78018))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFFDF6EC))
            ) {
                // 🔔 Notificaciones de cancelación
                if (cancelNotifications.isNotEmpty()) {
                    item {
                        Text(
                            "Notificaciones de Cancelación",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFFB78018),
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    items(cancelNotifications) { notif ->
                        val uid = notif["uid"] ?: "N/A"
                        val reason = notif["reason"] ?: "Motivo no especificado"
                        val timestamp = notif["timestamp"] as? Long
                        val formattedDate = timestamp?.let {
                            SimpleDateFormat("dd/MM/yyyy HH:mm")
                                .format(Date(it))
                        } ?: ""

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("👤 Usuario: $uid")
                                Text("❌ Motivo: $reason")
                                Text("📅 Fecha: $formattedDate")
                            }
                        }
                    }
                }

                // 📦 Pedidos
                items(orders) { order ->
                    var selectedEstado by remember { mutableStateOf("confirmado") }
                    val estados = listOf("pendiente", "confirmado", "en camino", "entregado", "rechazado")
                    var expanded by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("📌 Usuario ID: ${order.uid}", fontWeight = FontWeight.Bold)
                            Text("📦 Tipo: ${order.deliveryOption}")
                            Text("💰 Total: S/ ${"%.2f".format(order.totalPrice)}")
                            Text("💳 Pago con: ${order.cashAmount}")
                            Text("🟡 Estado actual: ${order.estado}")

                            if (order.deliveryOption == "Delivery") {
                                Spacer(Modifier.height(8.dp))
                                Text("📍 Ubicación del usuario:", fontWeight = FontWeight.Medium)
                                Text(order.userLocation.ifBlank { "No disponible" })

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = {
                                        val parts = order.userLocation.split(",")
                                        if (parts.size == 2) {
                                            val lat = parts[0].toDoubleOrNull()
                                            val lon = parts[1].toDoubleOrNull()
                                            if (lat != null && lon != null) {
                                                val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon")
                                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                                if (intent.resolveActivity(context.packageManager) != null) {
                                                    context.startActivity(intent)
                                                } else {
                                                    Toast.makeText(context, "No se encontró una app de mapas", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } else {
                                            Toast.makeText(context, "Ubicación no válida", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB78018))
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ver en Google Maps", color = Color.White)
                                }

                                Spacer(Modifier.height(4.dp))
                                Text("⏱ Tiempo estimado: ${order.estimatedTime}")
                                Text("🚚 Costo de delivery: S/ ${"%.2f".format(order.estimatedCost)}")
                            }

                            Spacer(Modifier.height(8.dp))
                            Text("🍽 Productos:", fontWeight = FontWeight.SemiBold)
                            order.items.forEach {
                                Text("• ${it.dishName} x${it.quantity} (S/ ${"%.2f".format(it.subtotal)})")
                            }

                            Spacer(Modifier.height(12.dp))
                            Text("🔄 Cambiar estado del pedido:")

                            Box {
                                Button(onClick = { expanded = true }) {
                                    Text("Estado: $selectedEstado")
                                }

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    estados.forEach { estado ->
                                        DropdownMenuItem(
                                            onClick = {
                                                selectedEstado = estado
                                                expanded = false
                                            },
                                            text = { Text(estado.replaceFirstChar { it.uppercase() }) }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    val uid = order.uid
                                    val orderId = order.orderId

                                    if (uid != null && orderId != null) {
                                        db.collection("users_ad")
                                            .document(uid)
                                            .collection("orders")
                                            .document(orderId)
                                            .update("estado", selectedEstado)
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Estado actualizado a '$selectedEstado'", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(context, "Error al actualizar estado", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                Spacer(Modifier.width(6.dp))
                                Text("Actualizar Estado", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

suspend fun getAllPendingOrders(): List<Order> {
    val db = Firebase.firestore
    val allOrders = mutableListOf<Order>()

    try {
        val usersSnapshot = db.collection("users_ad").get().await()
        for (userDoc in usersSnapshot.documents) {
            val uid = userDoc.id
            val ordersSnapshot = db.collection("users_ad")
                .document(uid)
                .collection("orders")
                .whereEqualTo("estado", "pendiente")
                .get()
                .await()

            for (orderDoc in ordersSnapshot.documents) {
                val order = orderDoc.toObject(Order::class.java)
                if (order != null) {
                    allOrders.add(order.copy(uid = uid, orderId = orderDoc.id))
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return allOrders
}