package com.example.tensoroid.util

import android.graphics.*
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import com.example.tensoroid.App
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

    fun maskImage(original: Bitmap, mask: Bitmap): Bitmap {
//
        val result1 = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
        val toMask = Bitmap.createScaledBitmap(mask, original.width, original.height, false)
        val renderScript: RenderScript = RenderScript.create(App.instance.context())
        val blurInput: Allocation = Allocation.createFromBitmap(renderScript, original)
        val blurOutput: Allocation = Allocation.createFromBitmap(renderScript, toMask)
        val blur: ScriptIntrinsicBlur = ScriptIntrinsicBlur.create(
            renderScript,
            Element.U8_4(renderScript)
        )
        blur.setInput(blurInput)
        blur.setRadius(25.0f)
        blur.forEach(blurOutput)
        blurOutput.copyTo(result1)

        renderScript.destroy()

//        val toMask = Bitmap.createScaledBitmap(mask, original.width, original.height, false)

        val result = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
        val mCanvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
//        //https://developer.android.com/reference/android/graphics/PorterDuff.Mode.html 참고.
        mCanvas.drawBitmap(original, 0f, 0f, null)
        mCanvas.drawBitmap(toMask, 0f, 0f, paint)


        val result2 = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
        val mCanva1 = Canvas(result2)
        val paint1 = Paint(Paint.ANTI_ALIAS_FLAG)
        //https://developer.android.com/reference/android/graphics/PorterDuff.Mode.html 참고.
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_ATOP)
        mCanva1.drawBitmap(result, 0f, 0f, null)
        mCanva1.drawBitmap(result1, 0f, 0f, paint)

        paint.xfermode = null
        return result2
    }


}