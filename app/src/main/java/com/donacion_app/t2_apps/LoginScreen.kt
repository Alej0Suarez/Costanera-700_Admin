package com.donacion_app.t2_apps

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun LoginScreen(rootNavController: NavHostController) {
    val context = LocalContext.current
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val recoveryEmail = remember { mutableStateOf("") }
    val showDialog = remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val isLoading = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo_restaurante), // Fondo de restaurante
            contentDescription = "Fondo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.White, shape = CircleShape)
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Administrador",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text("Correo electrónico") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.9f),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                    focusedContainerColor = Color.White
                ),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                ),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    scope.launch {
                        isLoading.value = true
                        val result = loginUser(email.value, password.value)
                        isLoading.value = false
                        if (result) {
                            rootNavController.navigate("main") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            Toast.makeText(context, "Error de login", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Iniciar Sesión", color = Color.White)
            }

            if (isLoading.value) {
                Spacer(modifier = Modifier.height(12.dp))
                CircularProgressIndicator()
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showDialog.value = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(50.dp)
            ) {
                Text("Olvidé mi contraseña", color = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Email, contentDescription = null, tint = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(onClick = {
                rootNavController.navigate("register")
            }) {
                Text("¿No tienes cuenta? Regístrate aquí", color = Color.White)
            }
        }
    }

    // Diálogo de recuperación de contraseña
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showDialog.value = false
                recoveryEmail.value = ""
            },
            title = { Text("Recuperar contraseña") },
            text = {
                Column {
                    Text("Ingresa tu correo registrado")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = recoveryEmail.value,
                        onValueChange = { recoveryEmail.value = it },
                        label = { Text("Correo electrónico") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (recoveryEmail.value.isNotBlank()) {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(recoveryEmail.value)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Correo de recuperación enviado", Toast.LENGTH_LONG).show()
                                recoveryEmail.value = ""
                                showDialog.value = false
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(context, "Correo requerido", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Enviar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog.value = false
                    recoveryEmail.value = ""
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// Función de login usando Firebase Auth
suspend fun loginUser(email: String, password: String): Boolean {
    return try {
        Firebase.auth.signInWithEmailAndPassword(email, password).await()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}