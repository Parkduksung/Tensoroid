package com.example.tensoroid.data.repo

import android.graphics.Bitmap
import com.example.tensoroid.data.model.TensorFlowResponse
import com.example.tensoroid.data.source.local.TensorFlowLocalDataSource
import com.example.tensoroid.domain.repo.TensorFlowRepository
import javax.inject.Inject

class TensorFlowRepositoryImpl @Inject constructor(private val tensorFlowLocalDataSource: TensorFlowLocalDataSource) :
    TensorFlowRepository {

    override fun getTensorFlowImage(bitmap: Bitmap): TensorFlowResponse =
        tensorFlowLocalDataSource.getTensorFlowImage(bitmap)

}