package com.dy.reader.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import com.ding.basic.repository.RequestRepositoryFactory
import com.dy.reader.R
import com.dy.reader.event.EventSetting
import com.dy.reader.presenter.ReadSettingPresenter
import com.dy.reader.setting.ReaderStatus
import kotlinx.android.synthetic.qbmfrmxs.reader_option_header.view.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.base.BaseBookApplication
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.download.CacheManager
import net.lzbook.kit.utils.download.DownloadState
import net.lzbook.kit.utils.onEnd
import net.lzbook.kit.utils.toast.showToastMessage
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
            isBookSubscribed()
        } else {
            if (this.visibility == View.VISIBLE && !isOutAnimationRun) {
                readerHeaderMorePopup.dismiss()
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

    private val requestFactory: RequestRepositoryFactory by lazy {
        RequestRepositoryFactory
                .loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
    }

    private val readerHeaderMorePopup: ReaderHeaderMorePopup by lazy {
        val popup = ReaderHeaderMorePopup(context)

        popup.changeSourceListener = {
            StatServiceUtils.statAppBtnClick(context,
                    StatServiceUtils.rb_click_change_source_btn)
            presenter?.changeSource()

            EventBus.getDefault().post(EventSetting(EventSetting.Type.MENU_STATE_CHANGE, false))
        }

        popup.addBookMarkListener = {
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_add_book_mark_btn)
            val result = presenter?.bookMark()
            val data = HashMap<String, String>()
            when (result) {
                1 -> {
                    context.applicationContext.showToastMessage("书签添加成功")
                    data["type"] = "1"
                }
                2 -> {
                    context.applicationContext.showToastMessage("书签已删除")
                    data["type"] = "2"
                }
                else -> {
                    context.applicationContext.showToastMessage("书签添加失败")
                }
            }
            StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGE_PAGE,
                    StartLogClickUtil.LABELEDIT, data)

        }

        popup.feedbackListener = {
            presenter?.readFeedBack()
            EventBus.getDefault().post(EventSetting(EventSetting.Type.MENU_STATE_CHANGE, false))
        }

        popup.bookDetailListener = {
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_read_head_bookinfo)
            presenter?.bookInfo()
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


        ibtn_reader_more.setOnClickListener {
            presenter?.showMore()
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_read_head_more)
            readerHeaderMorePopup.show(ibtn_reader_more)
        }

        ll_add_bookshelf.setOnClickListener {
            val result = requestFactory.insertBook(ReaderStatus.book)
            if (result > 0) {
                ll_add_bookshelf.visibility = View.GONE
                context.showToastMessage(R.string.add_bookshelf_success)
            }
            val data = HashMap<String, String>()
            data["bookid"] = ReaderStatus.book.book_id
            data["chapterid"] = ReaderStatus.book.book_chapter_id
            StartLogClickUtil.upLoadEventLog(context.applicationContext,
                    StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.SHELFADD, data)
        }

        // 初始化动画
        menuDownInAnimation = AnimationUtils.loadAnimation(context.applicationContext, R.anim.menu_push_down_in)
        menuUpOutAnimation = AnimationUtils.loadAnimation(context.applicationContext, R.anim.menu_push_up_out)

        this.visibility = View.GONE
    }

//    fun setBookDownLoadState(book_id: String?) {
//        if (book_id == ReaderStatus.book.book_id) {
//            bookDownloadState = CacheManager.getBookStatus(ReaderStatus.book)
//            when (bookDownloadState) {
//                DownloadState.NOSTART -> ibtn_reader_download.setImageResource(R.drawable.reader_option_download_icon)
//                DownloadState.DOWNLOADING -> ibtn_reader_download.setImageResource(R.drawable.reader_option_downloading_icon)
//                DownloadState.PAUSEED -> ibtn_reader_download.setImageResource(R.drawable.reader_option_download_pause_icon)
//                DownloadState.FINISH -> ibtn_reader_download.setImageResource(R.drawable.reader_option_download_finish_icon)
//                else -> {
//                }
//            }
//        }
//    }

    fun isBookSubscribed() {
        val subscribedBook = requestFactory.checkBookSubscribe(ReaderStatus.book.book_id)
        if (subscribedBook != null) {
            ll_add_bookshelf.visibility = View.GONE
        } else {
            ll_add_bookshelf.visibility = View.VISIBLE
        }
    }
}