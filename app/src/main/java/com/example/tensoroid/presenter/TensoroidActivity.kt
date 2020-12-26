package com.example.tensoroid.presenter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tensoroid.R
import com.example.tensoroid.base.BaseActivity
import com.example.tensoroid.databinding.ActivityMainBinding
import com.example.tensoroid.ext.showToast
import com.example.tensoroid.ext.toBitmap
import com.example.tensoroid.presenter.viewmodel.TensoroidViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executors


@AndroidEntryPoint
class TensoroidActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

    private val tensoroidViewModel by viewModels<TensoroidViewModel>()

    private lateinit var bgChangeDialog: BgChangeDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.run {
            vm = tensoroidViewModel
            fbType.setOnClickListener {
                startBackgroundChangeBottomSheetDialog()
            }
        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        tensoroidViewModel.bgColorTransform.observe(this, {
            if (::bgChangeDialog.isInitialized) {
                if(it == 1){
                    goToAlbum()
                    bgChangeDialog.dismiss()
                }else{
                    bgChangeDialog.dismiss()
                }
            }
        })
    }


    private fun goToAlbum() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        startActivityForResult(intent, REQUEST_CODE_PERMISSIONS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==REQUEST_CODE_PERMISSIONS && resultCode == RESULT_OK){

            val url = data?.data

            if(url != null){
                try {
                    val inputStream = contentResolver.openInputStream(url)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    tensoroidViewModel.bitmap = bitmap
                }catch (e : Exception) {
                    throw Exception()
                }
            }

        }else{
            Toast.makeText(this, "onActivityResult_x", Toast.LENGTH_SHORT).show()
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
                            tensoroidViewModel.inputSource(image.toBitmap())
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
        bgChangeDialog = BgChangeDialog().apply {
            show(
                supportFragmentManager,
                BgChangeDialog.TAG
            )
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}
