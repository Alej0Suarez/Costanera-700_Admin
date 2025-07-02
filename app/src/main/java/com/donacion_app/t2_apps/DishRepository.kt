package com.donacion_app.t2_apps

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

interface DishRepository {
    suspend fun getAllDishes(): Result<List<DishModel>>
    suspend fun addDish(dish: DishModel): Result<Boolean>
    suspend fun deleteDish(id: String): Result<Boolean>
    suspend fun updateDish(dish: DishModel): Result<Unit>
}

class FirebaseDishRepository : DishRepository {
    private val db = Firebase.firestore

    override suspend fun getAllDishes(): Result<List<DishModel>> = try {
        val result = db.collection("dishes").get().await()
        val dishes = result.documents.mapNotNull { it.toObject(DishModel::class.java) }
        Result.success(dishes)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun addDish(dish: DishModel): Result<Boolean> = try {
        val docRef = db.collection("dishes").add(dish).await()
        docRef.update("id", docRef.id).await()
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteDish(id: String): Result<Boolean> = try {
        db.collection("dishes").document(id).delete().await()
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateDish(dish: DishModel): Result<Unit> {
        return try {
            val docRef = Firebase.firestore.collection("dishes").document(dish.id)
            docRef.set(dish).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

