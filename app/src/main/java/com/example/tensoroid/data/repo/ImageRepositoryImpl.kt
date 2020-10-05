package com.example.tensoroid.data.repo

import com.example.tensoroid.data.source.local.ImageLocalDataSource
import com.example.domain.domain.entity.ImageFile
import com.example.domain.domain.repo.ImageRepository

class ImageRepositoryImpl(private val imageLocalDataSource: ImageLocalDataSource) :
        ImageRepository {

    override fun getImage(): ImageFile =
            imageLocalDataSource.getImage()

    companion object {
        fun getInstance(): ImageRepository =
                ImageRepositoryImpl(ImageLocalDataSource.getInstance())
    }
}