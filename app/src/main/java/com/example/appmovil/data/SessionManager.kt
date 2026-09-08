package com.example.appmovil.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class UserRole {
    ADMINISTRADOR,
    AUDITOR,
    CLIENTE
}

class SessionManager(context: Context) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val securePrefs: SharedPreferences = EncryptedSharedPreferences.create(
        "secure_user_session",
        masterKeyAlias,
        context.applicationContext,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // US01: Guardado de sesión y mapeo de rol
    suspend fun saveSession(token: String, userId: Int, username: String) = withContext(Dispatchers.IO) {
        val role = mapRoleFromId(userId)
        securePrefs.edit()
            .putString("auth_token", token)
            .putInt("user_id", userId)
            .putString("username", username)
            .putString("user_role", role.name)
            .putBoolean("is_logged_in", true)
            .apply()
    }

    // Regla de negocio US01: ID 1 y 2 = Admin, ID 3 = Auditor, resto = Cliente
    fun mapRoleFromId(userId: Int): UserRole {
        return when (userId) {
            1, 2 -> UserRole.ADMINISTRADOR
            3 -> UserRole.AUDITOR
            else -> UserRole.CLIENTE
        }
    }

    fun getToken(): String? = securePrefs.getString("auth_token", null)

    fun getUsername(): String = securePrefs.getString("username", "") ?: ""

    fun getUserRole(): UserRole {
        val roleName = securePrefs.getString("user_role", UserRole.CLIENTE.name)
        return try {
            UserRole.valueOf(roleName ?: UserRole.CLIENTE.name)
        } catch (_: Exception) {
            UserRole.CLIENTE
        }
    }

    // US02 - Escenario 3: Limpieza profunda de almacenamiento persistente
    suspend fun clearSession() = withContext(Dispatchers.IO) {
        securePrefs.edit().clear().commit() // commit() asegura escritura síncrona en disco
    }
}