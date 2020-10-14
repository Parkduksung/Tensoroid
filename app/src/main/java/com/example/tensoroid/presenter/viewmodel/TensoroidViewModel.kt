package com.example.tensoroid.presenter.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tensoroid.App
import com.example.tensoroid.domain.usecase.GetImage
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

class TensoroidViewModel(private val getImage: GetImage) : ViewModel() {

    private val _bitmapTransform = MutableLiveData<Bitmap>()
    val bitmapTransform
        get() = _bitmapTransform


    private val _bitmapNormal = MutableLiveData<Bitmap>()
    val bitmapNormal: LiveData<Bitmap>
        get() = _bitmapNormal


    init {
        getImage()
    }

    private fun getImage() {

        val assetManager = App.instance.context().assets

        val inputStream = assetManager.open(getImage.invoke().fileName)

        _bitmapNormal.value = BitmapFactory.decodeStream(inputStream)
    }


    fun onClick() {
        transformSegmentation(_bitmapNormal.value)
    }


    private fun transformSegmentation(bitmap: Bitmap?) {
        bitmap?.let { getBitmap ->
            val start = System.currentTimeMillis()

            //요거 쓰나 안쓰나 똑같다.
            val tfliteOptions = Interpreter.Options()
            tfliteOptions.setNumThreads(4)
//
//            val gpuDelegate = GpuDelegate()
//
//            tfliteOptions.addDelegate(gpuDelegate)

            val resizeBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true)
            Log.d("결과1", (System.currentTimeMillis() - start).toString())


            //4를 곱하는 이유는 of coordinate values * 4 bytes per float
            // 즉 float 형으로 하기위해 4를 곱하는 거였음.
            val segmentationMasks =
                ByteBuffer.allocateDirect(IMAGE_SIZE * IMAGE_SIZE * NUM_CLASSES * TO_FLOAT)

            //요거를 해야 마스킹한게 보이네.
            segmentationMasks.order(ByteOrder.nativeOrder())

            Log.d("결과2", (System.currentTimeMillis() - start).toString())
            val interpreter =
                Interpreter(
                    loadModelFile(),
                    tfliteOptions
                )

            Log.d("결과3", (System.currentTimeMillis() - start).toString())
            interpreter.run(
                bitmapToByteBuffer(resizeBitmap, IMAGE_SIZE, IMAGE_SIZE),
                segmentationMasks
            )

            Log.d("결과4", (System.currentTimeMillis() - start).toString())

            val resizeResultBitmap = Bitmap.createScaledBitmap(
                convertBytebufferMaskToBitmap(segmentationMasks),
                getBitmap.width,
                getBitmap.height,
                true
            )

            _bitmapTransform.value = maskImage(getBitmap, resizeResultBitmap)
            Log.d("결과5", (System.currentTimeMillis() - start).toString())
        }
    }

    private fun convertBytebufferMaskToBitmap(
        inputBuffer: ByteBuffer
    ): Bitmap {

//        val start = System.currentTimeMillis()

        val maskBitmap = Bitmap.createBitmap(IMAGE_SIZE, IMAGE_SIZE, Bitmap.Config.ARGB_8888)

        //되감을 필요가 있는지 모르겠음.
//        inputBuffer.rewind()

        //지금 이게 가로세로 257 x 257 에 픽셀 돌릴려는 거 같아보임.
        // 나한태 필요한건 0 : 배경, 15 : 사람 이니까 다른거 다 없앰.

        for (y in 0 until IMAGE_SIZE) {
            for (x in 0 until IMAGE_SIZE) {

                //c = 0 배경 , c = 15

                // 배경
                var backgroundVal = inputBuffer
                    .getFloat((((y * IMAGE_SIZE) + x) * NUM_CLASSES) * TO_FLOAT)

                // 사람
                val personVal = inputBuffer
                    .getFloat((((y * IMAGE_SIZE) + x) * NUM_CLASSES + NUM_PERSON) * TO_FLOAT)

                // 사람이크면 흰색으로 그림.
                if (personVal > backgroundVal) {
                    maskBitmap.setPixel(x, y, Color.WHITE)
                } else {
                    maskBitmap.setPixel(x, y, Color.BLACK)
                }

            }
        }

//        Log.d("결과", (System.currentTimeMillis() - start).toString())

        return maskBitmap
    }

    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = App.instance.context().assets.openFd(imageSegmentationModel)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        val retFile = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        fileDescriptor.close()
        return retFile
    }

    companion object {
        private const val imageSegmentationModel = "deeplabv3_257_mv_gpu.tflite"

        const val NUM_CLASSES = 21
        const val IMAGE_SIZE = 257

        const val NUM_PERSON = 15
        const val TO_FLOAT = 4
    }


}