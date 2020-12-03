package com.example.tensoroid.data.source.local

import android.graphics.Bitmap
import com.example.tensorflowlite.TensorFlowLite
import com.example.tensoroid.data.model.TensorFlowResponse
import javax.inject.Inject


class TensorFlowLocalDataSourceImpl @Inject constructor(private val tensorFlowLite: TensorFlowLite) :
    TensorFlowLocalDataSource {

    override fun getTensorFlowImage(bitmap: Bitmap) =
        TensorFlowResponse(tensorFlowLite.segmentImage(bitmap))

}


interface TensorFlowLocalDataSource {

    fun getTensorFlowImage(bitmap: Bitmap): TensorFlowResponse

}