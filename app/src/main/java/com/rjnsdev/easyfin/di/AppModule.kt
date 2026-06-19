package com.rjnsdev.easyfin.di

import com.rjnsdev.easyfin.data.local.SecureStorage
import com.rjnsdev.easyfin.data.repository.AuthRepository
import com.rjnsdev.easyfin.data.repository.MediaRepository
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.rjnsdev.easyfin.data.remote.ApiClientManager
import okhttp3.Interceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import com.rjnsdev.easyfin.MainViewModel
import com.rjnsdev.easyfin.ui.auth.AuthViewModel
import com.rjnsdev.easyfin.ui.dashboard.collection.CollectionViewModel
import com.rjnsdev.easyfin.ui.dashboard.explore.ExploreViewModel
import com.rjnsdev.easyfin.ui.dashboard.settings.SettingsViewModel
import com.rjnsdev.easyfin.ui.player.MediaPlayerViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

val appModule = module {

    single { SecureStorage(androidContext()) }

    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    single {
        val logging = HttpLoggingInterceptor { message ->
            android.util.Log.d("EasyfinAPI", message)
        }.apply {
            // Only log bodies in debug mode, or keep it BODY for now
            level = HttpLoggingInterceptor.Level.BODY
            
            // Redact sensitive headers
            redactHeader("Authorization")
            redactHeader("X-Emby-Token")
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    singleOf(::ApiClientManager)
    
    singleOf(::AuthRepository)
    singleOf(::MediaRepository)
    viewModelOf(::MainViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ExploreViewModel)
    viewModelOf(::CollectionViewModel)
    viewModelOf(::MediaPlayerViewModel)

    single {
        val secureStorage: SecureStorage = get()
        val baseOkHttpClient: OkHttpClient = get()

        val authInterceptor = Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            // We need to inject the X-Emby-Token for images if we have an active profile
            val activeProfile = runBlocking { secureStorage.activeProfile.firstOrNull() }
            if (activeProfile != null) {
                // Determine if we need to add standard auth header
                val authHeaderPrefix = "MediaBrowser Client=\"Easyfin\", Device=\"Android\", DeviceId=\"device123\", Version=\"1.0\", Token=\"${activeProfile.accessToken}\""
                val authHeader = if (activeProfile.customHeader.isNotBlank()) {
                    "$authHeaderPrefix, ${activeProfile.customHeader}"
                } else {
                    authHeaderPrefix
                }
                requestBuilder.addHeader("X-Emby-Token", activeProfile.accessToken)
                requestBuilder.addHeader("Authorization", authHeader)
            }
            chain.proceed(requestBuilder.build())
        }

        val imageOkHttpClient = baseOkHttpClient.newBuilder()
            .addInterceptor(authInterceptor)
            .build()

        ImageLoader.Builder(androidContext())
            .okHttpClient(imageOkHttpClient)
            .memoryCache {
                MemoryCache.Builder(androidContext())
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(androidContext().cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .build()
    }
}
