package com.example.tensoroid.domain.repo

import android.graphics.Bitmap

interface ImageRepository {
    fun getImage(): Bitmap
}
