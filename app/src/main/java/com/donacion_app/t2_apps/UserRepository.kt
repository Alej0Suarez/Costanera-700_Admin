package com.donacion_app.t2_apps

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

interface UserRepository {
    suspend fun getUserById(id: String): Result<UserModel?>
    suspend fun updateUser(user: UserModel): Result<Unit>
}

class FirebaseUserRepository : UserRepository {
    private val db = Firebase.firestore

    override suspend fun getUserById(id: String): Result<UserModel?> = try {
        val doc = db.collection("users").document(id).get().await()
        val user = doc.toObject(UserModel::class.java)
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateUser(user: UserModel): Result<Unit> = try {
        db.collection("users").document(user.id).set(user).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}