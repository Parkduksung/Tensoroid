package com.example.tensoroid.presenter

import android.graphics.Bitmap
import com.example.tensoroid.App
import com.example.tensoroid.util.ImageUtils
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TensorFlow {

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

    fun segmentImage(bitmap: Bitmap): ByteBuffer {

        val resizeBitmap = Bitmap.createScaledBitmap(
            bitmap,
            IMAGE_SIZE,
            IMAGE_SIZE, true
        )

        val segmentationMasks =
            ByteBuffer.allocateDirect(IMAGE_SIZE * IMAGE_SIZE * NUM_CLASSES * TO_FLOAT)

        segmentationMasks.order(ByteOrder.nativeOrder())

        interpreter.run(
            ImageUtils.bitmapToByteBuffer(
                resizeBitmap,
                IMAGE_SIZE,
                IMAGE_SIZE
            ),
            segmentationMasks
        )

        return segmentationMasks
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


    companion object {
        private const val Model_IMAGE_SEGMENTATION = "deeplabv3_257_mv_gpu.tflite"

        const val NUM_CLASSES = 21
        const val IMAGE_SIZE = 257
        const val TO_FLOAT = 4
    }
}