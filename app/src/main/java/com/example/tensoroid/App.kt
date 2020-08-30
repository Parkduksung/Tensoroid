package com.example.tensoroid

import android.app.Application
import android.content.Context

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    fun context(): Context = applicationContext

    companion object {
        lateinit var instance: App
            private set
    }

}