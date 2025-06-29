package com.donacion_app.t2_apps

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ProfileScreen(
    navController: NavHostController,
    rootNavController: NavHostController
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var user by remember { mutableStateOf<UserModel?>(null) }
    val uid = auth.currentUser?.uid

    // Cargar datos del usuario
    LaunchedEffect(uid) {
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        user = document.toObject(UserModel::class.java)
                    } else {
                        Toast.makeText(context, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al cargar usuario", Toast.LENGTH_SHORT).show()
                }
        }
    }

    user?.let { currentUser ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFDF6EC))
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen de perfil
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(currentUser.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFFB78018), CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Nombre
            Text(
                text = "${currentUser.name} ${currentUser.lastName}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E2723)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Correo
            Text(
                text = currentUser.email,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón editar
            Button(
                onClick = { navController.navigate("edit_user/$uid") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB78018)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Editar datos", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón cerrar sesión
            Button(
                onClick = {
                    auth.signOut()
                    rootNavController.navigate("login") {
                        popUpTo(rootNavController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar sesión", color = Color.White)
            }
        }
    } ?: run {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFDF6EC)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFFB78018))
        }
    }
}