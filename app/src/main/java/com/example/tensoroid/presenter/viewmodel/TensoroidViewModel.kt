package com.example.tensoroid.presenter.viewmodel

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tensoroid.App
import com.example.tensoroid.util.ImageUtils.bitmapToByteBuffer
import com.example.tensoroid.util.ImageUtils.maskImage
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TensoroidViewModel : ViewModel() {

    private val _bitmapTransform = MutableLiveData<Bitmap>()
    val bitmapTransform
        get() = _bitmapTransform


    private val interpreter by lazy {
        Interpreter(
            loadModelFile(),
            Interpreter.Options().apply {
                setNumThreads(4)
                addDelegate(GpuDelegate())
                setAllowBufferHandleOutput(false)
            }
        )
    }

    private var segmentedImage: Bitmap? = null

    private var isImageProcess = false

    private var blurRadius = 0.1f

    fun inputSource(bitmap: Bitmap, blurRadius: Float) {
        this.blurRadius = blurRadius
        if (!isImageProcess) {
            isImageProcess = true
            Thread {
                segmentedImage = segmentImage(bitmap)
                isImageProcess = false
            }.start()

        }
        _bitmapTransform.value = mergeBitmap(bitmap, segmentedImage)
    }

    private fun mergeBitmap(bitmap: Bitmap, segmentedImage: Bitmap?): Bitmap {
        if (segmentedImage == null) return bitmap
        return maskImage(original = bitmap, mask = segmentedImage, blurRadius = blurRadius)
    }

    private fun segmentImage(bitmap: Bitmap): Bitmap {

        val resizeBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true)

        val segmentationMasks =
            ByteBuffer.allocateDirect(IMAGE_SIZE * IMAGE_SIZE * NUM_CLASSES * TO_FLOAT)

        segmentationMasks.order(ByteOrder.nativeOrder())

        interpreter.run(
            bitmapToByteBuffer(resizeBitmap, IMAGE_SIZE, IMAGE_SIZE),
            segmentationMasks
        )

        return Bitmap.createScaledBitmap(
            convertBytebufferMaskToBitmap(segmentationMasks),
            bitmap.width,
            bitmap.height,
            true
        )
    }


    private fun convertBytebufferMaskToBitmap(
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
                    maskBitmap.setPixel(x, y, Color.WHITE)
                } else {
                    maskBitmap.setPixel(x, y, Color.TRANSPARENT)
                }
            }
        }
        return maskBitmap
    }

    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = App.instance.context().assets.openFd(Model_IMAGE_SEGMENTATION)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        val retFile = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        fileDescriptor.close()
        return retFile
    }

    companion object {
        private const val Model_IMAGE_SEGMENTATION = "deeplabv3_257_mv_gpu.tflite"

        const val NUM_CLASSES = 21
        const val IMAGE_SIZE = 257

        const val NUM_PERSON = 15
        const val TO_FLOAT = 4
    }


}