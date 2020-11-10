package com.example.tensoroid.util

import android.graphics.*
import android.media.Image
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import com.example.tensoroid.App
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder


object ImageUtils {


    fun bitmapToByteBuffer(
        bitmapIn: Bitmap,
        width: Int,
        height: Int,
        mean: Float = 0.0f,
        std: Float = 255.0f
    ): ByteBuffer {
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


    private fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
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

    fun maskImage(original: Bitmap, mask: Bitmap, blurRadius: Float, toggle: Boolean): Bitmap {

        return if (toggle && blurRadius != 0f) {
            val result1 =
                Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)

            val renderScript: RenderScript = RenderScript.create(App.instance.context())
            val blurInput: Allocation = Allocation.createFromBitmap(renderScript, original)
            val blurOutput: Allocation = Allocation.createFromBitmap(renderScript, mask)
            val blur: ScriptIntrinsicBlur = ScriptIntrinsicBlur.create(
                renderScript,
                Element.U8_4(renderScript)
            )
            blur.setInput(blurInput)
            blur.setRadius(blurRadius)
            blur.forEach(blurOutput)
            blurOutput.copyTo(result1)

            renderScript.destroy()

            blurFast(result1, blurRadius.toInt())


            val result =
                Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
            val mCanvas = Canvas(result)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
            mCanvas.drawBitmap(original, 0f, 0f, null)
            mCanvas.drawBitmap(mask, 0f, 0f, paint)


            val result2 =
                Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
            val mCanva1 = Canvas(result2)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_ATOP)
            mCanva1.drawBitmap(result, 0f, 0f, null)
            mCanva1.drawBitmap(result1, 0f, 0f, paint)

            paint.xfermode = null
            result2
        } else {
            val result =
                Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
            val mCanvas = Canvas(result)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            mCanvas.drawBitmap(original, 0f, 0f, null)
            mCanvas.drawBitmap(mask, 0f, 0f, paint)
            result
        }
    }

    private fun blurFast(bmp: Bitmap, radius: Int) {
        val w = bmp.width
        val h = bmp.height
        val pix = IntArray(w * h)
        bmp.getPixels(pix, 0, w, 0, 0, w, h)
        var r = radius
        while (r >= 1) {
            for (i in r until h - r) {
                for (j in r until w - r) {
                    val tl = pix[(i - r) * w + j - r]
                    val tr = pix[(i - r) * w + j + r]
                    val tc = pix[(i - r) * w + j]
                    val bl = pix[(i + r) * w + j - r]
                    val br = pix[(i + r) * w + j + r]
                    val bc = pix[(i + r) * w + j]
                    val cl = pix[i * w + j - r]
                    val cr = pix[i * w + j + r]
                    pix[i * w + j] = -0x1000000 or (
                            (tl and 0xFF) + (tr and 0xFF) + (tc and 0xFF) + (bl and 0xFF) + (br and 0xFF) + (bc and 0xFF) + (cl and 0xFF) + (cr and 0xFF) shr 3 and 0xFF) or (
                            (tl and 0xFF00) + (tr and 0xFF00) + (tc and 0xFF00) + (bl and 0xFF00) + (br and 0xFF00) + (bc and 0xFF00) + (cl and 0xFF00) + (cr and 0xFF00) shr 3 and 0xFF00) or (
                            (tl and 0xFF0000) + (tr and 0xFF0000) + (tc and 0xFF0000) + (bl and 0xFF0000) + (br and 0xFF0000) + (bc and 0xFF0000) + (cl and 0xFF0000) + (cr and 0xFF0000) shr 3 and 0xFF0000)
                }
            }
            r /= 2
        }
        bmp.setPixels(pix, 0, w, 0, 0, w, h)
    }
}