package com.example.tensoroid.di

import com.example.tensorflowlite.TensorFlowLite
import com.example.tensoroid.App
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object TensorModule {

    @Singleton
    @Provides
    fun provideTensorFlowLite(): TensorFlowLite =
        TensorFlowLite(App.instance.context())
}