package com.example.controlfinancierocompose.ui.credentials

import kotlinx.serialization.Serializable
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import android.content.Context

// Modelo para credenciales de acceso por titular
// Se asocia a cada cuenta/plataforma y titular
@Serializable
data class Credential(
    val platformId: Long, // banco o plataforma de inversión
    val accountId: Long?, // cuenta bancaria (puede ser null para inversiones)
    val holder: String,   // nombre del titular
    val username: String?,
    val password: String?
)

// Utilidad para guardar y recuperar credenciales de forma segura
object CredentialsStorage {
    // Obtiene todas las credenciales guardadas como lista serializable
    fun getAllCredentials(context: Context): List<Credential> {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val prefs = EncryptedSharedPreferences.create(
            PREFS_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val credentials = mutableListOf<Credential>()
        for ((key, value) in prefs.all) {
            if (value is String) {
                val parts = value.split("|")
                // Key format: platformId_accountId_holder
                val keyParts = key.split("_")
                val platformId = keyParts.getOrNull(0)?.toLongOrNull() ?: continue
                val accountId = keyParts.getOrNull(1)?.let { if (it == "none") null else it.toLongOrNull() }
                val holder = keyParts.getOrNull(2) ?: continue
                credentials.add(Credential(platformId, accountId, holder, parts.getOrNull(0), parts.getOrNull(1)))
            }
        }
        return credentials
    }

    // Guarda una lista de credenciales (sobrescribe las existentes con la misma clave)
    fun saveAllCredentials(context: Context, credentials: List<Credential>) {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val prefs = EncryptedSharedPreferences.create(
            PREFS_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val editor = prefs.edit()
        for (credential in credentials) {
            val key = credentialKey(credential)
            editor.putString(key, "${credential.username}|${credential.password}")
        }
        editor.apply()
    }
    const val PREFS_NAME = "secure_credentials"

    fun saveCredential(context: Context, credential: Credential) {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val prefs = EncryptedSharedPreferences.create(
            PREFS_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val key = credentialKey(credential)
        prefs.edit().putString(key, "${credential.username}|${credential.password}").apply()
    }

    fun getCredential(context: Context, platformId: Long, accountId: Long?, holder: String): Credential? {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val prefs = EncryptedSharedPreferences.create(
            PREFS_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val key = credentialKey(platformId, accountId, holder)
        val value = prefs.getString(key, null) ?: return null
        val parts = value.split("|")
        return Credential(platformId, accountId, holder, parts.getOrNull(0), parts.getOrNull(1))
    }

    private fun credentialKey(credential: Credential): String = credentialKey(credential.platformId, credential.accountId, credential.holder)
    private fun credentialKey(platformId: Long, accountId: Long?, holder: String): String = "${platformId}_${accountId ?: "none"}_$holder"
}
