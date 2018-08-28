package com.dy.reader.animation

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.dy.reader.R
import com.dy.reader.flip.Status
import com.dy.reader.page.GLPage
import com.dy.reader.helper.glCheckErr
import com.dy.reader.helper.loadShader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.properties.Delegates

/**
 * Created by xian on 18-3-23.
 */
open class TranslationAnimation(glSurfaceView: GLSurfaceView) : AbsGLAnimation(glSurfaceView) {

    val quadVertex = floatArrayOf(
            -1.0f, 1.0f, 0.0f, // Position 0
            -1.0f, -1f, 0.0f, // Position 1
            1.0f, -1f, 0.0f, // Position 2
            1.0f, 1.0f, 0.0f // Position 3
    )

    val coordsVertex = floatArrayOf(
            0.0f, 0.0f, // TexCoord 2
            0.0f, 1.0f,  // TexCoord 3
            1.0f, 1.0f, // TexCoord 0
            1.0f, 0.0f // TexCoord 1
    )

    val quadIndex = shortArrayOf(
            0.toShort(), // Position 0
            1.toShort(), // Position 1
            2.toShort(), // Position 2
            2.toShort(), // Position 2
            3.toShort(), // Position 3
            0.toShort())// Position 0


    // float size = 4
    var vertex = ByteBuffer.allocateDirect(quadVertex.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()

    // float size = 4
    var coords = ByteBuffer.allocateDirect(coordsVertex.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()

    // short size = 2
    var index = ByteBuffer.allocateDirect(quadIndex.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()

    protected val firstVMatrix = FloatArray(16)
    protected val secondVMatrix = FloatArray(16)

    var vertexShader by Delegates.notNull<Int>()
    var fragmentShader by Delegates.notNull<Int>()
    var program by Delegates.notNull<Int>()

    var av4_position by Delegates.notNull<Int>()
    var av2_texCoord by Delegates.notNull<Int>()
    var u_sampler2d by Delegates.notNull<Int>()
    var u_MVPMatrix by Delegates.notNull<Int>()


    override fun loadProgram() {
        GLES20.glViewport(0, 0, width, height)
//        program = loadProgram(glSurfaceView.context, R.raw.v_translation, R.raw.f_translation)

        vertexShader = loadShader(glSurfaceView.context, GLES20.GL_VERTEX_SHADER,
                R.raw.v_translation)
        // Load the fragment shaders
        fragmentShader = loadShader(glSurfaceView.context, GLES20.GL_FRAGMENT_SHADER,
                R.raw.f_translation)

        program = GLES20.glCreateProgram()

        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        // Link the program
        GLES20.glLinkProgram(program)

        GLES20.glUseProgram(program)

        av4_position = GLES20.glGetAttribLocation(program, "av4_position")
        av2_texCoord = GLES20.glGetAttribLocation(program, "av2_texCoord")
        u_sampler2d = GLES20.glGetUniformLocation(program, "u_sampler2d")
        u_MVPMatrix = GLES20.glGetUniformLocation(program, "u_MVPMatrix")

        vertex.put(quadVertex).position(0)
        coords.put(coordsVertex).position(0)
        index.put(quadIndex).position(0)

        resetOffset()

        glCheckErr()
    }

    override fun unloadProgram() {
        GLES20.glDeleteProgram(program)
        GLES20.glDeleteShader(vertexShader)
        GLES20.glDeleteShader(fragmentShader)
    }

    override fun onConfirmOritation() {
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        if (status == Status.SLIDING_TO_LEFT || status == Status.FLYING_TO_LEFT) {
            currentTranslateX = 0
        } else if (status == Status.SLIDING_TO_RIGHT || status == Status.FLYING_TO_RIGHT) {
            currentTranslateX = -2
        }
    }

    override fun resetOffset() {
        Matrix.setIdentityM(firstVMatrix, 0)

        Matrix.setIdentityM(secondVMatrix, 0)
        Matrix.translateM(secondVMatrix, 0, 2f, 0F, 0.1f)
    }

    private var currentTranslateX = 0

    private var curDistanceX = 0F

    override fun computeOffset() {
        curDistanceX = currentPoint.x - originPoint.x

        var xDis = curDistanceX * 2 / width

        when (status) {
            Status.SLIDING_TO_LEFT, Status.FLYING_TO_LEFT, Status.BACK_TO_RIGHT -> {

                Matrix.setIdentityM(firstVMatrix, 0)
                Matrix.translateM(firstVMatrix, 0, xDis + currentTranslateX, 0F, 0f)

                Matrix.setIdentityM(secondVMatrix, 0)
                Matrix.translateM(secondVMatrix, 0, 2 + xDis + currentTranslateX, 0F, 0f)
            }

            Status.SLIDING_TO_RIGHT, Status.FLYING_TO_RIGHT, Status.BACK_TO_LEFT -> {

                Matrix.setIdentityM(firstVMatrix, 0)
                Matrix.translateM(firstVMatrix, 0, xDis + currentTranslateX, 0F, 0f)

                Matrix.setIdentityM(secondVMatrix, 0)
                Matrix.translateM(secondVMatrix, 0, 2 + xDis + currentTranslateX, 0F, 0f)

            }
        }
    }

    override fun getMargin(): Float {
        return 0F
    }

    override fun draw() {

        GLES20.glEnableVertexAttribArray(av4_position)
        GLES20.glEnableVertexAttribArray(av2_texCoord)


        if (secondPage.isLoaded.get()) {
            drawTexture(secondPage, secondVMatrix)
        } else {
            println("miss draw ${secondPage.position}")
        }

        if (firstPage.isLoaded.get()) {
            drawTexture(firstPage, firstVMatrix)
        } else {
            println("miss draw ${firstPage.position}")
        }
    }

    protected fun drawTexture(page: GLPage, matrix: FloatArray) {
        if (page.textureID != -1) {

            // clear screen to black
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, page.textureID)

            // Set the sampler to texture unit 0
            GLES20.glUniform1i(u_sampler2d, 0)

            GLES20.glUniformMatrix4fv(u_MVPMatrix, 1, false,
                    matrix, 0)

            // load the position
            GLES20.glVertexAttribPointer(av4_position,
                    3, GLES20.GL_FLOAT,
                    false, 0, vertex)

            // load the texture coordinate
            GLES20.glVertexAttribPointer(av2_texCoord,
                    2, GLES20.GL_FLOAT,
                    false, 0, coords)

            GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT,
                    index)

            glCheckErr()
        }
    }
}