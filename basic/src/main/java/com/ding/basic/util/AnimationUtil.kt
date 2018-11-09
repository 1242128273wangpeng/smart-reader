package com.ding.basic.util

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.OvershootInterpolator

/**
 * Desc
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/10/13 09:21
 */
object AnimationUtil {

    fun flipAnimatorXViewShow(oldView: View, newView: View, time: Long) {

        val animatorOld = ObjectAnimator.ofFloat(oldView, "rotationX", 0f, 90f)
        animatorOld.duration = time

        val animatorNew = ObjectAnimator.ofFloat(newView, "rotationX", -90f, 0f)
        animatorNew.duration = time
        animatorNew.interpolator = OvershootInterpolator(1.5f)

        animatorOld.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                animatorNew.start()
                oldView.visibility = View.GONE
                newView.visibility = View.VISIBLE
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {

            }
        })
        animatorOld.start()
    }

    @Synchronized
    fun flipAnimatorYViewShow(oldView: View, newView: View, time: Long, position: Int, callBack: ((position: Int) -> Unit)? = null) {

        if (oldView.visibility == View.GONE || newView.visibility == View.VISIBLE) return

        val animatorOld = ObjectAnimator.ofFloat(oldView, "rotationY", 0f, 90f)
        animatorOld.duration = time

        val animatorNew = ObjectAnimator.ofFloat(newView, "rotationY", -90f, 0f)
        animatorNew.duration = time
        animatorNew.interpolator = OvershootInterpolator(2.0f)

        animatorOld.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator) {
                oldView.visibility = View.GONE
                animatorNew.start()
                newView.visibility = View.VISIBLE
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {

            }
        })
        animatorNew.addListener(object :Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                callBack?.invoke(position)
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {

            }

        })
        animatorOld.start()

    }
}
