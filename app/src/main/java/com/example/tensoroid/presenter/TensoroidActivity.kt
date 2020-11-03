package com.example.tensoroid.presenter

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLSurfaceView.RENDERMODE_CONTINUOUSLY
import android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.tensoroid.App
import com.example.tensoroid.R
import com.example.tensoroid.base.BaseActivity
import com.example.tensoroid.databinding.ActivityMainBinding
import com.example.tensoroid.presenter.viewmodel.TensoroidViewModel
import com.example.tensoroid.util.Tex
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.Executors
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class TensoroidActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main),
    GLSurfaceView.Renderer {

    private val movieViewModel by viewModel<TensoroidViewModel>()

    private lateinit var mTex: Tex

    private lateinit var bitmap: Bitmap

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        if (::bitmap.isInitialized) {
                mTex = Tex(bitmap)
                mTex.draw()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.run {
            vm = movieViewModel
        }

        binding.glsurface.setEGLContextClientVersion(2)
        binding.glsurface.preserveEGLContextOnPause = true
        binding.glsurface.setRenderer(this)
        binding.glsurface.renderMode = RENDERMODE_CONTINUOUSLY

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        movieViewModel.bitmapTransform.observe(this, Observer {
            bitmap = it
        })
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
                            movieViewModel.inputSource(image.toBitmap())
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


    private fun ImageProxy.toBitmap(): Bitmap {
        val start = System.currentTimeMillis()
        val yBuffer = this.planes[0].buffer // Y
        val uBuffer = this.planes[1].buffer // U
        val vBuffer = this.planes[2].buffer // V

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        //U and V are swapped
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 70, out)
        val imageBytes = out.toByteArray()

        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        val matrix = Matrix()
        matrix.setScale(-1f, 1f)
        matrix.postRotate(90f)

        Log.d("결과", (System.currentTimeMillis() - start).toString())

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }


    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
