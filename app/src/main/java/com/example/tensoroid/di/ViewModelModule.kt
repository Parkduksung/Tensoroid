package com.example.tensoroid.di

import com.example.tensor.Tensor
import com.example.tensoroid.presenter.viewmodel.TensoroidViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { TensoroidViewModel(get()) }
}

val tensorModule = module {
    single { Tensor() }
}