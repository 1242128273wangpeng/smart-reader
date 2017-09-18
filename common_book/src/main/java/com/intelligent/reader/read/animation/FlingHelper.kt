package com.intelligent.reader.read.animation

import android.hardware.SensorManager
import android.view.ViewConfiguration
import net.lzbook.kit.app.BaseBookApplication

/**
 * Created by xian on 2017/9/16.
 */
object FlingHelper {
    val DECELERATION_RATE = (Math.log(0.78) / Math.log(0.9)).toFloat()
    val INFLEXION = 0.35f
    private var mFlingFriction = ViewConfiguration.getScrollFriction()

    val mPpi: Float by lazy {
        BaseBookApplication.getGlobalContext().getResources().getDisplayMetrics().density * 160.0f
    }

    val mPhysicalCoeff: Float by lazy {
        computeDeceleration(0.84f)
    }

    fun computeDeceleration(friction: Float): Float {
        return SensorManager.GRAVITY_EARTH * 39.37f * mPpi * friction
    }

    fun getSplineDeceleration(velocity: Float): Double {
        return Math.log((INFLEXION * Math.abs(velocity) / (mFlingFriction * mPhysicalCoeff)).toDouble())
    }

    fun getSplineFlingDuration(velocity: Float): Int {
        val l = getSplineDeceleration(velocity)
        val decelMinusOne = DECELERATION_RATE - 1.0
        return (1000.0 * Math.exp(l / decelMinusOne)).toInt()
    }

    fun getSplineFlingDistance(velocity: Float): Double {
        val l = getSplineDeceleration(velocity)
        val decelMinusOne = DECELERATION_RATE - 1.0
        return mFlingFriction.toDouble() * mPhysicalCoeff.toDouble() * Math.exp(DECELERATION_RATE / decelMinusOne * l)
    }

    fun getTargetDistance(velocity: Float): Int {
        val totalDistance = getSplineFlingDistance(velocity)
        return (totalDistance * Math.signum(velocity)).toInt()
    }
}