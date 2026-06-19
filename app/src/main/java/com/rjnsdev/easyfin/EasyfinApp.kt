package com.rjnsdev.easyfin

import android.app.Application
import com.rjnsdev.easyfin.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class EasyfinApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@EasyfinApp)
            modules(appModule)
        }
    }
}
