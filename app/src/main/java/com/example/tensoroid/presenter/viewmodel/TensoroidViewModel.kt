package com.example.tensoroid.presenter.viewmodel

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tensorflowlite.TensorFlowLite.Companion.IMAGE_SIZE
import com.example.tensorflowlite.TensorFlowLite.Companion.NUM_CLASSES
import com.example.tensorflowlite.TensorFlowLite.Companion.NUM_PERSON
import com.example.tensorflowlite.TensorFlowLite.Companion.TO_FLOAT
import com.example.tensoroid.domain.usecase.GetTensorFlowImage
import com.example.tensoroid.util.ImageUtils.maskImage
import java.nio.ByteBuffer

class TensoroidViewModel(private val getTensorFlowImage: GetTensorFlowImage) : ViewModel() {

    private var segmentedImage: Bitmap? = null

    private var isImageProcess = false

    val blurRadius = MutableLiveData(DEFAULT_BLUR_RADIUS)

    val setBgColor: (color: Int) -> Unit = this::changeBgColor

    private val _bitmapTransform = MutableLiveData<Bitmap>()
    val bitmapTransform: LiveData<Bitmap>
        get() = _bitmapTransform

    private val _bgColorTransform = MutableLiveData(Color.TRANSPARENT)
    val bgColorTransform: LiveData<Int>
        get() = _bgColorTransform

    private val _isVisibleSlider = MutableLiveData(bgColorTransform.value == Color.TRANSPARENT)
    val isVisibleSlider: LiveData<Boolean>
        get() = _isVisibleSlider

    fun inputSource(bitmap: Bitmap) {
        if (!isImageProcess) {
            isImageProcess = true
            Thread {
                segmentedImage = Bitmap.createScaledBitmap(
                    convertByteBufferMaskToBitmap(
                        getTensorFlowImage.invoke(bitmap = bitmap).byteBuffer
                    ),
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
            blurRadius = blurRadius.value ?: 0f,
            toggle = bgColorTransform.value == Color.TRANSPARENT
        )
    }

    private fun changeBgColor(color: Int) {
        _bgColorTransform.value = color
        _isVisibleSlider.value = (color == Color.TRANSPARENT)
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
                    if (bgColorTransform.value == Color.TRANSPARENT) {
                        maskBitmap.setPixel(x, y, Color.WHITE)
                    } else {
                        maskBitmap.setPixel(x, y, Color.TRANSPARENT)
                    }
                } else {
                    maskBitmap.setPixel(x, y, bgColorTransform.value ?: Color.TRANSPARENT)
                }
            }
        }
        return maskBitmap
    }

    companion object {
        private const val DEFAULT_BLUR_RADIUS = 5f
    }
}
