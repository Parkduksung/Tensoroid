package com.example.tensoroid.presenter.viewmodel

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.util.Log
import android.view.Surface

class GLSurface {

    private lateinit var inputSurface: Surface

    private var mEGLDisplay = EGL14.EGL_NO_DISPLAY
    private var mEGLContext = EGL14.EGL_NO_CONTEXT
    protected var mEGLSurface = EGL14.EGL_NO_SURFACE


    /**
     * Prepares EGL. We want a GLES 2.0 context and a surface that supports
     * recording.
     * <p/>
     * 화면을 가상으로 생성하기 위해서 EGL을 사용. Encoder에서 사용하기에 화면이 출력될 필요는 없어 아래와 같이 EGL로 화면 처리
     */

    private fun eglSetup() {
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (mEGLDisplay === EGL14.EGL_NO_DISPLAY) {
            throw RuntimeException("unable to get EGL14 display")
        }

        val version = IntArray(2)
        if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
            mEGLDisplay = null
            throw RuntimeException("unable to initialize EGL14")
        }


        // Configure EGL for recording and OpenGL ES 2.0.
        val attribList = intArrayOf(
            EGL14.EGL_RED_SIZE,
            8,
            EGL14.EGL_GREEN_SIZE,
            8,
            EGL14.EGL_BLUE_SIZE,
            8,
            EGL14.EGL_ALPHA_SIZE,
            8,
            EGL14.EGL_RENDERABLE_TYPE,
            EGL14.EGL_OPENGL_ES2_BIT,
            EGL_RECORDABLE_ANDROID,
            1,
            EGL14.EGL_NONE
        )

        val configs = arrayOfNulls<EGLConfig>(1)

        val numConfigs = IntArray(1)

        if (!EGL14.eglChooseConfig(
                mEGLDisplay,
                attribList,
                0,
                configs,
                0,
                configs.size,
                numConfigs,
                0
            )
        ) {
            throw RuntimeException("unable to find RGB888+recordable ES2 EGL config")
        }


        // Configure context for OpenGL ES 2.0.
        val attrib_list = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        )
        mEGLContext = EGL14.eglCreateContext(
            mEGLDisplay,
            configs[0],
            EGL14.EGL_NO_CONTEXT,
            attrib_list,
            0
        )
        if (mEGLContext == null) {
            throw RuntimeException("null context")
        }

        // Create a window surface, and attach it to the Surface we received.

        // Create a window surface, and attach it to the Surface we received.
        val surfaceAttribList = intArrayOf(
            EGL14.EGL_NONE
        )
        mEGLSurface = EGL14.eglCreateWindowSurface(
            mEGLDisplay,
            configs[0],
            inputSurface,
            surfaceAttribList,
            0
        )
        if (mEGLSurface == null) {
            throw RuntimeException("surface was null")
        }
    }

    /**
     * Makes our EGL context and surface current
     */
    protected fun attachEglContext(): Boolean {
        if (mEGLSurface === EGL14.EGL_NO_SURFACE) {
            Log.e("", "EGLSurface is EGL_NO_SURFACE")
            return false
        }
        if (!EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)) {
            Log.e("", "eglMakeCurrent")
            return false
        }
        return true
    }

    protected fun detachEglContext() {
        if (mEGLDisplay != null) {
            EGL14.eglMakeCurrent(
                mEGLDisplay,
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT
            )
        }
    }

    /**
     * Calls eglSwapBuffers. Use this to "publish" the current frame.
     */
    fun swapBuffers(): Boolean {
        return EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface)
    }

    companion object {

        private const val EGL_RECORDABLE_ANDROID = 0x3142

    }

}