package com.example.tensoroid.presenter

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.tensoroid.R
import com.example.tensoroid.base.BaseActivity
import com.example.tensoroid.databinding.ActivityMainBinding
import com.example.tensoroid.ext.toBitmap
import com.example.tensoroid.presenter.viewmodel.TensoroidViewModel
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.Executors


class TensoroidActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

    private val movieViewModel by viewModel<TensoroidViewModel>()

    private var blurNum: Float = 10.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.run {
            vm = movieViewModel
        }

        slider.isVisible = true

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
//        slider.addOnChangeListener { slider, value, _ ->
//            if (!movieViewModel.toggle)
//                slider.value = 0.0f
//
//            if (value > 0.0f && value <= 25.0f)
//                blurNum = value
//        }

        movieViewModel.bgColorTransform.observe(this, { color ->
            movieViewModel.toggle = (color == Color.TRANSPARENT)
            slider.isVisible = (color == Color.TRANSPARENT)
            movieViewModel.color = color
        })

//
        fb_capture.setOnClickListener {
            BackgroundChangeBottomSheetDialog().show(supportFragmentManager, "BottomSheetDialog")
        }

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(
            Runnable {
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
                    ImageAnalysis.Analyzer { image ->
                        runOnUiThread {
                            val start = System.currentTimeMillis()
                            movieViewModel.inputSource(image.toBitmap())
                            Log.d("결과", (System.currentTimeMillis() - start).toString())
                            image.close()
                        }
                    })

                try {
                    // Unbind use cases before rebinding
                    cameraProvider.unbindAll()

                    // Bind use cases to camera
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
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }


    fun startBackgroundChangeBottomSheetDialog() {
        BackgroundChangeBottomSheetDialog().show(supportFragmentManager, "BottomSheetDialog")
    }


    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
