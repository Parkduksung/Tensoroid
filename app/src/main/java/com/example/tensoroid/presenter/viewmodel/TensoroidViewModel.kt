package com.example.tensoroid.presenter.viewmodel

import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tensor.Tensor
import com.example.tensor.Tensor.Companion.IMAGE_SIZE
import com.example.tensor.Tensor.Companion.NUM_CLASSES
import com.example.tensor.Tensor.Companion.TO_FLOAT
import com.example.tensoroid.App
import com.example.tensoroid.util.ImageUtils.maskImage
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TensoroidViewModel(private val tensor: Tensor) : ViewModel() {

    private var segmentedImage: Bitmap? = null

    private var isImageProcess = false

    val setBgColor: (color: Int) -> Unit = this::changeBgColor

    private val _bitmapTransform = MutableLiveData<Bitmap>()
    val bitmapTransform: LiveData<Bitmap>
        get() = _bitmapTransform

    private val _bgColorTransform = MutableLiveData(Color.TRANSPARENT)
    val bgColorTransform: LiveData<Int>
        get() = _bgColorTransform

    val blurRadius = MutableLiveData(DEFAULT_BLUR_RADIUS)

    init {
        tensor.setInterpreter(loadModelFile())
    }

    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor =
            App.instance.context().assets.openFd(Model_IMAGE_SEGMENTATION)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        val retFile = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        fileDescriptor.close()
        return retFile
    }

    fun inputSource(bitmap: Bitmap) {
        if (!isImageProcess) {
            isImageProcess = true
            Thread {
                segmentedImage = Bitmap.createScaledBitmap(
                    convertByteBufferMaskToBitmap(tensor.segmentImage(bitmap)),
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

        private const val Model_IMAGE_SEGMENTATION = "deeplabv3_257_mv_gpu.tflite"

        private const val DEFAULT_BLUR_RADIUS = 5f

        const val NUM_PERSON = 15
    }
}
