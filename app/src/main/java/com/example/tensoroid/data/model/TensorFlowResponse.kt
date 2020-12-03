package com.example.tensoroid.data.model

import com.example.tensoroid.domain.entity.TensorFlowImage
import java.nio.ByteBuffer

data class TensorFlowResponse(
    val byteBuffer: ByteBuffer
) {
    fun toTensorFlowImage(): TensorFlowImage =
        TensorFlowImage(byteBuffer)
}