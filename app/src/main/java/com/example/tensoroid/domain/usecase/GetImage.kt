package com.example.tensoroid.domain.usecase

import com.example.tensoroid.domain.entity.ImageFile
import com.example.tensoroid.domain.repo.ImageRepository

class GetImage(private val imageRepository: ImageRepository) {
    operator fun invoke(): ImageFile =
            imageRepository.getImage()
}