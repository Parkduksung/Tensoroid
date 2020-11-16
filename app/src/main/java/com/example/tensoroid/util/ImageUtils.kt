package com.example.tensoroid.util

import android.graphics.*
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import com.example.tensoroid.App

object ImageUtils {

    private val renderScript: RenderScript = RenderScript.create(App.instance.context())

    private val blur: ScriptIntrinsicBlur = ScriptIntrinsicBlur.create(
        renderScript,
        Element.U8_4(renderScript)
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private lateinit var mCanvas: Canvas

    fun maskImage(original: Bitmap, mask: Bitmap, blurRadius: Float, toggle: Boolean): Bitmap {

        return if (toggle && blurRadius != 0f) {
            val onlyBlurBitmap =
                Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)

            val blurInput: Allocation = Allocation.createFromBitmap(renderScript, original)
            val blurOutput: Allocation = Allocation.createFromBitmap(renderScript, mask)

            blur.setInput(blurInput)
            blur.setRadius(blurRadius)
            blur.forEach(blurOutput)
            blurOutput.copyTo(onlyBlurBitmap)

            renderScript.destroy()

            blurFast(onlyBlurBitmap, blurRadius.toInt())

            val onlyMaskBitmap =
                Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
            mCanvas = Canvas(onlyMaskBitmap)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
            mCanvas.drawBitmap(original, 0f, 0f, null)
            mCanvas.drawBitmap(mask, 0f, 0f, paint)

            val maskBlurBitmap =
                Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
            mCanvas = Canvas(maskBlurBitmap)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_ATOP)
            mCanvas.drawBitmap(onlyMaskBitmap, 0f, 0f, null)
            mCanvas.drawBitmap(onlyBlurBitmap, 0f, 0f, paint)

            paint.xfermode = null
            maskBlurBitmap
        } else {
            val onlyMaskBitmap =
                Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
            mCanvas = Canvas(onlyMaskBitmap)
            mCanvas.drawBitmap(original, 0f, 0f, null)
            mCanvas.drawBitmap(mask, 0f, 0f, paint)
            onlyMaskBitmap
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
            r /= 4
        }
        bmp.setPixels(pix, 0, w, 0, 0, w, h)
    }
}