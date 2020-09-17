package com.example.tensoroid.domain.usecase

import android.graphics.Bitmap
import com.example.tensoroid.domain.repo.ImageRepository

class GetImage(private val imageRepository: ImageRepository) {

    operator fun invoke(): Bitmap =
        imageRepository.getImage()

}
