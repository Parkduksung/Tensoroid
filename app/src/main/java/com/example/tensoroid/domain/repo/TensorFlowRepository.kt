package com.example.tensoroid.domain.repo

import android.graphics.Bitmap
import com.example.tensoroid.data.model.TensorFlowResponse

interface TensorFlowRepository {
    fun getTensorFlowImage(bitmap: Bitmap): TensorFlowResponse
}
