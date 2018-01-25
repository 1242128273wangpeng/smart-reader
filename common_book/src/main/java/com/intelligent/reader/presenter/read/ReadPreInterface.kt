package com.intelligent.reader.presenter.read

import com.intelligent.reader.read.page.PageInterface
import com.intelligent.reader.reader.ReaderViewModel

/**
 * Created by yuchao on 2017/11/14 0014.
 */
interface ReadPreInterface {


    interface View {
        fun initView(fac: ReaderViewModel)
        fun initPresenter(optionPresenter: ReadOptionPresenter?, markPresenter: CatalogMarkPresenter?)
        fun showSetMenu(isShow: Boolean)
        // 全屏切换
        fun full(isFull: Boolean)
        fun initSettingGuide()
        fun setMode()
        fun showAutoMenu(isShow: Boolean)
        fun resetPageView(pageView: PageInterface)
        fun initShowCacheState()
        fun changeChapter()
        fun checkModeChange()
        fun getAutoMenuShowState(): Boolean
        fun showStopAutoHint()
        fun initCatlogView()
        fun onNewInitView(): Boolean
        fun readOptionHeaderDismiss()
    }
}