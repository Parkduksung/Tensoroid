package com.example.tensoroid.presenter.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tensoroid.domain.usecase.GetImage

class TensoroidViewModel(private val getImage: GetImage) : ViewModel() {

    private val _bitmapTransform = MutableLiveData<Bitmap>()
    val bitmapTransform
        get() = _bitmapTransform


    val bitmapNormal = getImage.invoke()


    fun onClick() {
        _bitmapTransform.value = getImage.invoke()
    }

}