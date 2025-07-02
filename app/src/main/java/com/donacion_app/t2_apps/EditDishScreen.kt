package com.donacion_app.t2_apps

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun EditDishScreen(navController: NavHostController, dishId: String, viewModel: EditDishViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val showExitDialog = remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.imageUri = it }
    }

    LaunchedEffect(dishId) {
        viewModel.loadDish(dishId)
    }

    BackHandler(enabled = viewModel.hasUnsavedChanges()) {
        showExitDialog.value = true
    }

    if (showExitDialog.value) {
        AlertDialog(
            onDismissRequest = { showExitDialog.value = false },
            title = { Text("¿Salir sin guardar?") },
            text = { Text("Tienes cambios sin guardar. ¿Deseas salir?") },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog.value = false
                    navController.popBackStack()
                }) {
                    Text("Salir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog.value = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Editar Plato", style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold, color = Color(0xFFB78018)
        ))

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier.size(160.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFFEFEFEF)),
            contentAlignment = Alignment.Center
        ) {
            when {
                viewModel.imageUri != null -> Image(
                    painter = rememberAsyncImagePainter(viewModel.imageUri),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                viewModel.imageUrl.isNotEmpty() -> Image(
                    painter = rememberAsyncImagePainter(viewModel.imageUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                else -> Icon(Icons.Default.Fastfood, contentDescription = null, modifier = Modifier.size(60.dp))
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(onClick = { galleryLauncher.launch("image/*") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB78018))
        ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(4.dp))
            Text("Cambiar Imagen", color = Color.White)
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = viewModel.name,
            onValueChange = { viewModel.name = it },
            label = { Text("Nombre del plato") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.description,
            onValueChange = { viewModel.description = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.price,
            onValueChange = { viewModel.price = it },
            label = { Text("Precio") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.category,
            onValueChange = { viewModel.category = it },
            label = { Text("Categoría") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                scope.launch {
                    val result = viewModel.saveChanges(context, dishId)
                    result.fold(
                        onSuccess = {
                            Toast.makeText(context, "Plato actualizado", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        },
                        onFailure = {
                            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Guardar Cambios", color = Color.White)
        }

        if (viewModel.isLoading) {
            Spacer(Modifier.height(12.dp))
            CircularProgressIndicator()
        }
    }
}