package com.dy.reader.flip

import android.view.animation.Interpolator

/**
 * Created by Xian on 2018/2/28.
 */
class CurlInterpolator : Interpolator {
    override fun getInterpolation(input: Float): Float {
//        var x = input * 2.0f
//        if (input < 0.5f) return 0.5f * x * x * x * x * x
//        x = (input - 0.5f) * 2 - 1
//        return 0.5f * x * x * x * x * x + 1
//        return Math.sqrt(1.toDouble() - (input - 1) * (input - 1)).toFloat()
        return getPowOut(input, 3.0)

//        if(input < 0.7F){
//            return input
//        }else{
//            return 1.0f - (1.0f - input) * (1.0f - input)
//        }

//        return getPowInOut(input, 4.0)
//        return 1f - Math.cos(input * Math.PI / 2f).toFloat()
//        return Math.sin(input * Math.PI / 2f).toFloat()
//        return (1.toFloat() - Math.pow(1.0 - input, 4.0)).toFloat()
    }

    private fun getPowOut(elapsedTimeRate: Float, pow: Double): Float {
        return (1.toFloat() - Math.pow((1 - elapsedTimeRate).toDouble(), pow)).toFloat()
    }

    private fun getPowInOut(input: Float, pow: Double): Float {
        var elapsedTimeRate = input * 2F
        return if (elapsedTimeRate < 1) {
            (0.5 * Math.pow(elapsedTimeRate.toDouble(), pow)).toFloat()
        } else (1 - 0.5 * Math.abs(Math.pow((2 - elapsedTimeRate).toDouble(), pow))).toFloat()

    }
}