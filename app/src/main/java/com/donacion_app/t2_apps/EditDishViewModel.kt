package com.donacion_app.t2_apps

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class EditDishViewModel(
    private val repository: DishRepository = FirebaseDishRepository()
) : ViewModel() {

    var name by mutableStateOf("")
    var description by mutableStateOf("")
    var price by mutableStateOf("")
    var category by mutableStateOf("")
    var imageUrl by mutableStateOf("")
    var imageUri by mutableStateOf<Uri?>(null)
    var isLoading by mutableStateOf(false)
    var initialDish: DishModel? = null

    suspend fun loadDish(id: String) {
        isLoading = true
        try {
            val doc = Firebase.firestore.collection("dishes").document(id).get().await()
            val dish = doc.toObject(DishModel::class.java)
            dish?.let {
                initialDish = it
                name = it.name
                description = it.description
                price = it.price.toString()
                category = it.category
                imageUrl = it.imageUrl
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isLoading = false
    }

    fun hasUnsavedChanges(): Boolean {
        val initial = initialDish ?: return false
        return name != initial.name ||
                description != initial.description ||
                price != initial.price.toString() ||
                category != initial.category ||
                imageUri != null
    }

    suspend fun saveChanges(context: Context, dishId: String): Result<Unit> {
        val priceDouble = price.toDoubleOrNull() ?: return Result.failure(Exception("Precio inválido"))

        var finalImageUrl = imageUrl
        if (imageUri != null) {
            val uploaded = uploadImageToFirebaseStorage(context, imageUri!!)
            if (uploaded != null) finalImageUrl = uploaded
            else return Result.failure(Exception("Error al subir imagen"))
        }

        val dish = DishModel(
            id = dishId,
            name = name,
            description = description,
            price = priceDouble,
            category = category,
            imageUrl = finalImageUrl
        )

        return repository.updateDish(dish)
    }
}