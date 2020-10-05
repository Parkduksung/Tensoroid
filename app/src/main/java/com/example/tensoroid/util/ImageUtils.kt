package com.example.tensoroid.util

import android.R
import android.graphics.*
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder


object ImageUtils {

    fun decodeBitmap(file: File): Bitmap {
        // First, decode EXIF data and retrieve transformation matrix
        val exif = ExifInterface(file.absolutePath)
        val transformation =
                decodeExifOrientation(
                        exif.getAttributeInt(
                                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90
                        )
                )

        // Read bitmap using factory methods, and transform it using EXIF data
        val options = BitmapFactory.Options()
        val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
        return Bitmap.createBitmap(
                BitmapFactory.decodeFile(file.absolutePath),
                0, 0, bitmap.width, bitmap.height, transformation, true
        )
    }

    private fun decodeExifOrientation(orientation: Int): Matrix {
        val matrix = Matrix()

        // Apply transformation corresponding to declared EXIF orientation
        when (orientation) {
            ExifInterface.ORIENTATION_NORMAL, ExifInterface.ORIENTATION_UNDEFINED -> Unit
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90F)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180F)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270F)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1F, 1F)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1F, -1F)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postScale(-1F, 1F)
                matrix.postRotate(270F)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postScale(-1F, 1F)
                matrix.postRotate(90F)
            }

            // Error out if the EXIF orientation is invalid
            else -> throw IllegalArgumentException("Invalid orientation: $orientation")
        }

        // Return the resulting matrix
        return matrix
    }

//    fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer =
//        ByteBuffer.allocate(bitmap.byteCount).also { bitmap.copyPixelsToBuffer(it) }

    fun bitmapToByteBuffer(
            bitmapIn: Bitmap,
            width: Int,
            height: Int,
            mean: Float = 0.0f,
            std: Float = 255.0f
    ): ByteBuffer {
//        val bitmap = getResizedBitmap(bitmapIn, width)
        val inputImage = ByteBuffer.allocateDirect(1 * width * height * 3 * 4)
        inputImage.order(ByteOrder.nativeOrder())
        inputImage.rewind()

        val intValues = IntArray(width * height)
        bitmapIn.getPixels(intValues, 0, width, 0, 0, width, height)
        var pixel = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val value = intValues[pixel++]

                // Normalize channel values to [-1.0, 1.0]. This requirement varies by
                // model. For example, some models might require values to be normalized
                // to the range [0.0, 1.0] instead.
                inputImage.putFloat(((value shr 16 and 0xFF) - mean) / std)
                inputImage.putFloat(((value shr 8 and 0xFF) - mean) / std)
                inputImage.putFloat(((value and 0xFF) - mean) / std)
            }
        }

        inputImage.rewind()
        return inputImage
    }

    fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        val resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false
        )
//        bm.recycle()
        return resizedBitmap
    }

    fun scaleBitmapAndKeepRatio(
            targetBmp: Bitmap,
            reqHeightInPixels: Int,
            reqWidthInPixels: Int
    ): Bitmap {
        if (targetBmp.width == reqHeightInPixels && targetBmp.width == reqWidthInPixels) {
            return targetBmp
        }
        val matrix = Matrix()
        matrix.setRectToRect(
                RectF(
                        0f, 0f,
                        targetBmp.width.toFloat(),
                        targetBmp.width.toFloat()
                ),
                RectF(
                        0f, 0f,
                        reqWidthInPixels.toFloat(),
                        reqHeightInPixels.toFloat()
                ),
                Matrix.ScaleToFit.FILL
        )
        return Bitmap.createBitmap(
                targetBmp, 0, 0,
                targetBmp.width,
                targetBmp.width, matrix, true
        )
    }

    fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    fun maskImage(original: Bitmap, mask: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
        val mCanvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        //https://developer.android.com/reference/android/graphics/PorterDuff.Mode.html 참고.
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DARKEN)
        mCanvas.drawBitmap(original, 0f, 0f, null)
        mCanvas.drawBitmap(mask, 0f, 0f, paint)
//        paint.xfermode = null
        return result
    }
}