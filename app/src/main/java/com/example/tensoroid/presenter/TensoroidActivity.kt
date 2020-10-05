package com.example.tensoroid.presenter

import android.os.Bundle
import com.example.tensoroid.R
import com.example.tensoroid.base.BaseActivity
import com.example.tensoroid.databinding.ActivityMainBinding
import com.example.tensoroid.presenter.viewmodel.TensoroidViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class TensoroidActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

    private val movieViewModel by viewModel<TensoroidViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.run {
            vm = movieViewModel
        }

    }
}
