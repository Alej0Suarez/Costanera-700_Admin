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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun EditUserScreen(navController: NavHostController, userId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val initialUser = remember { mutableStateOf<UserModel?>(null) }

    val name = remember { mutableStateOf("") }
    val lastName = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val phone = remember { mutableStateOf("") }

    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val imageUrl = remember { mutableStateOf("") }
    val isLoadingImage = remember { mutableStateOf(false) }

    val showExitDialog = remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        val doc = Firebase.firestore.collection("users").document(userId).get().await()
        val user = doc.toObject(UserModel::class.java)
        user?.let {
            initialUser.value = it
            name.value = it.name
            lastName.value = it.lastName
            email.value = it.email
            password.value = it.password
            phone.value = it.phone
            imageUrl.value = it.imageUrl
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { imageUri.value = it }
    }

    fun hasUnsavedChanges(): Boolean {
        val initial = initialUser.value ?: return false
        return name.value != initial.name ||
                lastName.value != initial.lastName ||
                email.value != initial.email ||
                password.value != initial.password ||
                phone.value != initial.phone ||
                imageUri.value != null
    }

    BackHandler(enabled = hasUnsavedChanges()) {
        showExitDialog.value = true
    }

    if (showExitDialog.value) {
        AlertDialog(
            onDismissRequest = { showExitDialog.value = false },
            title = { Text("¿Salir sin guardar?", fontWeight = FontWeight.Bold) },
            text = { Text("Tienes cambios sin guardar. ¿Estás seguro de que deseas salir?") },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog.value = false
                    navController.popBackStack()
                }) {
                    Text("Salir", color = Color.Red)
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
            .background(Color(0xFFFFFBF3))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Editar Usuario",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB78018)
        )

        Spacer(Modifier.height(20.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Color(0xFFFDE7C2))
        ) {
            when {
                imageUri.value != null -> {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri.value),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                imageUrl.value.isNotEmpty() -> {
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl.value),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                else -> {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(60.dp), tint = Color.Gray)
                }
            }
        }

        TextButton(onClick = { galleryLauncher.launch("image/*") }) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color(0xFFB78018))
            Spacer(Modifier.width(6.dp))
            Text("Cambiar imagen", color = Color(0xFFB78018))
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = name.value, onValueChange = { name.value = it },
            label = { Text("Nombres") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = lastName.value, onValueChange = { lastName.value = it },
            label = { Text("Apellidos") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = email.value, onValueChange = { email.value = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password.value, onValueChange = { password.value = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = phone.value, onValueChange = { phone.value = it },
            label = { Text("Teléfono") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                scope.launch {
                    if (name.value.isBlank() || lastName.value.isBlank() || email.value.isBlank() || password.value.isBlank() || phone.value.isBlank()) {
                        Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    var finalImageUrl = imageUrl.value

                    if (imageUri.value != null) {
                        isLoadingImage.value = true
                        val uploadedUrl = uploadImageToFirebaseStorage(context, imageUri.value!!)
                        isLoadingImage.value = false
                        if (uploadedUrl != null) {
                            finalImageUrl = uploadedUrl
                        } else {
                            Toast.makeText(context, "Error al subir imagen", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                    }

                    val updatedUser = UserModel(
                        id = userId,
                        name = name.value,
                        lastName = lastName.value,
                        email = email.value,
                        password = password.value,
                        phone = phone.value,
                        imageUrl = finalImageUrl
                    )

                    try {
                        Firebase.firestore.collection("users").document(userId).set(updatedUser).await()
                        Toast.makeText(context, "Usuario actualizado", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFB78018),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Guardar Cambios")
        }

        if (isLoadingImage.value) {
            Spacer(modifier = Modifier.height(12.dp))
            CircularProgressIndicator()
        }
    }
}