package com.dy.reader.animation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.view.animation.LinearInterpolator
import android.widget.Scroller
import com.dy.reader.R
import com.dy.reader.event.EventReaderConfig
import com.dy.reader.page.PageManager
import com.dy.reader.setting.ReaderSettings
import com.dy.reader.setting.ReaderStatus
import com.intelligent.reader.reader.v2.*
import org.greenrobot.eventbus.EventBus
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.properties.Delegates

class AutoAnimation(val glSurfaceView: GLSurfaceView) : IGLAnimation {
    override fun onFlipUp() {
    }

    override fun onFlipDown() {
    }


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

    var vShader by Delegates.notNull<Int>()
    var fShader by Delegates.notNull<Int>()
    var program by Delegates.notNull<Int>()

    var av4_position by Delegates.notNull<Int>()
    var av2_texCoord by Delegates.notNull<Int>()
    var u_sampler2d by Delegates.notNull<Int>()
    var u_MVPMatrix by Delegates.notNull<Int>()


    //    val SHADOW_WIDTH = 0.07F
    val SHADOW_WIDTH by lazy {
        0.02F
    }


    val shadowquadVertex = floatArrayOf(
            -1.0f, 0f, 0.0f, // Position 0
            -1.0f, 0f - SHADOW_WIDTH, 0.0f, // Position 1
            1.0f, 0f - SHADOW_WIDTH, 0.0f, // Position 2
            1.0f, 0f, 0.0f // Position 3
    )

    val shadowcoordsVertex = floatArrayOf(
            0F, 1F,
            1f, 1F,
            1f, 0f,
            0F, 0F
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


    val scroller by lazy {
        Scroller(glSurfaceView.context, LinearInterpolator())
    }


    var yOffset = 0

    override fun loadProgram() {

        vShader = loadShader(glSurfaceView.context, GLES20.GL_VERTEX_SHADER, R.raw.v_translation)
        fShader = loadShader(glSurfaceView.context, GLES20.GL_FRAGMENT_SHADER, R.raw.f_translation)

        // Create the program object
        program = GLES20.glCreateProgram()
        if (program == 0) {
            throw RuntimeException("Error create program.")
        }
        GLES20.glAttachShader(program, vShader)
        GLES20.glAttachShader(program, fShader)
        // Link the program
        GLES20.glLinkProgram(program)
        val linked = IntArray(1)
        // Check the link status
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linked, 0)
        if (linked[0] == 0) {
            throw RuntimeException("Error linking program: " + GLES20.glGetProgramInfoLog(program))
        }

//        program = org.danny.glreader.loadProgram(R.raw.v_translation, R.raw.f_translation)

        av4_position = GLES20.glGetAttribLocation(program, "av4_position")
        av2_texCoord = GLES20.glGetAttribLocation(program, "av2_texCoord")
        u_sampler2d = GLES20.glGetUniformLocation(program, "u_sampler2d")
        u_MVPMatrix = GLES20.glGetUniformLocation(program, "u_MVPMatrix")

        vertex.put(quadVertex).position(0)
        coords.put(coordsVertex).position(0)
        index.put(quadIndex).position(0)

        shadowvertex.put(shadowquadVertex).position(0)
        shadowcoords.put(shadowcoordsVertex).position(0)
        shadowindex.put(shadowquadIndex).position(0)

        shadowId = loadShadowTexture()

        resetOffset()

        glCheckErr()

        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        startAnimation()
    }

    fun resetOffset() {

        Matrix.setIdentityM(secondVMatrix, 0)
        Matrix.translateM(secondVMatrix, 0, 0f, 0F, 0.1f)

        Matrix.setIdentityM(firstVMatrix, 0)


        Matrix.setIdentityM(shadowVMatrix, 0)
        Matrix.translateM(shadowVMatrix, 0, 0f, 0F, 0.2f)

    }

    override fun unloadProgram() {
        GLES20.glDeleteProgram(program)
        if (shadowId != -1) {
            GLES20.glDeleteTextures(1, arrayOf(shadowId).toIntArray(), 0)
        }

        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        glSurfaceView.requestRender()
    }

    var firstPage by Delegates.notNull<GLPage>()
    var secondPage by Delegates.notNull<GLPage>()

    init {
        firstPage = PageManager.currentPage!!
        secondPage = PageManager.rightPage!!
    }


    override fun drawFrame() {
        GLES20.glUseProgram(program)

        GLES20.glEnableVertexAttribArray(av4_position)
        GLES20.glEnableVertexAttribArray(av2_texCoord)

        if (firstPage.isLoaded.get()) {
            drawTexture(firstPage, firstVMatrix)
        } else {
            println("miss draw ${firstPage.position}")
        }

        if (secondPage.isLoaded.get()) {
            var usedScroller = false
            if (scroller.computeScrollOffset()) {
                usedScroller = true

                yOffset = scroller.currY
            }
            val percent = yOffset / glSurfaceView.height.toFloat()
            val vp = 2 * (0.5F - percent)
            val cp = percent

            vertex.put(4, vp)
            vertex.put(7, vp)
            coords.put(3, cp)
            coords.put(5, cp)

            drawTexture(secondPage, secondVMatrix)
            vertex.put(quadVertex).position(0)
            coords.put(coordsVertex).position(0)


            Matrix.setIdentityM(shadowVMatrix, 0)
            Matrix.translateM(shadowVMatrix, 0, 0F, vp, 0F)

            drawShadow()


            if (usedScroller && scroller.isFinished && scroller.currY == scroller.finalY) {

                val shouldUseEvent = PageManager.isReadyForward()

                if (shouldUseEvent) {
                    firstPage = PageManager.currentPage
                    secondPage = PageManager.rightPage
                    yOffset = 0
                    startAnimation()
                }else if(PageManager.currentPage.position.group == ReaderStatus.chapterCount -1
                        && PageManager.currentPage.position.index == PageManager.currentPage.position.groupChildCount -1){

                    ReaderSettings.instance.isAutoReading = false

                    EventBus.getDefault().post(EventReaderConfig(ReaderSettings.ConfigType.GO_TO_BOOKEND))
                }
            }


        } else {
            println("miss draw ${secondPage.position}")
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

            glCheckErr()
        }
    }


    fun loadShadowTexture(): Int {
        val drawable = glSurfaceView.context.resources.getDrawable(R.drawable.reader_page_shadow_bg)

        val bitmap = Bitmap.createBitmap(1000, 1, Bitmap.Config.ARGB_8888)
        drawable.setBounds(0, 0, 1000, 1)

        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        drawable.draw(canvas)

        var id = loadTexture(bitmap)[INDEX_TEXTURE_ID]

        bitmap.recycle()

        return id
    }

    private fun startAnimation(forward: Boolean = true) {
        if (forward) {
            PageManager.forwardPage()
        }

        val duration = (10 + (20 - ReaderSettings.instance.autoReadSpeed)) * 1000

        scroller.startScroll(0, yOffset, 0, glSurfaceView.height - yOffset,
                (duration * (1F - yOffset.toFloat() / (glSurfaceView.height + 1))).toInt())
    }

    fun pause() {
        scroller.forceFinished(true)
    }

    fun resume() {
        startAnimation(false)
    }

    var lastY = 0
    override fun down(x: Float, y: Float) {
        lastY = y.toInt()
    }

    override fun move(x: Float, y: Float) {
        scroller.abortAnimation()
        yOffset += y.toInt() - lastY
        lastY = y.toInt()

        yOffset = Math.max(0, yOffset)
        yOffset = Math.min(glSurfaceView.height, yOffset)
    }

    override fun up(x: Float, y: Float, xVelocity: Float) {
        startAnimation(false)
    }

    override fun cancel() {
    }
}