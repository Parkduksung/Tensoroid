package com.example.tensoroid.util

import android.opengl.GLES20
import android.util.Log

object OpenGLUtils {

    private const val TAG = "openGLUtil"

    /**
     * OpenGL ES 2.0 Vertex Shader code Shader의 좌표를 설정하는 코드
     */
    private const val vertexShaderSource = "uniform mat4 uMVPMatrix;\n" +
            "attribute vec4 a_position; \n" +
            "attribute vec2 a_texCoord; \n" +
            "varying vec2 v_texCoord; \n" +
            "void main() \n" +
            "{ \n" +
            "  gl_Position = uMVPMatrix * a_position;\n" +
            " 	v_texCoord = a_texCoord; \n" +
            "} \n"

    /**
     * OpenGL ES 2.0 Fragment Shader code Shader의 화면을 출력하는 코드(RGBA를 Surface에
     * 출력하기 위한 코드)
     */
    private const val fragmentShaderSource = "precision mediump float; \n" +
            "uniform sampler2D texture; \n" +
            "varying vec2 v_texCoord;\n" +
            "void main() \n" +
            "{ \n" +
            "	gl_FragColor = texture2D(texture, v_texCoord); \n" +
            "} \n"


    private fun loadShader(shaderType: Int, source: String): Int {
        var shader = GLES20.glCreateShader(shaderType)

        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)

        val compiled = IntArray(1)

        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)

        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader $shaderType:")
            Log.e(TAG, " " + GLES20.glGetShaderInfoLog(shader))
            GLES20.glDeleteShader(shader)
            shader = 0
        }

        return shader
    }

    private fun createProgram(
        vertexSource: String = vertexShaderSource,
        fragmentSource: String = fragmentShaderSource
    ): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) {
            return 0
        }
        val pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (pixelShader == 0) {
            return 0
        }
        var program = GLES20.glCreateProgram()
        if (program == 0) {
            Log.e(TAG, "Could not create program")
        }
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, pixelShader)
        GLES20.glLinkProgram(program)
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Could not link program: ")
            Log.e(TAG, GLES20.glGetProgramInfoLog(program))
            GLES20.glDeleteProgram(program)
            program = 0
        }
        return program
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

}