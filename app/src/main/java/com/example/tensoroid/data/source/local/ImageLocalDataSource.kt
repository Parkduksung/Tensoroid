package com.example.tensoroid.data.source.local

import com.example.tensoroid.domain.entity.ImageFile


class ImageLocalDataSource {

    fun getImage(): ImageFile =
            ImageFile("image.png")


    companion object {
        fun getInstance() =
                ImageLocalDataSource()
    }
}