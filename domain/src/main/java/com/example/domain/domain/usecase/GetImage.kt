package com.example.domain.domain.usecase

import com.example.domain.domain.entity.ImageFile
import com.example.domain.domain.repo.ImageRepository

class GetImage(private val imageRepository: ImageRepository) {
    operator fun invoke(): ImageFile =
        imageRepository.getImage()
}
