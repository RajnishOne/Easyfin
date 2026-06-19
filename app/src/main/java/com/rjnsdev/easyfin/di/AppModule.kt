package com.rjnsdev.easyfin.di

import com.rjnsdev.easyfin.data.local.SecureStorage
import com.rjnsdev.easyfin.data.repository.AuthRepository
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import com.rjnsdev.easyfin.ui.auth.AuthViewModel

val appModule = module {

    single { SecureStorage(androidContext()) }

    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    single {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    singleOf(::AuthRepository)
    viewModelOf(::AuthViewModel)
}
