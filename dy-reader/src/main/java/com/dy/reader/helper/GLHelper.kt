package com.dy.reader.helper

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import android.support.annotation.RawRes


/**
 * Created by admin on 08/03/2018.
 */

val INDEX_TEXTURE_ID = 0
val INDEX_TEXTURE_WIDTH = 1
val INDEX_TEXTURE_HEIGHT = 2

fun glCheckErr() {
    var glGetError = GLES20.glGetError()
    while (glGetError != GLES20.GL_NO_ERROR) {
        println(" = = = = = = = = = = = = = = = = = = = = = = = = = ")
        Exception("glError : $glGetError").printStackTrace()
        println(" = = = = = = = = = = = = = = = = = = = = = = = = = ")
        glGetError = GLES20.glGetError()
    }
}

fun loadShader(context: Context, shaderType: Int, @RawRes source: Int): Int {

    // Create the shader object
    val shader = GLES20.glCreateShader(shaderType)
    if (shader == 0) {
        throw RuntimeException("Error create shader.")
    }
    val compiled = IntArray(1)
    // Load the shader source
    GLES20.glShaderSource(shader, context.resources.openRawResource(source).bufferedReader().readText())
    // Compile the shader
    GLES20.glCompileShader(shader)
    // Check the compile status
    GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
    if (compiled[0] == 0) {
        GLES20.glDeleteShader(shader)
        throw RuntimeException("Error compile shader: " + GLES20.glGetShaderInfoLog(shader))
    }
    return shader
}


fun loadProgram(context: Context, @RawRes verSource: Int, @RawRes fragSource: Int): Int {

    // Load the vertex shaders
    val vertexShader = loadShader(context, GLES20.GL_VERTEX_SHADER,
            verSource)
    // Load the fragment shaders
    val fragmentShader = loadShader(context, GLES20.GL_FRAGMENT_SHADER,
            fragSource)
    // Create the program object
    val program = GLES20.glCreateProgram()
    if (program == 0) {
        throw RuntimeException("Error create program.")
    }
    GLES20.glAttachShader(program, vertexShader)
    GLES20.glAttachShader(program, fragmentShader)
    // Link the program
    GLES20.glLinkProgram(program)
    val linked = IntArray(1)
    // Check the link status
    GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linked, 0)
    if (linked[0] == 0) {
        GLES20.glDeleteProgram(program)
        throw RuntimeException("Error linking program: " + GLES20.glGetProgramInfoLog(program))
    }
    // Free up no longer needed shader resources
    GLES20.glDeleteShader(vertexShader)
    GLES20.glDeleteShader(fragmentShader)
    return program
}

//fun loadTexture(assetsPath: String): IntArray {
//
//    val textureId = IntArray(1)
//    // Generate a texture object
//    GLES20.glGenTextures(1, textureId, 0)
//    var result = IntArray(3)
//    if (textureId[0] != 0) {
//        val `is` = App.app.assets.open(assetsPath)
//        val bitmap: Bitmap
//        try {
//            bitmap = BitmapFactory.decodeStream(`is`)
//        } finally {
//            try {
//                `is`.close()
//            } catch (e: IOException) {
//                throw RuntimeException("Error loading Bitmap.")
//            }
//
//        }
//
//        result = loadTexture(bitmap)
//        // Recycle the bitmap, since its data has been loaded into OpenGL.
//        bitmap.recycle()
//    } else {
//        throw RuntimeException("Error loading texture.")
//    }
//    return result
//}

fun loadTexture(bitmap: Bitmap): IntArray {

    val textureId = IntArray(1)
    // Generate a texture object
    GLES20.glGenTextures(1, textureId, 0)
    var result = IntArray(3)

    result[INDEX_TEXTURE_ID] = textureId[0] // TEXTURE_ID
    result[INDEX_TEXTURE_WIDTH] = bitmap.width // TEXTURE_WIDTH
    result[INDEX_TEXTURE_HEIGHT] = bitmap.height // TEXTURE_HEIGHT
    // Bind to the texture in OpenGL
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0])
    // Set filtering
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE)
    // Load the bitmap into the bound texture.
    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

    glCheckErr()

    return result
}

fun loadTexture(bitmap: Bitmap, textureId: Int) {
    // Bind to the texture in OpenGL
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
    // Set filtering
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE)
    // Load the bitmap into the bound texture.
    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

    glCheckErr()
}

//fun loadShadowTexture(): Int {
//    val drawable = App.app.resources.getDrawable(R.drawable.page_shadow)
//
//    val bitmap = Bitmap.createBitmap(1000, 1, Bitmap.Config.ARGB_8888)
//    drawable.setBounds(0, 0, 1000, 1)
//
//    val canvas = Canvas(bitmap)
//    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
//
//    drawable.draw(canvas)
//
//    var id = loadTexture(bitmap)[INDEX_TEXTURE_ID]
//
//    bitmap.recycle()
//
//    return id
//}

