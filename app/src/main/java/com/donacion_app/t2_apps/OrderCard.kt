package com.donacion_app.t2_apps

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OrderCard(order: Order, onUpdateEstado: (String) -> Unit) {
    var selectedEstado by remember { mutableStateOf(order.estado) }
    var expanded by remember { mutableStateOf(false) }

    val estados = listOf("pendiente", "confirmado", "en camino", "entregado", "rechazado")
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📌 Usuario ID: ${order.uid}", fontWeight = FontWeight.Bold)
            Text("📦 Tipo: ${order.deliveryOption}")
            Text("💰 Total: S/ ${"%.2f".format(order.totalPrice)}")
            Text("💳 Pago con: ${order.cashAmount}")
            Text("🟡 Estado actual: ${order.estado}")

            if (order.deliveryOption == "Delivery") {
                Text("📍 Ubicación: ${order.userLocation.ifBlank { "No disponible" }}")
                // Botón Google Maps (igual que antes)
            }

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
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    estados.forEach { estado ->
                        DropdownMenuItem(
                            onClick = {
                                selectedEstado = estado
                                expanded = false
                            },
                            text = { Text(estado.capitalize()) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onUpdateEstado(selectedEstado) },
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