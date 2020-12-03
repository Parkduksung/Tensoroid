package com.example.tensoroid.di

import com.example.tensoroid.data.source.local.TensorFlowLocalDataSource
import com.example.tensoroid.data.source.local.TensorFlowLocalDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
abstract class DataSourceModule {

    @Singleton
    @Binds
    abstract fun bindTensorFlowLocalDataSource(tensorFlowLocalDataSourceImpl: TensorFlowLocalDataSourceImpl): TensorFlowLocalDataSource

}