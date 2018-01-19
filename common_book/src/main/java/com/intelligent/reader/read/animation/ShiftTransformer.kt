package com.intelligent.reader.read.animation

import android.util.Log
import android.view.View
import com.intelligent.reader.view.ViewPager


/**
 * Created by Xian on 2018/1/5.
 */
class ShiftTransformer : ViewPager.PageTransformer {

    override fun transformPage(view: View, position: Float) {
        val pageWidth = view.width

        if (position <= -1) {
            view.translationX = 0f
        } else if (position <= 0) {
            view.translationX = 0f
        } else if (position <= 1) {
            view.translationX = pageWidth * -position
        }else{
            view.translationX = 0f
        }
    }
}