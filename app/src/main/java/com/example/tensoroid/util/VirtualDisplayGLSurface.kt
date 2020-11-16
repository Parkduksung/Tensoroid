package com.example.tensoroid.util

import android.annotation.TargetApi
import android.opengl.*
import android.os.Build
import android.view.Surface
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import java.util.concurrent.locks.ReentrantLock

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class VirtualDisplayGLSurface(
    encoderInputSurface: Surface?,
    inputWidth: Int,
    inputHeight: Int
) : OnSurfaceDrawable {
    private val TAG = "RecordHelper"
    private val FLOAT_SIZE_BYTES = 4
    protected val TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES
    protected val TRIANGLE_VERTICES_DATA_POS_OFFSET = 0
    protected val TRIANGLE_VERTICES_DATA_UV_OFFSET = 3
    private var mEGLDisplay = EGL14.EGL_NO_DISPLAY
    private var mEGLContext = EGL14.EGL_NO_CONTEXT
    protected var mEGLSurface = EGL14.EGL_NO_SURFACE
    private var inputSurface: Surface? = null

    // 4 각형 그림을 그릴 포인터 지정
    private val mIndicesData = shortArrayOf(0, 1, 2, 0, 2, 3)
    protected lateinit var mTextureIDs: IntArray
    protected var mProgram = 0
    protected var muMVPMatrixHandle = 0
    protected lateinit var mTriangleVertices: FloatBuffer
    protected lateinit var mIndices: ShortBuffer
    protected var mMVPMatrix = FloatArray(16)
    protected var maPositionHandle = 0
    protected var maTextureHandle = 0
    protected var glSurfaceLock: ReentrantLock
    protected var width: Int
    protected var height: Int
    private var rotation: Int
    fun setRotation(rotation: Int) {
        this.rotation = rotation
    }

    /**
     * Prepares EGL. We want a GLES 2.0 context and a surface that supports
     * recording.
     *
     *
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
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL_RECORDABLE_ANDROID, 1,
            EGL14.EGL_NONE
        )
        val configs =
            arrayOfNulls<EGLConfig>(1)
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
     * Discards all resources held by this class, notably the EGL context. Also
     * releases the Surface that was passed to our constructor.
     */
    fun release() {
        glSurfaceLock.lock()
        if (EGL14.eglGetCurrentContext() == mEGLContext) {
            // Clear the current context and surface to ensure they are
            // discarded immediately.
            EGL14.eglMakeCurrent(
                mEGLDisplay,
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT
            )
        }
        EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface)
        EGL14.eglDestroyContext(mEGLDisplay, mEGLContext)
        EGL14.eglTerminate(mEGLDisplay)
        inputSurface!!.release()

        // null everything out so future attempts to use this object will cause
        // an NPE
        mEGLDisplay = EGL14.EGL_NO_DISPLAY
        mEGLContext = EGL14.EGL_NO_CONTEXT
        mEGLSurface = EGL14.EGL_NO_SURFACE
        inputSurface = null
        glSurfaceLock.unlock()
    }

    /**
     * Checks for EGL errors. Throws an exception if one is found.
     */
    protected fun checkEglError(msg: String) {
        var error: Int
        if (EGL14.eglGetError().also { error = it } != EGL14.EGL_SUCCESS) {
            throw RuntimeException(
                "$msg: EGL error: 0x" + Integer.toHexString(
                    error
                )
            )
        }
    }

    /**
     * Makes our EGL context and surface current
     */
    protected fun attachEglContext(): Boolean {
        if (mEGLSurface === EGL14.EGL_NO_SURFACE) {
            return false
        }
        return EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)
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

    /**
     * Sends the presentation time stamp to EGL. Time is expressed in
     * nanoseconds.
     */
    protected fun setPresentationTime(presentationTime: Long) {
        EGLExt.eglPresentationTimeANDROID(mEGLDisplay, mEGLSurface, presentationTime)
        //        checkEglError("eglPresentationTimeANDROID");
    }

    /**
     * OpenGL ES 2.0 Shader code build
     */
    private fun loadShader(shaderType: Int, source: String): Int {
        var shader = GLES20.glCreateShader(shaderType)
        //        checkEglError("glCreateShader type=" + shaderType);
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            GLES20.glDeleteShader(shader)
            shader = 0
        }
        return shader
    }

    private fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) {
            return 0
        }
        val pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (pixelShader == 0) {
            return 0
        }
        var program = GLES20.glCreateProgram()
        //        checkEglError("glCreateProgram");
        if (program == 0) {
        }
        GLES20.glAttachShader(program, vertexShader)
        //        checkEglError("glAttachShader");
        GLES20.glAttachShader(program, pixelShader)
        //        checkEglError("glAttachShader");
        GLES20.glLinkProgram(program)
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES20.GL_TRUE) {
            GLES20.glDeleteProgram(program)
            program = 0
        }
        return program
    }

    /**
     * OpenGL ES 2.0 Vertex Shader code Shader의 좌표를 설정하는 코드
     */
    private val vertexShaderSource = """uniform mat4 uMVPMatrix;
attribute vec4 a_position; 
attribute vec2 a_texCoord; 
varying vec2 v_texCoord; 
void main() 
{ 
  gl_Position = uMVPMatrix * a_position;
 	v_texCoord = a_texCoord; 
} 
"""

    /**
     * OpenGL ES 2.0 Fragment Shader code Shader의 화면을 출력하는 코드(RGBA를 Surface에
     * 출력하기 위한 코드)
     */
    private val fragmentShaderSource = """precision mediump float; 
uniform sampler2D texture; 
varying vec2 v_texCoord;
void main() 
{ 
	gl_FragColor = texture2D(texture, v_texCoord); 
} 
"""

    /**
     * OpenGL ES 2.0 Renderer prepare
     */
    fun prepareRenderer(width: Int, height: Int) {
        if (!attachEglContext()) {
            return
        }
        try {
            var exponentWidth = getMinPowerByTwo(width)
            var exponentHeight = getMinPowerByTwo(height)
            if (exponentWidth > exponentHeight) {
                exponentHeight = exponentWidth
            } else {
                exponentWidth = exponentHeight
            }

            /*
             * 실제 비디오 사이즈와 2의 지수승을 나누어 실제 확대해야할 비율 지정
             */
            val xValue = width.toFloat() / exponentWidth.toFloat()
            val yValue = height.toFloat() / exponentHeight.toFloat()

            /**
             * 해당 코드는 화면의 좌표를 설정하는 코드이며, 텍스쳐 크기가 16의 배수가 아닌 경우를 처리하기 위하여 16의 배수로
             * 확장하고, 이를 아래와 같이 좌표로 설정하는 부분
             *
             * verticesData 값 중 9, 13, 14, 18 번의 값을 2의 지수승으로 나눈 비율로 변경 (확대 사이즈로
             * 처리) private final float[] verticesData = { -1.f, 1.f, 0.0f, //
             * Position 0 0.0f, 0.0f, // TexCoord 0 -1.f, -1.f, 0.0f, //
             * Position 1 1.f, -1.f, 0.0f, // Position 2 xValue, yValue, //
             * TexCoord 2 1.f, 1.f, 0.0f, // Position 3 xValue, 0.0f // TexCoord
             * 3
             *
             * 의로 정의하며 좌표의 값은 0.0 ~ 1.0의 비율값을 가진다.
             */
            val verticesData = floatArrayOf(
                -1f, 1f, 0.0f,  // Position 0
                0.0f, 0.0f,  // TexCoord 0
                -1f, -1f, 0.0f,  // Position 1
                0.0f, yValue,  // TexCoord 1
                1f, -1f, 0.0f,  // Position 2
                xValue, yValue,  // TexCoord 2
                1f, 1f, 0.0f,  // Position 3
                xValue, 0.0f // TexCoord 3
            )

            // Alpha com.rsupport.setting
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
            /**
             * OpenGL ES 2.0 에서 Shader를 사용하기 위한 코드 OpenGL ES에서 Shader 코드를 빌드하도록
             * 처리
             */
            mProgram = createProgram(vertexShaderSource, fragmentShaderSource)
            if (mProgram == 0) {
                throw RuntimeException("Could not create program.")
            }
            /**
             * 빌드한 Shader에 함수별로 매핑하여 사용하기 위한 코드
             */
            maPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_position")
            //            checkEglError("glGetAttribLocation aPosition");
            if (maPositionHandle == -1) {
                throw RuntimeException("Could not get attrib location for a_position")
            }
            maTextureHandle = GLES20.glGetAttribLocation(mProgram, "a_texCoord")
            //            checkEglError("glGetAttribLocation aTextureCoord");
            if (maTextureHandle == -1) {
                throw RuntimeException("Could not get attrib location for a_texCoord")
            }

            // 해당 코드는 화면의 좌표를 조절하기 위한 코드로 불필요시 사용하지 않아도 됩니다.
            // 화면의 텍스쳐를 상하좌우 또는 회전 하기위해서 사용하는 코드입니다.
            muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
            //            checkEglError("glGetUniformLocation uMVPMatrix");
            if (muMVPMatrixHandle == -1) {
                throw RuntimeException("Could not get attrib location for uMVPMatrix")
            }
            mTextureIDs = IntArray(1)
            GLES20.glGenTextures(1, mTextureIDs, 0)
            //            checkEglError("glBindTexture mTextureID");

            /*
             * 화면에 출력하기 위한 텍스쳐 RGBA texture 생성
             */
            mTriangleVertices =
                ByteBuffer.allocateDirect(verticesData.size * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer()
            mTriangleVertices.put(verticesData).position(0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureIDs[0])
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR.toFloat()
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE
            )
            GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_RGBA,
                exponentWidth,
                exponentHeight,
                0,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE,
                null
            )
            GLES20.glClearColor(.0f, .0f, .0f, .0f)
        } finally {
            detachEglContext()
        }
    }

    /**
     * OpenGL ES에서 사용할 2의 배수 처리
     */
    private fun getMinPowerByTwo(value: Int): Int {
        var result = 2
        do {
            result *= 2
        } while (result < value)
        return result
    }

    override fun initialized() {
        mMVPMatrix = FloatArray(16)
        mIndices = ByteBuffer.allocateDirect(mIndicesData.size * FLOAT_SIZE_BYTES)
            .order(ByteOrder.nativeOrder()).asShortBuffer()
        mIndices.put(mIndicesData).position(0)
        eglSetup()
        prepareRenderer(width, height)
    }

    override fun onDrawable(
        imageBuffer: ByteBuffer?,
        width: Int,
        height: Int,
        pixelStride: Int,
        rowStride: Int,
        rowPadding: Int,
        presentationTime: Long
    ) {
        glSurfaceLock.lock()
        if (mEGLSurface === EGL14.EGL_NO_SURFACE) {
            glSurfaceLock.unlock()
            return
        }
        if (!attachEglContext()) {
            return
        }
        try {
            // Clear the color buffer
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
            GLES20.glUseProgram(mProgram)
            /**
             * capture image mapping
             *
             * 좌표를 Shader의 각 함수에 맵핑하는 부분과 RGBA 데이터를 출력하는 코드
             */
            // get handle to vertex shader's a_position member
            // Shader의 Position(vertex)과 Texture(Fragment)를 맵핑하기 위한 코드로
            // mTriangleVertices의 좌표값을 활용
            mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET)
            GLES20.glVertexAttribPointer(
                maPositionHandle,
                3,
                GLES20.GL_FLOAT,
                false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES,
                mTriangleVertices
            )
            //            checkEglError("glVertexAttribPointer maPosition");
            GLES20.glEnableVertexAttribArray(maPositionHandle)
            //            checkEglError("glEnableVertexAttribArray maPositionHandle");
            mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET)
            GLES20.glVertexAttribPointer(
                maTextureHandle,
                2,
                GLES20.GL_FLOAT,
                false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES,
                mTriangleVertices
            )
            //            checkEglError("glVertexAttribPointer maTextureHandle");
            GLES20.glEnableVertexAttribArray(maTextureHandle)
            //            checkEglError("glEnableVertexAttribArray maTextureHandle");
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureIDs[0])
            GLES20.glTexSubImage2D(
                GLES20.GL_TEXTURE_2D,
                0,
                0,
                0,
                rowStride / pixelStride,
                height,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE,
                imageBuffer
            )
            Matrix.setIdentityM(mMVPMatrix, 0)
            Matrix.rotateM(mMVPMatrix, 0, rotation.toFloat(), 0f, 0f, -1.0f)
            GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0)
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mIndices)
            setPresentationTime(presentationTime)
            swapBuffers()
        } finally {
            detachEglContext()
        }
        glSurfaceLock.unlock()
    }

    companion object {
        private const val EGL_RECORDABLE_ANDROID = 0x3142
    }

    /**
     * Creates a CodecInputSurface from a surface.
     */
    init {
        glSurfaceLock = ReentrantLock(true)
        inputSurface = encoderInputSurface
        width = inputWidth
        height = inputHeight
        rotation = 0
    }
}