package com.intelligent.reader.read.animation

import android.view.View
import com.intelligent.reader.view.ViewPager


/**
 * Created by Xian on 2018/1/5.
 */
class SlideTransformer : ViewPager.PageTransformer {

    override fun transformPage(view: View, position: Float) {
        view.translationX = 0f
    }

}