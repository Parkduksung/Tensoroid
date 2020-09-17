package com.example.tensoroid.data.repo

import android.graphics.Bitmap
import com.example.tensoroid.data.source.local.ImageLocalDataSource
import com.example.tensoroid.domain.repo.ImageRepository

class ImageRepositoryImpl(private val imageLocalDataSource: ImageLocalDataSource) :
    ImageRepository {

    override fun getImage(): Bitmap =
        imageLocalDataSource.getImage()

    companion object {
        fun getInstance(): ImageRepository =
            ImageRepositoryImpl(ImageLocalDataSource.getInstance())

    }
}