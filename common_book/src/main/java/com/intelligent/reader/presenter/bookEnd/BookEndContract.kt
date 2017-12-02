package com.intelligent.reader.presenter.bookEnd

import android.graphics.Bitmap
import com.android.volley.toolbox.ImageLoader
import net.lzbook.kit.data.bean.Source
import java.util.ArrayList

/**
 * Created by zhenXiang on 2017\11\21 0021.
 */

interface BookEndContract {

    fun showSource(hasSource: Boolean, sourceList: ArrayList<Source>) //展示多个来源
    fun showAdViewLogo(rationName: String)//显示广告的Logo
    fun showAdImgSuccess(bitmap: Bitmap)//广告图片加载成功
    fun showAdImgError()//广告图片加载失败

}
