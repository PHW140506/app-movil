package com.example.appmovil.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

enum class UserRole {
    ADMINISTRADOR,
    AUDITOR,
    CLIENTE
}

class SessionManager(context: Context) {

    // Clave maestra criptográfica para cifrado nativo por hardware
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    // SharedPreferences cifradas
    private val securePrefs: SharedPreferences = EncryptedSharedPreferences.create(
        "secure_user_session",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveSession(token: String, userId: Int) {
        val role = mapRoleFromId(userId)
        securePrefs.edit()
            .putString("auth_token", token)
            .putInt("user_id", userId)
            .putString("user_role", role.name)
            .putBoolean("is_logged_in", true)
            .apply()
    }

    // Regla de negocio: IDs 1 y 2 -> Admin, 3 -> Auditor, restantes -> Cliente
    fun mapRoleFromId(userId: Int): UserRole {
        return when (userId) {
            1, 2 -> UserRole.ADMINISTRADOR
            3 -> UserRole.AUDITOR
            else -> UserRole.CLIENTE
        }
    }

    fun getToken(): String? = securePrefs.getString("auth_token", null)

    fun getUserRole(): UserRole {
        val roleName = securePrefs.getString("user_role", UserRole.CLIENTE.name)
        return try {
            UserRole.valueOf(roleName ?: UserRole.CLIENTE.name)
        } catch (e: Exception) {
            UserRole.CLIENTE
        }
    }

    fun clearSession() {
        securePrefs.edit().clear().apply()
    }
}