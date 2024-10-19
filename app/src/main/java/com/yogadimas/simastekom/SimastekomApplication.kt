package com.yogadimas.simastekom

import android.app.Application
import com.yogadimas.simastekom.di.koin.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class SimastekomApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        // Start Koin
        startKoin {
            androidLogger()
            androidContext(this@SimastekomApplication)
            modules(appModule)
        }
    }

}