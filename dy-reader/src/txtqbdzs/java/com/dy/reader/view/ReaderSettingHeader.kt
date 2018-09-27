package com.dy.reader.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout

import com.dy.reader.R
import com.dy.reader.event.EventSetting
import com.dy.reader.presenter.ReadSettingPresenter
import com.dy.reader.setting.ReaderStatus
import kotlinx.android.synthetic.txtqbdzs.reader_option_header.view.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil

import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.download.CacheManager
import net.lzbook.kit.utils.download.DownloadState
import net.lzbook.kit.utils.onEnd
import net.lzbook.kit.utils.sp.SPKey
import net.lzbook.kit.utils.sp.SPUtils
import net.lzbook.kit.utils.toast.ToastUtil
import org.greenrobot.eventbus.EventBus

class ReaderSettingHeader : FrameLayout {

    var presenter: ReadSettingPresenter? = null

    private var bookDownloadState: DownloadState = DownloadState.NOSTART

    var isOutAnimationRun = false

    private val guideSharedKey = AppUtils.getVersionCode().toString() + SPKey.READING_SETING_GUIDE_TAG

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
                // 关闭更多对话框
                readerHeaderMorePopup.dismiss()
            }
            if (view_guide.visibility == View.VISIBLE) {
                hideGuide()
            }
        }

        EventBus.getDefault().post(EventSetting(EventSetting.Type.FULL_WINDOW_CHANGE, !flag))

        if (!SPUtils.getDefaultSharedBoolean(guideSharedKey)) {
            view_guide.visibility = View.VISIBLE
            txt_reader_guide_option.visibility = View.VISIBLE

            view_guide.setOnClickListener {
                hideGuide()
            }
        }
    }

    private fun hideGuide() {
        view_guide.visibility = View.GONE
        txt_reader_guide_option.visibility = View.GONE
        SPUtils.putDefaultSharedBoolean(guideSharedKey, true)
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var menuDownInAnimation: Animation

    private var menuUpOutAnimation: Animation

    private val readerHeaderMorePopup: ReaderHeaderMorePopup by lazy {
        val popup = ReaderHeaderMorePopup(context)

        popup.changeSourceListener = {
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_change_source_btn)
            presenter?.changeSource()
            popup.dismiss()

            EventBus.getDefault().post(EventSetting(EventSetting.Type.MENU_STATE_CHANGE, false))
        }

        popup.handleBookmarkListener = {
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_add_book_mark_btn)
            val result = presenter?.bookMark()
            val data = HashMap<String, String>()

            when (result) {
                1 -> {
                    ToastUtil.showToastMessage("书签添加成功")
                    popup.insertBookmarkContent(false)
                    data["type"] = "1"
                }
                2 -> {
                    ToastUtil.showToastMessage("书签已删除")
                    popup.insertBookmarkContent(true)
                    data["type"] = "2"

                }
                else -> {
                    ToastUtil.showToastMessage("书签添加失败")
                }
            }

            popup.dismiss()
            StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.LABELEDIT, data)
        }

        popup.startBookDetailListener = {
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_read_head_bookinfo)
            presenter?.bookInfo()
            popup.dismiss()
        }

        popup.feedbackListener = {
            presenter?.readFeedBack()
            popup.dismiss()
        }
        popup
    }

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.reader_option_header, null)
        addView(view)

        img_reader_back.setOnClickListener {
            presenter?.back()
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

            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_read_head_more)

            readerHeaderMorePopup.show(ibtn_reader_more)
        }

        // 初始化动画
        menuDownInAnimation = AnimationUtils.loadAnimation(context.applicationContext, R.anim.menu_push_down_in)
        menuUpOutAnimation = AnimationUtils.loadAnimation(context.applicationContext, R.anim.menu_push_up_out)

        this.visibility = View.GONE
    }
}