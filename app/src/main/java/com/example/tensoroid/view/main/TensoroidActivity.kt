package com.example.tensoroid.view.main

import android.os.Bundle
import android.os.PersistableBundle
import com.example.tensoroid.R
import com.example.tensoroid.base.BaseActivity
import com.example.tensoroid.databinding.ActivityMainBinding
import com.example.tensoroid.viewmodel.TensoroidViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class TensoroidActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

    private val movieViewModel by viewModel<TensoroidViewModel>()

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        binding.run {
            vm = movieViewModel
        }
    }
}