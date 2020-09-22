package com.example.tensoroid.domain.repo

import com.example.tensoroid.domain.entity.ImageFile

interface ImageRepository {
    fun getImage(): ImageFile
}
