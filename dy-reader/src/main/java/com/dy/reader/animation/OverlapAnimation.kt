package com.dy.reader.animation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.dy.reader.R
import com.dy.reader.Reader
import com.dy.reader.flip.Status
import com.dy.reader.helper.AppHelper
import com.intelligent.reader.reader.v2.TranslationAnimation
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by xian on 18-3-25.
 */
class OverlapAnimation(glSurfaceView: GLSurfaceView) : TranslationAnimation(glSurfaceView) {

    //    val SHADOW_WIDTH = 0.07F
    val SHADOW_WIDTH by lazy {
        AppHelper.screenWidth * 0.07F / glSurfaceView.width
    }


    val shadowquadVertex = floatArrayOf(
            1.0f, 1.0f, 0.0f, // Position 0
            1.0f, -1f, 0.0f, // Position 1
            1.0f + SHADOW_WIDTH, -01f, 0.0f, // Position 2
            1.0f + SHADOW_WIDTH, 1.0f, 0.0f // Position 3
    )

    val shadowcoordsVertex = floatArrayOf(

            0F, 0F,
            0F, 1F,
            1f, 1F,
            1f, 0f
    )

    val shadowquadIndex = shortArrayOf(
            0.toShort(), // Position 0
            1.toShort(), // Position 1
            2.toShort(), // Position 2
            2.toShort(), // Position 2
            3.toShort(), // Position 3
            0.toShort())// Position 0


    // float size = 4
    var shadowvertex = ByteBuffer.allocateDirect(shadowquadVertex.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()

    // float size = 4
    var shadowcoords = ByteBuffer.allocateDirect(shadowcoordsVertex.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()

    // short size = 2
    var shadowindex = ByteBuffer.allocateDirect(shadowquadIndex.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()


    var shadowId = -1
    private val shadowVMatrix = FloatArray(16)

    override fun loadProgram() {
        super.loadProgram()
        shadowvertex.put(shadowquadVertex).position(0)
        shadowcoords.put(shadowcoordsVertex).position(0)
        shadowindex.put(shadowquadIndex).position(0)
        resetOffset()
    }

    override fun onConfirmOritation() {
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }

    override fun resetOffset() {

        Matrix.setIdentityM(firstVMatrix, 0)
        Matrix.translateM(firstVMatrix, 0, 0f, 0F, 0.1f)

        Matrix.setIdentityM(secondVMatrix, 0)

        Matrix.setIdentityM(shadowVMatrix, 0)
        Matrix.translateM(shadowVMatrix, 0, 0f, 0F, 0.1f)
    }

    private var curDistanceX = 0F

    override fun computeOffset() {
        curDistanceX = currentPoint.x - originPoint.x
        var xDis = curDistanceX * 2 / width

//        println("xdis : $xDis")

        when (status) {
            Status.SLIDING_TO_LEFT, Status.FLYING_TO_LEFT, Status.BACK_TO_RIGHT -> {

                Matrix.setIdentityM(firstVMatrix, 0)
                Matrix.translateM(firstVMatrix, 0, xDis, 0F, 0.1f)

                Matrix.setIdentityM(shadowVMatrix, 0)
                Matrix.translateM(shadowVMatrix, 0, xDis, 0F, 0.1f)
            }

            Status.SLIDING_TO_RIGHT, Status.FLYING_TO_RIGHT, Status.BACK_TO_LEFT -> {

                Matrix.setIdentityM(firstVMatrix, 0)
                Matrix.translateM(firstVMatrix, 0, xDis - 2, 0F, 0.1f)

                Matrix.setIdentityM(shadowVMatrix, 0)
                Matrix.translateM(shadowVMatrix, 0, xDis - 2, 0F, 0.1f)
            }
        }
    }

    override fun getMargin(): Float {
        return SHADOW_WIDTH
    }

    override fun draw() {
        if (shadowId == -1) {
            shadowId = loadShadowTexture()
        }

        super.draw()

        drawShadow()
    }

    private fun drawShadow() {
        if (shadowId != -1) {

            // clear screen to black
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, shadowId)

            // Set the sampler to texture unit 0
            GLES20.glUniform1i(u_sampler2d, 0)

            GLES20.glEnable(GLES20.GL_BLEND)

            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
//            GLES20.glBlendFunc(GLES20.GL_SRC_COLOR, GLES20.GL_ONE_MINUS_SRC_COLOR)
//            GLES20.glBlendFunc(GLES20.GL_ONE_MINUS_SRC_COLOR, GLES20.GL_SRC_COLOR)

            GLES20.glUniformMatrix4fv(u_MVPMatrix, 1, false,
                    shadowVMatrix, 0)

            // load the position
            GLES20.glVertexAttribPointer(av4_position,
                    3, GLES20.GL_FLOAT,
                    false, 0, shadowvertex)

            // load the texture coordinate
            GLES20.glVertexAttribPointer(av2_texCoord,
                    2, GLES20.GL_FLOAT,
                    false, 0, shadowcoords)

            GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT,
                    shadowindex)
            GLES20.glDisable(GLES20.GL_BLEND)

            com.intelligent.reader.reader.v2.glCheckErr()
        }
    }


    fun loadShadowTexture(): Int {
        val drawable = Reader.context.resources.getDrawable(R.drawable.reader_page_shadow_bg)

        val bitmap = Bitmap.createBitmap(1000, 1, Bitmap.Config.ARGB_8888)
        drawable.setBounds(0, 0, 1000, 1)

        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        drawable.draw(canvas)

        var id = com.intelligent.reader.reader.v2.loadTexture(bitmap)[com.intelligent.reader.reader.v2.INDEX_TEXTURE_ID]

        bitmap.recycle()

        return id
    }
}