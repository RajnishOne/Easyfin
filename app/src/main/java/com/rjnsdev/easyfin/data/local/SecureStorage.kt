package com.rjnsdev.easyfin.data.local

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.nio.charset.StandardCharsets

private val Context.dataStore by preferencesDataStore(name = "easyfin_secure_prefs")

class SecureStorage(private val context: Context) {

    private val aead: Aead

    init {
        AeadConfig.register()
        val keysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context, "easyfin_keyset", "easyfin_pref")
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri("android-keystore://easyfin_master_key")
            .build()
            .keysetHandle
        aead = keysetHandle.getPrimitive(Aead::class.java)
    }

    private fun encrypt(data: String): String {
        val encrypted = aead.encrypt(data.toByteArray(StandardCharsets.UTF_8), null)
        return Base64.encodeToString(encrypted, Base64.DEFAULT)
    }

    private fun decrypt(encryptedData: String): String? {
        return try {
            val decoded = Base64.decode(encryptedData, Base64.DEFAULT)
            val decrypted = aead.decrypt(decoded, null)
            String(decrypted, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun saveServerUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[SERVER_URL_KEY] = encrypt(url)
        }
    }

    val serverUrl: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[SERVER_URL_KEY]?.let { decrypt(it) }
    }

    suspend fun saveUsername(username: String) {
        context.dataStore.edit { prefs ->
            prefs[USERNAME_KEY] = encrypt(username)
        }
    }

    val username: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USERNAME_KEY]?.let { decrypt(it) }
    }

    suspend fun savePassword(password: String) {
        context.dataStore.edit { prefs ->
            prefs[PASSWORD_KEY] = encrypt(password)
        }
    }

    val password: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[PASSWORD_KEY]?.let { decrypt(it) }
    }

    suspend fun saveCustomHeader(header: String) {
        context.dataStore.edit { prefs ->
            prefs[CUSTOM_HEADER_KEY] = encrypt(header)
        }
    }

    val customHeader: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[CUSTOM_HEADER_KEY]?.let { decrypt(it) }
    }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[AUTH_TOKEN_KEY] = encrypt(token)
        }
    }

    val authToken: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[AUTH_TOKEN_KEY]?.let { decrypt(it) }
    }
    
    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    companion object {
        private val SERVER_URL_KEY = stringPreferencesKey("server_url")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val PASSWORD_KEY = stringPreferencesKey("password")
        private val CUSTOM_HEADER_KEY = stringPreferencesKey("custom_header")
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
    }
}
