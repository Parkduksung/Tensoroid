package com.example.tensoroid.di

import com.example.tensoroid.data.repo.ImageRepositoryImpl
import com.example.domain.domain.usecase.GetImage
import com.example.tensoroid.presenter.viewmodel.TensoroidViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { TensoroidViewModel(GetImage(ImageRepositoryImpl.getInstance())) }
}