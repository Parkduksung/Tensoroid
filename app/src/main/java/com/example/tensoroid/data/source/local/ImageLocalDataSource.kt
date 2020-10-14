package com.example.tensoroid.data.source.local

import com.example.tensoroid.domain.entity.ImageFile


class ImageLocalDataSource {

    fun getImage(): ImageFile =
            ImageFile("not_resize2.jpeg")


    companion object {
        fun getInstance() =
                ImageLocalDataSource()
    }
}