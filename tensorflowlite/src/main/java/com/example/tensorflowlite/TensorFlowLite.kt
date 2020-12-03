package com.example.tensorflowlite

import android.content.Context
import android.graphics.Bitmap
import com.example.tensorflowlite.Utils.bitmapToByteBuffer
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TensorFlowLite(private val context: Context) {


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

    private val segmentationMasks by lazy {
        ByteBuffer.allocateDirect(IMAGE_SIZE * IMAGE_SIZE * NUM_CLASSES * TO_FLOAT).apply {
            order(ByteOrder.nativeOrder())
        }
    }


    fun segmentImage(bitmap: Bitmap): ByteBuffer {

        segmentationMasks.rewind()

        val resizeBitmap = Bitmap.createScaledBitmap(
            bitmap,
            IMAGE_SIZE,
            IMAGE_SIZE, true
        )

        interpreter.run(
            bitmapToByteBuffer(
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
            context.assets.openFd(Model_IMAGE_SEGMENTATION)
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
        const val NUM_PERSON = 15
    }

}