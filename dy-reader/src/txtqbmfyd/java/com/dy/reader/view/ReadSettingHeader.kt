package com.dy.reader.view

import android.content.Context
import android.support.annotation.DrawableRes
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import com.ding.basic.RequestRepositoryFactory
import com.dingyue.contract.util.showToastMessage
import com.dy.reader.R
import com.dy.reader.event.EventSetting
import com.dy.reader.presenter.ReadSettingPresenter
import com.dy.reader.setting.ReaderStatus
import kotlinx.android.synthetic.txtqbmfyd.reader_option_header.view.*
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.onEnd
import org.greenrobot.eventbus.EventBus

class ReadSettingHeader : FrameLayout{

    var presenter: ReadSettingPresenter? = null
    private var bookDownloadState: DownloadState = DownloadState.NOSTART
    var isOutAnimationRun = false

    fun showMenu(flag: Boolean) {
        if (flag) {
            this.visibility = View.VISIBLE
            isOutAnimationRun = false
            updateStatus()
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

        var isMarkPage = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).isBookMarkExist(ReaderStatus.book.book_id, ReaderStatus.position.group,ReaderStatus.position.offset)

        ibtn_reader_bookmark.isSelected = isMarkPage

        ibtn_reader_download.setOnClickListener {
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_download_btn)

            if (bookDownloadState == DownloadState.DOWNLOADING) {
                CacheManager.stop(ReaderStatus.book.book_id)
            } else {
                presenter?.cache()
            }

            EventBus.getDefault().post(EventSetting(EventSetting.Type.MENU_STATE_CHANGE, false))
        }

        ibtn_reader_bookmark.setOnClickListener { v ->
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_add_book_mark_btn)
            val result = presenter?.bookMark()
            val data = HashMap<String, String>()
            when (result) {
                1 -> {
                    v.context.applicationContext.showToastMessage("书签添加成功")
                    isMarkPage = true
                    ibtn_reader_bookmark.isSelected = true
                    data["type"] = "1"
                    ibtn_reader_bookmark.setImageResource(R.drawable.reader_option_bookmark_checked_icon)
                }
                2 -> {
                    v.context.applicationContext.showToastMessage("书签已删除")
                    isMarkPage = false
                    ibtn_reader_bookmark.isSelected = false
                    data["type"] = "2"
                    ibtn_reader_bookmark.setImageResource(R.drawable.reader_option_bookmark_check_icon)

                }
                else -> {
                    v.context.applicationContext.showToastMessage("书签添加失败")
                }
            }
            StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.LABELEDIT, data)
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

            readerHeaderMorePopup.startBookDetailListener = {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_read_head_bookinfo)
                presenter?.bookInfo()
                readerHeaderMorePopup.dismiss()
            }

            readerHeaderMorePopup.showAsDropDown(ibtn_reader_more)
        }

        // 初始化动画
        menuDownInAnimation = AnimationUtils.loadAnimation(context.applicationContext, R.anim.menu_push_down_in)
        menuUpOutAnimation = AnimationUtils.loadAnimation(context.applicationContext, R.anim.menu_push_up_out)

        this.visibility = View.GONE
    }

    fun setBookSource(source: String) {
        
    }

    fun setBookMarkImg(@DrawableRes id: Int) {
        ibtn_reader_bookmark?.setImageResource(id)
    }


    fun setBookDownLoadState(book_id: String?) {
        if (book_id == ReaderStatus.book.book_id) {
            bookDownloadState = CacheManager.getBookStatus(ReaderStatus.book)
            when (bookDownloadState) {
                DownloadState.NOSTART -> ibtn_reader_download.setImageResource(R.drawable.reader_option_download_icon)
                DownloadState.DOWNLOADING -> ibtn_reader_download.setImageResource(R.drawable.reader_option_downloading_icon)
                DownloadState.PAUSEED -> ibtn_reader_download.setImageResource(R.drawable.reader_option_download_pause_icon)
                DownloadState.FINISH -> ibtn_reader_download.setImageResource(R.drawable.reader_option_download_finish_icon)
                else -> {
                }
            }
        }
    }

    fun updateStatus() {
        val isMarkPage = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).isBookMarkExist(ReaderStatus.book.book_id, ReaderStatus.position.group,ReaderStatus.position.offset)

        if (ibtn_reader_bookmark != null && ibtn_reader_bookmark.visibility == View.VISIBLE) { 
            if (isMarkPage) {
                ibtn_reader_bookmark.setImageResource(R.drawable.reader_option_bookmark_checked_icon)
            } else {
                ibtn_reader_bookmark.setImageResource(R.drawable.reader_option_bookmark_check_icon)
            }
        }
    }
}