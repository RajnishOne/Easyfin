package com.rjnsdev.easyfin.data.repository

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.rjnsdev.easyfin.data.local.SecureStorage
import com.rjnsdev.easyfin.data.local.ServerProfile
import com.rjnsdev.easyfin.data.remote.AuthRequest
import com.rjnsdev.easyfin.data.remote.JellyfinApi
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.UUID

class AuthRepository(
    private val secureStorage: SecureStorage,
    private val okHttpClient: OkHttpClient,
    private val json: Json
) {

    suspend fun authenticate(serverUrl: String, request: AuthRequest, customHeader: String?): Result<Unit> {
        return try {
            val baseUrl = if (serverUrl.endsWith("/")) serverUrl else "$serverUrl/"
            
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()

            val api = retrofit.create(JellyfinApi::class.java)

            val authHeaderPrefix = "MediaBrowser Client=\"Easyfin\", Device=\"Android\", DeviceId=\"device123\", Version=\"1.0\""
            val authHeader = if (!customHeader.isNullOrBlank()) {
                "$authHeaderPrefix, $customHeader"
            } else {
                authHeaderPrefix
            }

            val response = api.authenticate(authHeader, request)
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null && authResponse.User != null) {
                    val serverId = UUID.randomUUID().toString()
                    val profile = ServerProfile(
                        id = serverId,
                        name = authResponse.User.Name,
                        url = baseUrl,
                        username = request.Username,
                        userId = authResponse.User.Id,
                        accessToken = authResponse.AccessToken,
                        customHeader = customHeader ?: ""
                    )
                    secureStorage.saveProfile(profile)
                    secureStorage.setActiveServerId(serverId)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Empty response body or missing User info"))
                }
            } else {
                Result.failure(Exception("Authentication failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun isLoggedIn(): Boolean {
        val activeId = secureStorage.activeServerId.firstOrNull()
        return !activeId.isNullOrBlank()
    }
    
    suspend fun logoutActive() {
        val activeId = secureStorage.activeServerId.firstOrNull()
        if (activeId != null) {
            secureStorage.deleteProfile(activeId)
        }
    }

    suspend fun logoutAll() {
        secureStorage.clearAll()
    }
}
