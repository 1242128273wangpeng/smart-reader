package com.intelligent.reader.read.page

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.annotation.DrawableRes
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.PopupWindow
import com.dingyue.contract.util.showToastMessage
import com.intelligent.reader.R
import com.intelligent.reader.read.mode.ReadState
import kotlinx.android.synthetic.txtqbmfyd.read_option_header.view.*
import kotlinx.android.synthetic.txtqbmfyd.read_option_pop.view.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.onEnd

/**
 * Created by xian on 2017/8/8.
 */
class ReadOptionHeader : FrameLayout {

    override var presenter: ReadOption.Presenter? = null

    private var mBookDownlLoadState: DownloadState = DownloadState.NOSTART

    override fun show(flag: Boolean) {
        if (flag) {
            this.visibility = View.VISIBLE
            this.startAnimation(menuDownInAnimation)
        } else {
            if (this.visibility == View.VISIBLE) {
                menuUpOutAnimation.onEnd {
                    this.visibility = View.GONE
                }
                this.startAnimation(menuUpOutAnimation)
            }
        }
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var menuDownInAnimation: Animation

    private var menuUpOutAnimation: Animation

    var isMarkPage = false

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.read_option_header, null)
        addView(view)

        novel_read_back.setOnClickListener {
            presenter?.back()
        }

        novel_source_url.setOnClickListener {
            presenter?.openWeb()
        }

        header_ibtn_download.setOnClickListener {
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_download_btn)
            if (mBookDownlLoadState == DownloadState.DOWNLOADING) {
                CacheManager.stop(ReadState.book.book_id)
            } else {
                presenter?.cache()
            }
            (context as ReadingActivity).showMenu(false)
        }

        header_ibtn_bookmark.isSelected = isMarkPage

        header_ibtn_bookmark?.setOnClickListener {
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_add_book_mark_btn)
            val result = presenter?.bookMark()
            val data = HashMap<String, String>()
            data.put("bookid", ReadState.book_id)
            data.put("chapterid", ReadState.chapterId ?: "")
            when (result) {
                1 -> {
                    context.showToastMessage("书签添加成功！")
                    isMarkPage = true
                    header_ibtn_bookmark.isSelected = true
                    data.put("type", "1")
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.LABELEDIT, data)
                }
                2 -> {
                    context.showToastMessage("书签已删除！")
                    isMarkPage = false
                    header_ibtn_bookmark.isSelected = false
                    data.put("type", "2")
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.LABELEDIT, data)
                }
                else -> {
                    context.showToastMessage(R.string.add_mark_fail)
                }
            }
        }

        header_ibtn_more?.setOnClickListener {

            presenter?.showMore();
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_read_head_more)

            val inflate = LayoutInflater.from(context).inflate(R.layout.read_option_pop, null)

            val popupWindow = PopupWindow(inflate, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            popupWindow.setBackgroundDrawable(ColorDrawable(0x00000000));
            popupWindow.isFocusable = true
            popupWindow.isTouchable = true
            popupWindow.isOutsideTouchable = false
            popupWindow.showAsDropDown(header_ibtn_more)

            inflate.read_option_pop_change_source.setOnClickListener {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_change_source_btn)
                presenter?.changeSource()
                (context as ReadingActivity).showMenu(false)
                popupWindow.dismiss()
            }

            inflate.read_option_pop_info.setOnClickListener {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_read_head_bookinfo)
                presenter?.bookInfo()
                popupWindow.dismiss()
            }

            inflate.read_option_pop_feedback.setOnClickListener {
                presenter?.feedback()
                (context as ReadingActivity).showMenu(false)
                popupWindow.dismiss()
            }
        }

        // 初始化动画
        menuDownInAnimation = AnimationUtils.loadAnimation(context.applicationContext, R.anim.menu_push_down_in)
        menuUpOutAnimation = AnimationUtils.loadAnimation(context.applicationContext, R.anim.menu_push_up_out)

        this.visibility = View.GONE
    }

    fun setBookDownLoadState(book_id: String?) {
        if (book_id == ReadState.book.book_id) {
            val downlLoadState = CacheManager.getBookStatus(ReadState.book)
            mBookDownlLoadState = downlLoadState
            Log.d("Cover Page", "getBookDownLoadState downlLoadState: " + downlLoadState)
            when (downlLoadState) {
                DownloadState.NOSTART -> header_ibtn_download.setImageResource(R.drawable.icon_read_option_down_normal)
                DownloadState.DOWNLOADING -> header_ibtn_download.setImageResource(R.drawable.icon_read_option_down_running)
                DownloadState.PAUSEED -> header_ibtn_download.setImageResource(R.drawable.icon_read_option_down_pause)
                DownloadState.FINISH -> header_ibtn_download.setImageResource(R.drawable.icon_read_option_down_finish)
                else -> {
                }
            }
        }
    }


    override fun setBookSource(source: String) {
        novel_source_url?.text = source
    }

    override fun setBookMarkImg(@DrawableRes id: Int) {
        novel_bookmark?.setImageResource(id)
    }

    override fun updateStatus(bookDaoHelper: BookDaoHelper) {
        var typeChangeMark: Int
        isMarkPage = bookDaoHelper.isBookMarkExist(ReadState.book_id, ReadState.sequence, ReadState.offset, ReadState.book.book_type)

//        if (novel_bookmark != null && novel_bookmark.visibility == View.VISIBLE) {
//            typeChangeMark = if (isMarkPage) {
//                R.mipmap.read_bookmarked
//            } else {
//                R.mipmap.read_bookmark
//            }
//            novel_bookmark.setImageResource(typeChangeMark)
//        }

        header_ibtn_bookmark.isSelected = isMarkPage

        if (novel_name != null) {
            novel_name.text = ReadState.book.name
        }
        if (ReadState.sequence == -1) {
            novel_source_url.visibility = View.GONE
        } else {
            //显示原网站地址
            if (Constants.QG_SOURCE == ReadState.book.site) {
                novel_source_url.text = "青果阅读"
            } else {
                if (ReadState.currentChapter != null && !TextUtils.isEmpty(ReadState.currentChapter!!.curl)) {
                    novel_source_url.text = UrlUtils.buildContentUrl(ReadState.currentChapter!!.curl)
                } else {
                    novel_source_url.visibility = View.GONE
                }
            }
        }
    }

    fun dismissLoadingPage() {
        presenter?.dismissLoadingPage()
    }
}