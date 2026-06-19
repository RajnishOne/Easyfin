package com.rjnsdev.easyfin.data.local

import kotlinx.serialization.Serializable

@Serializable
data class ServerProfile(
    val id: String,
    val name: String,
    val url: String,
    val username: String,
    val userId: String,
    val accessToken: String,
    val customHeader: String = ""
)
