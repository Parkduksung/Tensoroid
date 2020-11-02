package com.example.tensoroid.util

import java.nio.ByteBuffer

interface OnSurfaceDrawable {
    /**
     * Surface initialized
     */
    fun initialized()

    /**
     * onDrawable
     */
    fun onDrawable(
        imageBuffer: ByteBuffer?,
        width: Int,
        height: Int,
        pixelStride: Int,
        rowStride: Int,
        rowPadding: Int,
        presentationTime: Long
    )
}