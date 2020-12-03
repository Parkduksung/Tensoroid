package com.example.tensoroid.di

import com.example.tensoroid.data.repo.TensorFlowRepositoryImpl
import com.example.tensoroid.domain.repo.TensorFlowRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindTensorFlowRepository(tensorFlowRepositoryImpl: TensorFlowRepositoryImpl): TensorFlowRepository

}