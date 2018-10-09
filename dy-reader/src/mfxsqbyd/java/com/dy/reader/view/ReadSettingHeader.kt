package com.dy.reader.view

import android.content.Context
import android.support.annotation.DrawableRes
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import com.ding.basic.database.helper.BookDataProviderHelper
import com.ding.basic.repository.RequestRepositoryFactory
import com.ding.basic.util.editShared
import com.ding.basic.util.getSharedBoolean
import com.dingyue.contract.util.CommonUtil
import com.dingyue.contract.util.SharedPreUtil
import com.dingyue.contract.util.showToastMessage
import com.dy.reader.R
import com.dy.reader.event.EventSetting
import com.dy.reader.presenter.ReadSettingPresenter
import com.dy.reader.setting.ReaderStatus
import kotlinx.android.synthetic.mfxsqbyd.reader_option_header.view.*
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.onEnd
import org.greenrobot.eventbus.EventBus

class ReadSettingHeader : FrameLayout {

    var presenter: ReadSettingPresenter? = null
    private var bookDownloadState: DownloadState = DownloadState.NOSTART
    var isOutAnimationRun = false

    fun showMenu(flag: Boolean) {
        if (flag) {
            this.visibility = View.VISIBLE
            isOutAnimationRun = false
            this.startAnimation(menuDownInAnimation)
        } else {
            if (this.visibility == View.VISIBLE && !isOutAnimationRun) {
                isOutAnimationRun = true
                menuUpOutAnimation.onEnd {
                    this.visibility = View.GONE
                    isOutAnimationRun = false
                }
                this.startAnimation(menuUpOutAnimation)
            }
        }

        EventBus.getDefault().post(EventSetting(EventSetting.Type.FULL_WINDOW_CHANGE, !flag))
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var menuDownInAnimation: Animation

    private var menuUpOutAnimation: Animation

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.reader_option_header, null)
        addView(view)

        img_reader_back.setOnClickListener {
            presenter?.back()
        }

        val hasPromptShowed = context.getSharedBoolean(SharedPreUtil.READER_SHARE_PROMPT)

        if (hasPromptShowed) {
            view_reader_share.visibility = View.INVISIBLE
        } else {
            view_reader_share.visibility = View.VISIBLE
        }

        ibtn_reader_share?.setOnClickListener {
            context.editShared {
                putBoolean(SharedPreUtil.READER_SHARE_PROMPT, true)
            }
            view_reader_share.visibility = View.INVISIBLE
            presenter?.showShareDialog()
        }

        if (RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(ReaderStatus.book.book_id) != null) {
            txt_add_bookshelf?.visibility = View.GONE
        } else {
            txt_add_bookshelf?.visibility = View.VISIBLE
        }

        if (ReaderStatus.book?.book_type == Constants.BOOK_LOCAL) {
            ibtn_reader_download.visibility = View.GONE
            ibtn_reader_more.visibility = View.GONE
        } else {
            ibtn_reader_download.visibility = View.VISIBLE
            ibtn_reader_more.visibility = View.VISIBLE
        }
        ibtn_reader_download.setOnClickListener {
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_download_btn)

            if (bookDownloadState == DownloadState.DOWNLOADING) {
                CommonUtil.showToastMessage("正在缓存中")
//                CacheManager.stop(ReaderStatus.book.book_id)
            } else {
                presenter?.cache()
            }

            EventBus.getDefault().post(EventSetting(EventSetting.Type.MENU_STATE_CHANGE, false))
        }

        txt_add_bookshelf?.setOnClickListener {
            presenter?.addBookShelf()
        }

        ibtn_reader_more?.setOnClickListener {

            presenter?.showMore()
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_read_head_more)


            val readerHeaderMorePopup = ReaderHeaderMorePopup(context)

            readerHeaderMorePopup.changeSourceListener = {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_change_source_btn)
                presenter?.changeSource()
                readerHeaderMorePopup.dismiss()

                EventBus.getDefault().post(EventSetting(EventSetting.Type.MENU_STATE_CHANGE, false))
            }

            readerHeaderMorePopup.startFeedbackListener = {
                presenter?.readFeedBack()
                readerHeaderMorePopup.dismiss()
                EventBus.getDefault().post(EventSetting(EventSetting.Type.MENU_STATE_CHANGE, false))
            }

            readerHeaderMorePopup.startBookMarkListener = {
                readerHeaderMorePopup.dismiss()
                val result = presenter?.bookMark()
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_add_book_mark_btn)
                val data = HashMap<String, String>()
                when (result) {
                    1 -> {
                        context.applicationContext.showToastMessage("书签添加成功")
                        data["type"] = "1"
                    }
                    2 -> {
                        context.applicationContext.showToastMessage("书签已删除")
                        data["type"] = "0"
                    }
                    else -> {
                        context.applicationContext.showToastMessage("书签添加失败")
                    }
                }
            }

            readerHeaderMorePopup.show(ibtn_reader_more)
        }

        // 初始化动画
        menuDownInAnimation = AnimationUtils.loadAnimation(context.applicationContext, R.anim.menu_push_down_in)
        menuUpOutAnimation = AnimationUtils.loadAnimation(context.applicationContext, R.anim.menu_push_up_out)

        this.visibility = View.GONE
    }


    fun setBookDownLoadState(book_id: String?) {
        if (book_id == ReaderStatus.book.book_id) {
            bookDownloadState = CacheManager.getBookStatus(ReaderStatus.book)
            when (bookDownloadState) {
                DownloadState.NOSTART -> ibtn_reader_download.setImageResource(R.drawable.reader_option_download_icon)
                DownloadState.DOWNLOADING -> ibtn_reader_download.setImageResource(R.drawable.reader_option_download_icon)
                DownloadState.PAUSEED -> ibtn_reader_download.setImageResource(R.drawable.reader_option_download_icon)
                DownloadState.FINISH -> ibtn_reader_download.setImageResource(R.drawable.reader_option_download_icon)
                else -> {
                }
            }
        }
    }

}