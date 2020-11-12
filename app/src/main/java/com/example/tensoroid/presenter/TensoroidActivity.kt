package com.example.tensoroid.presenter

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.tensor.Tensor
import com.example.tensoroid.R
import com.example.tensoroid.base.BaseActivity
import com.example.tensoroid.databinding.ActivityMainBinding
import com.example.tensoroid.ext.showToast
import com.example.tensoroid.ext.toBitmap
import com.example.tensoroid.presenter.viewmodel.TensoroidViewModel
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.Executors


class TensoroidActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

    private val tensoroidViewModel by viewModel<TensoroidViewModel>()

    private lateinit var backgroundChangeBottomSheetDialog: BackgroundChangeBottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.run {
            vm = tensoroidViewModel
        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        tensoroidViewModel.bgColorTransform.observe(this, { color ->
            binding.slider.isVisible = (color == Color.TRANSPARENT)
            if (::backgroundChangeBottomSheetDialog.isInitialized)
                backgroundChangeBottomSheetDialog.dismiss()
        })

        fb_capture.setOnClickListener {
            startBackgroundChangeBottomSheetDialog()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(
            {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                val preview = Preview.Builder()
                    .build().also {
                        it.setSurfaceProvider(binding.viewFinder.createSurfaceProvider())
                    }
                val imageAnalysis = ImageAnalysis.Builder()
                    .setImageQueueDepth(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
                    .build()

                imageAnalysis.setAnalyzer(
                    Executors.newSingleThreadExecutor(),
                    { image ->
                        runOnUiThread {
                            val start = System.currentTimeMillis()
                            tensoroidViewModel.inputSource(image.toBitmap())
                            Log.d("결과", (System.currentTimeMillis() - start).toString())
                            image.close()
                        }
                    })

                try {
                    cameraProvider.unbindAll()

                    cameraProvider.bindToLifecycle(
                        this, cameraSelector, imageAnalysis, preview
                    )

                } catch (exc: Exception) {
                }

            }, ContextCompat.getMainExecutor(this)
        )
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                showToast(getString(R.string.permission_fail))
                finish()
            }
        }
    }


    private fun startBackgroundChangeBottomSheetDialog() {
        backgroundChangeBottomSheetDialog = BackgroundChangeBottomSheetDialog().apply {
            show(
                supportFragmentManager,
                "BackgroundChangeBottomSheetDialog"
            )
        }
    }


    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
