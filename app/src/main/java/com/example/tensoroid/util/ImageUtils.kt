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

    fun maskImage(original: Bitmap, mask: Bitmap, blurRadius: Float): Bitmap {


        val result1 = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)

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


        val result = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
        val mCanvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        //https://developer.android.com/reference/android/graphics/PorterDuff.Mode.html 참고.
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
        mCanvas.drawBitmap(original, 0f, 0f, null)
        mCanvas.drawBitmap(mask, 0f, 0f, paint)


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

    fun Image.toBitmap(): Bitmap {

        val yBuffer = planes[0].buffer // Y
        val uBuffer = planes[1].buffer // U
        val vBuffer = planes[2].buffer // V

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        //U and V are swapped
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
        val imageBytes = out.toByteArray()

        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        val matrix = Matrix()
        matrix.setScale(-1f, 1f)
        matrix.postRotate(90f)

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}