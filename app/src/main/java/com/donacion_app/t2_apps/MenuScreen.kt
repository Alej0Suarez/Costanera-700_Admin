package com.donacion_app.t2_apps

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun MenuScreen(navController: NavHostController, viewModel: MenuViewModel = viewModel()) {
    val context = LocalContext.current
    var dishToDelete by remember { mutableStateOf<DishModel?>(null) }

    LaunchedEffect(true) { viewModel.loadDishes() }

    Scaffold(
        topBar = {
            Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFB78018)) // Dorado
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Menú del Restaurante",
                color = Color.White,
                style = TextStyle(
                    fontSize = 26.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    letterSpacing = 1.2.sp
                )
            )
        }},
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("register_dish") },
                containerColor = Color(0xFFB78018),
                contentColor = Color.White,
                shape = CircleShape) {
                Icon(Icons.Default.Add, contentDescription = "Agregar plato")
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = Color(0xFFFFF8E1) // Fondo general crema claro
    ) { innerPadding ->
        if (viewModel.dishes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No hay platos registrados", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFFFFF8E1)) // Fondo crema claro
            ) {
                items(viewModel.dishes) { dish ->
                    DishCard(
                        dish = dish,
                        onEditClick = { navController.navigate("edit_dish/${dish.id}") },
                        onDeleteClick = { dishToDelete = dish }
                    )
                }
            }
        }

        if (dishToDelete != null) {
            AlertDialog(
                onDismissRequest = { dishToDelete = null },
                title = { Text("Eliminar plato") },
                text = { Text("¿Eliminar '${dishToDelete?.name}'?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteDish(
                            id = dishToDelete!!.id,
                            onSuccess = {
                                Toast.makeText(context, "Eliminado", Toast.LENGTH_SHORT).show()
                                viewModel.loadDishes()
                                dishToDelete = null
                            },
                            onError = {
                                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                dishToDelete = null
                            }
                        )
                    }) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { dishToDelete = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}