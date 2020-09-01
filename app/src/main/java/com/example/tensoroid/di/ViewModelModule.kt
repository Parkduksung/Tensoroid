package com.example.tensoroid.di

import com.example.tensoroid.viewmodel.TensoroidViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { TensoroidViewModel() }
}