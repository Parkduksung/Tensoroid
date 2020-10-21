package com.example.tensoroid.presenter

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLSurfaceView.RENDERMODE_CONTINUOUSLY
import android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
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
import java.io.IOException
import java.util.concurrent.Executors
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class TensoroidActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main),
    GLSurfaceView.Renderer {

    private val movieViewModel by viewModel<TensoroidViewModel>()

    private lateinit var mTex: Tex

    private var toggle: Boolean = false

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
//        binding.viewFinder.bitmap?.let {
//            mTex = Tex(it)
//        }

        init()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

    }

    override fun onDrawFrame(gl: GL10?) {
//        GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT)
//        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

//        Log.d("결과", "결과찍힘")
        if (toggle) {
            GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT)
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
            Log.d("결과", "결과찍힘1")
            mTex.draw()
            toggle = false
        }

//        tex.draw()
    }

    private fun init() {
        try {

//            mTex = Tex(binding.viewFinder.bitmap!!)
            val inputStream = App.instance.assets.open("image.png")
            val bitmap = BitmapFactory.decodeStream(inputStream)

            mTex = Tex(bitmap)
//            binding.viewFinder.bitmap?.let { mTex = Tex(it) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.run {
            vm = movieViewModel
        }

//        mTex = Tex(binding.viewFinder.bitmap!!)

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

//        movieViewModel.bitmapTransform.observe(this, Observer { bitmap->
//            mTex = Tex(bitmap)
//        })
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

                        val toByteBuffer = image.planes[0].buffer

                        movieViewModel.inputSource(toByteBuffer)

//                        binding.glsurface.queueEvent {
//
//
//                            binding.viewFinder.bitmap?.let{
//                                toggle= true
//                                mTex = Tex(it)
//                                image.close()
//                            }
//
////                            runOnUiThread {
////
////
//////                                image.close()
////
////                            }
//                        }
//                        runOnUiThread {
//
//
//                            val start = System.currentTimeMillis()
//                            binding.viewFinder.bitmap?.let { bitmap ->
//                                movieViewModel.inputSource(bitmap)
//
//                            }
//
//                            Log.d("결과", (System.currentTimeMillis() - start).toString())
//                            image.close()
//                        }
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


    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
