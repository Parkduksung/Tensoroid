package com.example.tensoroid.ext

import android.graphics.Bitmap
import android.graphics.Matrix

fun Bitmap.rotate(degrees: Float): Bitmap =
        Bitmap.createBitmap(
                this, 0, 0, width, height, Matrix().apply { postRotate(degrees) }, true
        )