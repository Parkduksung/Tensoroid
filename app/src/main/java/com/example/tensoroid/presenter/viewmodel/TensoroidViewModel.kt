package com.example.tensoroid.presenter.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tensoroid.domain.usecase.GetImage

class TensoroidViewModel(private val getImage: GetImage) : ViewModel() {


    private val _imageBitmap = MutableLiveData<Bitmap>()
    val imageBitmap
        get() = _imageBitmap


    fun onClick() {
        _imageBitmap.value = getImage.invoke()
    }

}