package com.dy.reader.view

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout

import com.ding.basic.RequestRepositoryFactory

import com.dy.reader.R
import com.dy.reader.event.EventSetting
import com.dy.reader.presenter.ReadSettingPresenter
import com.dy.reader.setting.ReaderStatus
import kotlinx.android.synthetic.zsmfqbxs.reader_option_header.view.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.download.CacheManager
import net.lzbook.kit.utils.download.DownloadState
import net.lzbook.kit.utils.onEnd
import net.lzbook.kit.utils.toast.ToastUtil
import org.greenrobot.eventbus.EventBus

class ReaderSettingHeader : FrameLayout {

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

        txt_reader_source.setOnClickListener {
            presenter?.openWeb()
        }

        ibtn_reader_download.setOnClickListener {
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_download_btn)

            if (bookDownloadState == DownloadState.DOWNLOADING) {
                CacheManager.stop(ReaderStatus.book.book_id)
            } else {
                presenter?.cache()
            }

            EventBus.getDefault().post(EventSetting(EventSetting.Type.MENU_STATE_CHANGE, false))
        }

        ibtn_reader_more?.setOnClickListener {

            presenter?.showMore()
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_read_head_more)

            val readerHeaderMorePopup = ReaderHeaderMorePopup(context)

            val isMarkPage = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).isBookMarkExist(ReaderStatus.book.book_id, ReaderStatus.position.group, ReaderStatus.position.offset)

            if (isMarkPage) {
                readerHeaderMorePopup.insertBookmarkContent("删除书签")
            } else {
                readerHeaderMorePopup.insertBookmarkContent("添加书签")
            }

            readerHeaderMorePopup.changeSourceListener = {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_change_source_btn)
                presenter?.changeSource()
                readerHeaderMorePopup.dismiss()

                EventBus.getDefault().post(EventSetting(EventSetting.Type.MENU_STATE_CHANGE, false))
            }

            readerHeaderMorePopup.handleBookmarkListener = {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_add_book_mark_btn)
                val result = presenter?.bookMark()
                val data = HashMap<String, String>()

                when (result) {
                    1 -> {
                        ToastUtil.showToastMessage("书签添加成功")
                        readerHeaderMorePopup.insertBookmarkContent("删除书签")
                        data["type"] = "1"
                    }
                    2 -> {
                        ToastUtil.showToastMessage("书签已删除")
                        readerHeaderMorePopup.insertBookmarkContent("添加书签")
                        data["type"] = "2"

                    }
                    else -> {
                        ToastUtil.showToastMessage("书签添加失败")
                    }
                }

                readerHeaderMorePopup.dismiss()
                StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.LABELEDIT, data)
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

    private fun updateStatus() {
        if (ReaderStatus.position.group == -1) {
            txt_reader_source.visibility = View.GONE
        } else {
            if (ReaderStatus.book.fromQingoo()) {
                txt_reader_source.text = "青果阅读"
                txt_reader_source.visibility = View.VISIBLE
            } else {
                if (ReaderStatus.currentChapter != null && !TextUtils.isEmpty(ReaderStatus.currentChapter!!.url)) {
                    txt_reader_source.text = ReaderStatus.currentChapter!!.url
                    txt_reader_source.visibility = View.VISIBLE
                } else {
                    txt_reader_source.visibility = View.GONE
                }
            }
        }
    }
}