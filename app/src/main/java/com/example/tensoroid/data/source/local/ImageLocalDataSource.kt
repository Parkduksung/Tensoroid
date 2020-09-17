package com.example.tensoroid.data.source.local

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.tensoroid.App


class ImageLocalDataSource {


    fun getImage(): Bitmap {

        val assetManager = App.instance.context().assets

        val inputStream = assetManager.open("image.jpg")

        return BitmapFactory.decodeStream(inputStream)
    }


    companion object {
        fun getInstance() =
            ImageLocalDataSource()
    }
}