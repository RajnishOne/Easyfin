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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets

private val Context.dataStore by preferencesDataStore(name = "easyfin_secure_prefs")

class SecureStorage(private val context: Context) {

    private val aead: Aead
    private val json = Json { ignoreUnknownKeys = true }

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

    // --- Profiles Management ---

    val serverProfiles: Flow<List<ServerProfile>> = context.dataStore.data.map { prefs ->
        val encryptedJson = prefs[SERVER_PROFILES_KEY] ?: return@map emptyList()
        val decryptedJson = decrypt(encryptedJson) ?: return@map emptyList()
        try {
            json.decodeFromString<List<ServerProfile>>(decryptedJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    val activeServerId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[ACTIVE_SERVER_ID_KEY]?.let { decrypt(it) }
    }

    val activeProfile: Flow<ServerProfile?> = context.dataStore.data.map { prefs ->
        val activeId = prefs[ACTIVE_SERVER_ID_KEY]?.let { decrypt(it) } ?: return@map null
        val encryptedJson = prefs[SERVER_PROFILES_KEY] ?: return@map null
        val decryptedJson = decrypt(encryptedJson) ?: return@map null
        try {
            val profiles = json.decodeFromString<List<ServerProfile>>(decryptedJson)
            profiles.find { it.id == activeId }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveProfile(profile: ServerProfile) {
        context.dataStore.edit { prefs ->
            // Retrieve existing
            val existingJson = prefs[SERVER_PROFILES_KEY]?.let { decrypt(it) }
            val profiles = try {
                if (existingJson != null) json.decodeFromString<List<ServerProfile>>(existingJson).toMutableList()
                else mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }

            // Update or add
            val index = profiles.indexOfFirst { it.id == profile.id }
            if (index != -1) {
                profiles[index] = profile
            } else {
                profiles.add(profile)
            }

            prefs[SERVER_PROFILES_KEY] = encrypt(json.encodeToString(profiles))
        }
    }

    suspend fun setActiveServerId(id: String) {
        context.dataStore.edit { prefs ->
            prefs[ACTIVE_SERVER_ID_KEY] = encrypt(id)
        }
    }

    suspend fun deleteProfile(id: String) {
        context.dataStore.edit { prefs ->
            val existingJson = prefs[SERVER_PROFILES_KEY]?.let { decrypt(it) }
            val profiles = try {
                if (existingJson != null) json.decodeFromString<List<ServerProfile>>(existingJson).toMutableList()
                else mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }

            profiles.removeAll { it.id == id }
            prefs[SERVER_PROFILES_KEY] = encrypt(json.encodeToString(profiles))

            // If active profile was deleted, clear active
            val activeId = prefs[ACTIVE_SERVER_ID_KEY]?.let { decrypt(it) }
            if (activeId == id) {
                prefs.remove(ACTIVE_SERVER_ID_KEY)
                // Optionally set to the first available profile if any
                if (profiles.isNotEmpty()) {
                    prefs[ACTIVE_SERVER_ID_KEY] = encrypt(profiles.first().id)
                }
            }
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    companion object {
        private val SERVER_PROFILES_KEY = stringPreferencesKey("server_profiles_v2")
        private val ACTIVE_SERVER_ID_KEY = stringPreferencesKey("active_server_id_v2")
    }
}
