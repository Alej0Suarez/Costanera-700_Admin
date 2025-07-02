package com.donacion_app.t2_apps

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

interface UserRepository {
    suspend fun getUserById(id: String): Result<UserModel?>
    suspend fun getCurrentUser(): Result<UserModel?>
    suspend fun signOut(): Result<Unit>
}

class FirebaseUserRepository : UserRepository {
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    override suspend fun getUserById(id: String): Result<UserModel?> = try {
        val doc = db.collection("users").document(id).get().await()
        Result.success(doc.toObject(UserModel::class.java))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getCurrentUser(): Result<UserModel?> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("No hay usuario"))
            getUserById(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> = try {
        auth.signOut()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}