package com.donacion_app.t2_apps

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import android.content.Context

class EditUserViewModel(
    private val repository: UserRepository = FirebaseUserRepository()
) : ViewModel() {

    var name by mutableStateOf("")
    var lastName by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var phone by mutableStateOf("")
    var imageUri by mutableStateOf<Uri?>(null)
    var imageUrl by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var initialUser: UserModel? = null

    suspend fun loadUser(userId: String) {
        isLoading = true
        val result = repository.getUserById(userId)
        result.getOrNull()?.let { user ->
            initialUser = user
            name = user.name
            lastName = user.lastName
            email = user.email
            password = user.password
            phone = user.phone
            imageUrl = user.imageUrl
        }
        isLoading = false
    }

    fun hasUnsavedChanges(): Boolean {
        val user = initialUser ?: return false
        return name != user.name ||
                lastName != user.lastName ||
                email != user.email ||
                password != user.password ||
                phone != user.phone ||
                imageUri != null
    }

    suspend fun saveChanges(context: Context, userId: String): Result<Unit> {
        if (name.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank() || phone.isBlank()) {
            return Result.failure(Exception("Todos los campos son obligatorios"))
        }

        var finalImageUrl = imageUrl
        if (imageUri != null) {
            val uploaded = uploadImageToFirebaseStorage(context, imageUri!!)
            if (uploaded != null) {
                finalImageUrl = uploaded
            } else {
                return Result.failure(Exception("Error al subir imagen"))
            }
        }

        val updatedUser = UserModel(
            id = userId,
            name = name,
            lastName = lastName,
            email = email,
            password = password,
            phone = phone,
            imageUrl = finalImageUrl
        )

        return repository.updateUser(updatedUser)
    }
}