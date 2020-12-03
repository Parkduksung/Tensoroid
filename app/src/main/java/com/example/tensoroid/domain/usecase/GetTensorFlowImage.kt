package com.example.tensoroid.domain.usecase

import android.graphics.Bitmap
import com.example.tensoroid.domain.entity.TensorFlowImage
import com.example.tensoroid.domain.repo.TensorFlowRepository
import javax.inject.Inject

class GetTensorFlowImage @Inject constructor(private val tensorFlowRepository: TensorFlowRepository) {
    operator fun invoke(bitmap: Bitmap): TensorFlowImage =
        tensorFlowRepository.getTensorFlowImage(bitmap = bitmap).toTensorFlowImage()
}