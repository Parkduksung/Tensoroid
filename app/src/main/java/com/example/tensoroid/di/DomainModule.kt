package com.example.tensoroid.di

import com.example.tensoroid.domain.repo.TensorFlowRepository
import com.example.tensoroid.domain.usecase.GetTensorFlowImage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object DomainModule {

    @Singleton
    @Provides
    fun provideTensorFlowImage(repository: TensorFlowRepository): GetTensorFlowImage {
        return GetTensorFlowImage(repository)
    }
}