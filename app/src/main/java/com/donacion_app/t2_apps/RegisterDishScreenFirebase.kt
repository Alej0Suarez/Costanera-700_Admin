package com.donacion_app.t2_apps

import android.app.AlertDialog
import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun RegisterDishScreenFirebase(
    navController: NavHostController,
    repository: DishRepository = FirebaseDishRepository()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val name = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val price = remember { mutableStateOf("") }
    val category = remember { mutableStateOf("") }

    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }
    val isLoading = remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { imageUri.value = it }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) imageUri.value = cameraImageUri.value
    }

    fun createImageUri(): Uri? {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Registrar Plato",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB78018)
            )
        )

        Spacer(Modifier.height(16.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFE0E0E0))
        ) {
            if (imageUri.value != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri.value),
                    contentDescription = "Dish Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.Fastfood, contentDescription = "No Image", modifier = Modifier.size(60.dp))
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.padding(bottom = 8.dp)) {
            Button(
                onClick = {
                    val options = listOf("Galería", "Cámara")
                    AlertDialog.Builder(context)
                        .setTitle("Seleccionar imagen")
                        .setItems(options.toTypedArray()) { _, index ->
                            if (index == 0) {
                                galleryLauncher.launch("image/*")
                            } else {
                                val uri = createImageUri()
                                cameraImageUri.value = uri
                                uri?.let { cameraLauncher.launch(it) }
                            }
                        }
                        .show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB78018))
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(4.dp))
                Text("Foto", color = Color.White)
            }

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = { imageUri.value = null },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(4.dp))
                Text("Eliminar", color = Color.White)
            }
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = name.value,
            onValueChange = { name.value = it },
            label = { Text("Nombre del plato") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = description.value,
            onValueChange = { description.value = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = price.value,
            onValueChange = { price.value = it },
            label = { Text("Precio") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = category.value,
            onValueChange = { category.value = it },
            label = { Text("Categoría") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                if (imageUri.value == null) {
                    Toast.makeText(context, "Selecciona una imagen", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                scope.launch {
                    isLoading.value = true
                    val imageUrl = uploadImageToFirebaseStorage(context, imageUri.value!!)
                    if (imageUrl != null) {
                        val dish = DishModel(
                            name = name.value,
                            description = description.value,
                            price = price.value.toDoubleOrNull() ?: 0.0,
                            category = category.value,
                            imageUrl = imageUrl
                        )
                        val result = repository.addDish(dish)
                        result.fold(
                            onSuccess = {
                                Toast.makeText(context, "Plato registrado", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            },
                            onFailure = {
                                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        Toast.makeText(context, "Error al subir imagen", Toast.LENGTH_SHORT).show()
                    }
                    isLoading.value = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Registrar", color = Color.White)
        }

        if (isLoading.value) {
            Spacer(modifier = Modifier.height(12.dp))
            CircularProgressIndicator()
        }
    }
}

suspend fun saveDishToFirestore(dish: DishModel): Boolean {
    return try {
        val db = Firebase.firestore
        val docRef = db.collection("dishes").add(dish).await()
        docRef.update("id", docRef.id) // Guardamos el ID dentro del documento
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}