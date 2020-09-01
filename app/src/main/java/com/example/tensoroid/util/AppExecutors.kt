package com.example.tensoroid.util

import android.os.Handler
import android.os.Looper
import org.koin.dsl.module
import java.util.concurrent.Executor
import java.util.concurrent.Executors

const val THREAD_COUNT = 3

open class AppExecutors constructor(
    val diskIO: Executor = DiskIOThreadExecutor(),
    val networkIO: Executor = Executors.newFixedThreadPool(THREAD_COUNT),
    val mainThread: Executor = MainThreadExecutor()
) {

    private class MainThreadExecutor : Executor {

        private val mainThreadHandler = Handler(Looper.getMainLooper())

        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }
}

class DiskIOThreadExecutor : Executor {

    private val diskIO = Executors.newSingleThreadExecutor()

    override fun execute(command: Runnable) {
        diskIO.execute(command)
    }

}

val appExecutorsModule = module {
    single { AppExecutors() }
}