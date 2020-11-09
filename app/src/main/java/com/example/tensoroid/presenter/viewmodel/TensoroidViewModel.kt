package com.example.tensoroid.presenter.viewmodel

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tensoroid.presenter.TensorFlow
import com.example.tensoroid.util.ImageUtils.maskImage
import java.nio.ByteBuffer

class TensoroidViewModel : ViewModel() {

    private val _bitmapTransform = MutableLiveData<Bitmap>()
    val bitmapTransform: LiveData<Bitmap>
        get() = _bitmapTransform


    private val _bgColorTransform = MutableLiveData<Int>()
    val bgColorTransform: LiveData<Int>
        get() = _bgColorTransform


    private val tensorFlow by lazy { TensorFlow() }

    private var segmentedImage: Bitmap? = null

    private var isImageProcess = false

    private var blurRadius = 0.1f

    var color = 0

    var toggle = true

    fun inputSource(bitmap: Bitmap, blurRadius: Float) {
        this.blurRadius = blurRadius
        if (!isImageProcess) {
            isImageProcess = true
            Thread {
                segmentedImage = Bitmap.createScaledBitmap(
                    convertByteBufferMaskToBitmap(tensorFlow.segmentImage(bitmap)),
                    bitmap.width,
                    bitmap.height,
                    true
                )
                isImageProcess = false
            }.start()

        }
        _bitmapTransform.value = mergeBitmap(bitmap, segmentedImage)
    }

    private fun mergeBitmap(bitmap: Bitmap, segmentedImage: Bitmap?): Bitmap {
        if (segmentedImage == null) return bitmap
        return maskImage(
            original = bitmap,
            mask = segmentedImage,
            blurRadius = blurRadius,
            toggle = toggle
        )
    }

    fun setBgColor(color: Int) {
        _bgColorTransform.value = color
    }


    private fun convertByteBufferMaskToBitmap(
        inputBuffer: ByteBuffer
    ): Bitmap {

        val maskBitmap = Bitmap.createBitmap(IMAGE_SIZE, IMAGE_SIZE, Bitmap.Config.ARGB_8888)


        //지금 이게 가로세로 257 x 257 에 픽셀 돌릴려는 거 같아보임.
        // 나한태 필요한건 0 : 배경, 15 : 사람 이니까 다른거 다 없앰.

        for (y in 0 until IMAGE_SIZE) {
            for (x in 0 until IMAGE_SIZE) {

                //c = 0 배경 , c = 15
                // 배경
                val backgroundVal = inputBuffer
                    .getFloat((((y * IMAGE_SIZE) + x) * NUM_CLASSES) * TO_FLOAT)

                // 사람
                val personVal = inputBuffer
                    .getFloat((((y * IMAGE_SIZE) + x) * NUM_CLASSES + NUM_PERSON) * TO_FLOAT)

                // 사람이크면 흰색으로 그림.
                if (personVal > backgroundVal) {
                    if (toggle) {
                        maskBitmap.setPixel(x, y, Color.WHITE)
                    } else {
                        maskBitmap.setPixel(x, y, Color.TRANSPARENT)
                    }
                } else {
                    maskBitmap.setPixel(x, y, color)
                }
            }
        }
        return maskBitmap
    }


    companion object {
//        private const val Model_IMAGE_SEGMENTATION = "deeplabv3_257_mv_gpu.tflite"

        const val NUM_CLASSES = 21
        const val IMAGE_SIZE = 257

        const val NUM_PERSON = 15
        const val TO_FLOAT = 4
    }


}