package com.rjnsdev.easyfin.data.repository

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.rjnsdev.easyfin.data.local.SecureStorage
import com.rjnsdev.easyfin.data.remote.AuthRequest
import com.rjnsdev.easyfin.data.remote.JellyfinApi
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
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

            // Basic Jellyfin auth header formatting
            // In a real app this includes Device, DeviceId, Version, etc.
            val authHeaderPrefix = "MediaBrowser Client=\"Easyfin\", Device=\"Android\", DeviceId=\"device123\", Version=\"1.0\""
            val authHeader = if (!customHeader.isNullOrBlank()) {
                "$authHeaderPrefix, $customHeader"
            } else {
                authHeaderPrefix
            }

            val response = api.authenticate(authHeader, request)
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null) {
                    secureStorage.saveServerUrl(baseUrl)
                    secureStorage.saveUsername(request.Username)
                    secureStorage.savePassword(request.Pw)
                    if (!customHeader.isNullOrBlank()) {
                        secureStorage.saveCustomHeader(customHeader)
                    }
                    secureStorage.saveAuthToken(authResponse.AccessToken)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Empty response body"))
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
        val token = secureStorage.authToken.firstOrNull()
        return !token.isNullOrBlank()
    }
    
    suspend fun logout() {
        secureStorage.clear()
    }
}
