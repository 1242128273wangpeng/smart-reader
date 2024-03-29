package com.dy.reader.animation

interface IGLAnimation {

    fun loadProgram()
    fun unloadProgram()


    fun drawFrame()

    fun down(x: Float, y: Float)
    fun move(x: Float, y: Float)
    fun up(x: Float, y: Float, xVelocity: Float)
    fun cancel()

    fun onFlipUp()
    fun onFlipDown()
}