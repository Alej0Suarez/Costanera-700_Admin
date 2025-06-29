package com.donacion_app.t2_apps

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

@Composable
fun RegisterUserScreenFirebase(rootNavController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val name = remember { mutableStateOf("") }
    val lastName = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val phone = remember { mutableStateOf("") }
    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }
    val isLoadingImage = remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { imageUri.value = it }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri.value = cameraImageUri.value
        }
    }

    fun createImageUri(): Uri? {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            Spacer(modifier = Modifier.height(16.dp))

            Text("Registro de Administrador", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            ) {
                if (imageUri.value != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri.value),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = "No Image", modifier = Modifier.size(60.dp))
                }
            }

            Row(modifier = Modifier.padding(top = 8.dp)) {
                Button(onClick = {
                    val items = listOf("Galería", "Cámara")
                    AlertDialog.Builder(context)
                        .setTitle("Seleccionar imagen")
                        .setItems(items.toTypedArray()) { _, which ->
                            if (which == 0) {
                                galleryLauncher.launch("image/*")
                            } else {
                                val uri = createImageUri()
                                cameraImageUri.value = uri
                                uri?.let { cameraLauncher.launch(it) }
                            }
                        }
                        .show()
                }) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Seleccionar")
                }

                Spacer(Modifier.width(8.dp))

                Button(onClick = { imageUri.value = null }) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                    Spacer(Modifier.width(4.dp))
                    Text("Eliminar", color = Color.Red)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name.value,
                onValueChange = { name.value = it },
                label = { Text("Nombres") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.9f),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                    focusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = lastName.value,
                onValueChange = { lastName.value = it },
                label = { Text("Apellidos") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.9f),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                    focusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text("Correo electrónico") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.9f),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                    focusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(0.9f),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                    focusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = phone.value,
                onValueChange = { phone.value = it },
                label = { Text("Teléfono") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.9f),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                    focusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (imageUri.value == null) {
                        Toast.makeText(context, "Selecciona una imagen", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    scope.launch {
                        isLoadingImage.value = true
                        val imageUrl = uploadImageToFirebaseStorage(context, imageUri.value!!)
                        isLoadingImage.value = false

                        if (imageUrl != null) {
                            val user = UserModel(
                                name = name.value,
                                lastName = lastName.value,
                                email = email.value,
                                password = password.value,
                                phone = phone.value,
                                imageUrl = imageUrl,
                                id = ""
                            )

                            val authSuccess = registerUserInFirebaseAuth(email.value, password.value)
                            if (authSuccess) {
                                val success = saveUserToFirestore(user)
                                Toast.makeText(
                                    context,
                                    if (success) "Administrador registrado" else "Error al guardar en Firestore",
                                    Toast.LENGTH_SHORT
                                ).show()
                                rootNavController.popBackStack()
                            } else {
                                Toast.makeText(context, "Error al registrar en Auth", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Error al subir imagen", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Registrar", color = Color.White)
            }

            if (isLoadingImage.value) {
                Spacer(modifier = Modifier.height(12.dp))
                CircularProgressIndicator()
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Al registrarte aceptas los Términos y Condiciones",
                style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}


//funcion para subir imagen

suspend fun uploadImageToFirebaseStorage(context: Context, uri: Uri): String? {
    return try {
        val storageRef = Firebase.storage.reference
        val fileName = "user_images/${UUID.randomUUID()}.jpg"
        val imageRef = storageRef.child(fileName)

        val stream = context.contentResolver.openInputStream(uri) ?: return null
        val uploadTask = imageRef.putStream(stream).await()
        imageRef.downloadUrl.await().toString()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


//funcion para guardar en firebase

suspend fun saveUserToFirestore(user: UserModel): Boolean {
    return try {
        val uid = Firebase.auth.currentUser?.uid ?: return false
        val db = Firebase.firestore
        db.collection("users").document(uid).set(user).await()  // 👈 guardamos con el UID como ID del documento
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

//funcion para autentication

suspend fun registerUserInFirebaseAuth(email: String, password: String): Boolean {
    return try {
        Firebase.auth.createUserWithEmailAndPassword(email, password).await()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}