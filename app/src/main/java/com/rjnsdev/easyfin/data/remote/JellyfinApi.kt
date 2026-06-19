package com.rjnsdev.easyfin.data.remote

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class AuthRequest(
    val Username: String,
    val Pw: String
)

@Serializable
data class AuthResponse(
    val AccessToken: String,
    val User: UserDto? = null
)

@Serializable
data class UserDto(
    val Id: String,
    val Name: String
)

@Serializable
data class BaseItemDto(
    val Id: String,
    val Name: String? = null,
    val ServerId: String? = null,
    val RunTimeTicks: Long? = null,
    val ProductionYear: Int? = null,
    val Type: String? = null, // e.g., Movie, Series, Episode
    val CollectionType: String? = null, // e.g., movies, tvshows
    val ImageTags: Map<String, String>? = null,
    val BackdropImageTags: List<String>? = null
)

@Serializable
data class BaseItemDtoQueryResult(
    val Items: List<BaseItemDto>,
    val TotalRecordCount: Int
)

interface JellyfinApi {

    @POST("Users/AuthenticateByName")
    suspend fun authenticate(
        @Header("Authorization") authorization: String,
        @Body request: AuthRequest
    ): Response<AuthResponse>

    @GET("Users/{userId}/Views")
    suspend fun getUserViews(
        @Path("userId") userId: String
    ): Response<BaseItemDtoQueryResult>

    @GET("Users/{userId}/Items/Latest")
    suspend fun getLatestMedia(
        @Path("userId") userId: String,
        @Query("Limit") limit: Int = 16,
        @Query("ParentId") parentId: String? = null,
        @Query("Fields") fields: String = "PrimaryImageAspectRatio,BasicSyncInfo",
        @Query("IncludeItemTypes") includeItemTypes: String = "Movie,Series,Episode"
    ): Response<List<BaseItemDto>>
}
