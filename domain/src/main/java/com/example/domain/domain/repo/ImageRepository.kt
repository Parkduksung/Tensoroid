package com.example.domain.domain.repo

import com.example.domain.domain.entity.ImageFile

interface ImageRepository {
    fun getImage(): ImageFile
}
