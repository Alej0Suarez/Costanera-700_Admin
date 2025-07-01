package com.donacion_app.t2_apps

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Boolean>
    suspend fun sendRecoveryEmail(email: String): Result<Boolean>
}

class FirebaseAuthRepository : AuthRepository {
    override suspend fun login(email: String, password: String): Result<Boolean> = try {
        Firebase.auth.signInWithEmailAndPassword(email, password).await()
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun sendRecoveryEmail(email: String): Result<Boolean> = try {
        Firebase.auth.sendPasswordResetEmail(email).await()
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
