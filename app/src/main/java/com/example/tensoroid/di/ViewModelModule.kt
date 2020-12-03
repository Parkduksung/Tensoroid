package com.example.tensoroid.di

import com.example.tensoroid.presenter.viewmodel.TensoroidViewModel
import org.koin.dsl.module


val viewModelModule = module {
    single { TensoroidViewModel(get()) }
}