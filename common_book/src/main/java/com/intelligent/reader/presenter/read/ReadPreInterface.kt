package com.intelligent.reader.presenter.read

import com.intelligent.reader.read.page.PageInterface
import com.intelligent.reader.reader.ReaderViewModel
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.NovelLineBean
import net.lzbook.kit.data.bean.ReadStatus
import java.util.ArrayList

/**
 * Created by yuchao on 2017/11/14 0014.
 */
interface ReadPreInterface {


    interface View {
        fun initView(fac: ReaderViewModel)
        fun initPresenter(optionPresenter: ReadOptionPresenter?, markPresenter: CatalogMarkPresenter?)
        fun setReadStatus(readStatus: ReadStatus)
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
        fun loadChapterSuccess(what: Int,chapter:Chapter,chapterList: ArrayList<ArrayList<NovelLineBean>>)
        //刷新时间
        fun freshTime(time_text: CharSequence?)
        //刷新电池
        fun setBackground()
    }
}