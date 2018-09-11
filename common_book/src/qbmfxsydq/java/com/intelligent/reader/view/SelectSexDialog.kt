package com.intelligent.reader.view

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import com.intelligent.reader.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import net.lzbook.kit.book.view.MyDialog
import kotlinx.android.synthetic.qbmfxsydq.dialog_select_sex.*
import net.lzbook.kit.utils.AppLog
import java.util.concurrent.TimeUnit

/**
 * Desc 选男女
 * Author zhenxiang
 * Mail zhenxiang_lin@dingyuegroup.cn
 * Date 2018\09\110 0013 16:06
 */
class SelectSexDialog(val activity: Activity) {
    private val dialog = MyDialog(activity, R.layout.dialog_select_sex, Gravity.CENTER)

    private var isMan:Boolean = true
    @JvmField
    var hasFinishAni:Boolean = false
    init {

        val window = dialog.window
        val layoutParams = window.attributes

        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
        layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT

        window.attributes = layoutParams

        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

    }


    fun show(isMale:Boolean) {
        dialog.show()
        this.isMan = isMale
        if(isMan){
            dialog.rl_sex2.visibility = View.GONE
            dialog.rl_sex1.visibility = View.VISIBLE
            dialog.img_backgroud.setImageResource(R.drawable.select_boy)
            dialog.txt_title.text = activity.getText(R.string.select_boy)
            Observable.timer(250, TimeUnit.MICROSECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        setLeftToRihtAni(dialog.img_boy,2.0f,-25f)
                        setRightToLeftAni(dialog.img_girl,1.0f,0f)
                    }
        }else{
            dialog.rl_sex2.visibility = View.VISIBLE
            dialog.rl_sex1.visibility = View.GONE
            dialog.img_backgroud.setImageResource(R.drawable.select_girl)
            dialog.txt_title.text = activity.getText(R.string.select_girl)
            Observable.timer(250, TimeUnit.MICROSECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        setLeftToRihtAni(dialog.img_boy1,1.0f,0f)
                        setRightToLeftAni(dialog.img_girl1,2.0f,-25f)
                    }

        }


        AppLog.e("left",(dialog.rl_container.getWidth() / 2).toString() +""+ dialog.img_boy.getLeft() +""+ dialog.img_boy.width/2)
        AppLog.e("right",(dialog.rl_container.getWidth() / 2).toString()+""+ dialog.img_girl.right +""+ dialog.img_girl.width/2)
    }

    fun dismiss() {
        dialog.dismiss()
    }
    fun isShow(): Boolean{
        return dialog.isShowing
    }

    fun setLeftToRihtAni(aniView: View,scalSize:Float,translateSize:Float) {
        val mScaleAnimation = ScaleAnimation(1.0f, scalSize, 1.0f, scalSize// 整个屏幕就0.0到1.0的大小//缩放
        )

        var translate:Float
        if(isMan){
            translate = (dialog.rl_container.getWidth() / 2 - aniView.getLeft() - aniView.width).toFloat()
        }else{
            translate = (dialog.rl_container.getWidth() / 2 -  aniView.getLeft() - aniView.width/2 ).toFloat()
        }

        val mTranslateAnimation = TranslateAnimation(0f,translate , 0f, translateSize)// 移动

        val mAnimationSet = AnimationSet(false)
        mAnimationSet.addAnimation(mScaleAnimation)
        mAnimationSet.addAnimation(mTranslateAnimation)

        mAnimationSet.duration = 500
        mAnimationSet.fillAfter = true
        aniView.startAnimation(mAnimationSet)
    }

    fun setRightToLeftAni(aniView: View,scalSize:Float,translateSize:Float) {
        val mScaleAnimation = ScaleAnimation(1.0f, scalSize, 1.0f, scalSize// 整个屏幕就0.0到1.0的大小//缩放
        )

        var translate:Float
        if(isMan){
            translate = ((dialog.rl_container.getWidth() / 2 -  aniView.right + aniView.width/2)).toFloat()
        }else{
            translate = (dialog.rl_container.getWidth() / 2 -  aniView.right ).toFloat()
        }

        val mTranslateAnimation = TranslateAnimation(0f, translate, 0f, translateSize)// 移动
        val mAnimationSet = AnimationSet(false)
        mAnimationSet.addAnimation(mScaleAnimation)
        mAnimationSet.addAnimation(mTranslateAnimation)

        mAnimationSet.duration = 500
        mAnimationSet.fillAfter = true
        aniView.startAnimation(mAnimationSet)
        mAnimationSet.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationStart(animation: Animation) {

            }

            override fun onAnimationRepeat(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                hasFinishAni = true
            }
        })
    }



}