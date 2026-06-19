package com.rjnsdev.easyfin.data.remote

import com.rjnsdev.easyfin.data.local.SecureStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class ApiClientManager(
    private val secureStorage: SecureStorage,
    private val okHttpClient: OkHttpClient,
    private val json: Json
) {
    private val _api = MutableStateFlow<JellyfinApi?>(null)
    val api: StateFlow<JellyfinApi?> = _api.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            secureStorage.activeProfile.collect { profile ->
                if (profile != null) {
                    val retrofit = Retrofit.Builder()
                        .baseUrl(profile.url)
                        .client(okHttpClient)
                        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                        .build()
                    _api.value = retrofit.create(JellyfinApi::class.java)
                } else {
                    _api.value = null
                }
            }
        }
    }
}
