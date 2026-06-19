package com.rjnsdev.easyfin.data.remote

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

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

interface JellyfinApi {

    @POST("Users/AuthenticateByName")
    suspend fun authenticate(
        @Header("Authorization") authorization: String,
        @Body request: AuthRequest
    ): Response<AuthResponse>
}
