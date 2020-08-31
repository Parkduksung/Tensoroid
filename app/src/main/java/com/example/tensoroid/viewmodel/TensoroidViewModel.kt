package com.example.tensoroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tensoroid.data.model.ResultItem
import com.example.tensoroid.util.ImageSegmentationModelExecutor
import com.example.tensoroid.util.ImageUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

class TensoroidViewModel : ViewModel() {

    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(viewModelJob)

    private val _resultItem = MutableLiveData<ResultItem>()

    val resultItem: LiveData<ResultItem>
        get() = _resultItem

    fun onApplyModel(
        filePath: String,
        imageSegmentationModel: ImageSegmentationModelExecutor,
        inferenceThread: ExecutorCoroutineDispatcher
    ) {
        viewModelScope.launch(inferenceThread) {
            val contentImage =
                ImageUtils.decodeBitmap(
                    File(filePath)
                )

            val result = imageSegmentationModel.execute(contentImage)

            _resultItem.postValue(result)
        }
    }

}