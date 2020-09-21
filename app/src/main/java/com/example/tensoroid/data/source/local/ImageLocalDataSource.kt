package com.example.tensoroid.data.source.local

import com.example.domain.domain.entity.ImageFile


class ImageLocalDataSource {

    fun getImage(): ImageFile =
            ImageFile("image.png")

//
//
//        val assetManager = App.instance.context().assets
//
//        val inputStream = assetManager.open("image.png")
//
//        return BitmapFactory.decodeStream(inputStream)
//    }


    companion object {
        fun getInstance() =
                ImageLocalDataSource()
    }
}