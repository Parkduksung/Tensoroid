package com.example.tensoroid

import android.app.Application
import android.content.Context
import com.example.tensoroid.di.viewModelModule
import dagger.hilt.android.HiltAndroidApp
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        startKOIN()
    }

    fun context(): Context = applicationContext

    private fun startKOIN() {
        startKoin {
            androidContext(this@App)
            modules(
                listOf(
                    viewModelModule
                )
            )
        }
    }

    companion object {
        lateinit var instance: App
            private set
    }

}