package com.rjnsdev.easyfin.data.repository

import com.rjnsdev.easyfin.data.local.SecureStorage
import com.rjnsdev.easyfin.data.remote.ApiClientManager
import com.rjnsdev.easyfin.data.remote.BaseItemDto
import kotlinx.coroutines.flow.firstOrNull

class MediaRepository(
    private val apiClientManager: ApiClientManager,
    private val secureStorage: SecureStorage
) {
    suspend fun getActiveServerUrl(): String? {
        return secureStorage.activeProfile.firstOrNull()?.url
    }

    suspend fun getUserViews(): Result<List<BaseItemDto>> {
        return try {
            val api = apiClientManager.api.value ?: return Result.failure(Exception("No active server"))
            val profile = secureStorage.activeProfile.firstOrNull() ?: return Result.failure(Exception("No active profile"))
            
            val response = api.getUserViews(profile.userId)
            if (response.isSuccessful) {
                Result.success(response.body()?.Items ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch views: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLatestMedia(parentId: String? = null): Result<List<BaseItemDto>> {
        return try {
            val api = apiClientManager.api.value ?: return Result.failure(Exception("No active server"))
            val profile = secureStorage.activeProfile.firstOrNull() ?: return Result.failure(Exception("No active profile"))

            val response = api.getLatestMedia(profile.userId, parentId = parentId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch latest media: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
