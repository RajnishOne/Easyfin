package com.rjnsdev.easyfin

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.rjnsdev.easyfin.di.appModule
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class EasyfinApp : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@EasyfinApp)
            modules(appModule)
        }
    }

    override fun newImageLoader(): ImageLoader {
        return get()
    }
}
