package com.dy.reader.view

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.annotation.DrawableRes
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.PopupWindow
import com.ding.basic.database.helper.BookDataProviderHelper
import com.dingyue.contract.util.showToastMessage
import com.dy.reader.R
import com.dy.reader.event.EventSetting
import com.dy.reader.presenter.ReadSettingPresenter
import com.dy.reader.setting.ReaderStatus
import kotlinx.android.synthetic.main.read_option_pop.view.*
import kotlinx.android.synthetic.qbzsydq.read_option_header.view.*
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.onEnd
import org.greenrobot.eventbus.EventBus


class ReadSettingHeader : FrameLayout{


    var presenter: ReadSettingPresenter? = null

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
            presenter?.cache()
        }
        header_ibtn_more?.setOnClickListener {

            presenter?.showMore()
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_read_head_more)

            val inflate = LayoutInflater.from(context).inflate(R.layout.read_option_pop, null)

            val popupWindow = PopupWindow(inflate, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            popupWindow.setBackgroundDrawable(ColorDrawable(0x00000000));
            popupWindow.isFocusable = true
            popupWindow.isTouchable = true
            popupWindow.isOutsideTouchable = false
            popupWindow.showAsDropDown(header_ibtn_more)

            val isMarkPage = BookDataProviderHelper.loadBookDataProviderHelper(BaseBookApplication.getGlobalContext()).isBookMarkExist(ReaderStatus.book.book_id, ReaderStatus.position.group,ReaderStatus.position.offset)

            if (isMarkPage) {
                inflate.read_option_pop_mark.text = "删除书签"
            }

            inflate.read_option_pop_change_source.setOnClickListener {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_change_source_btn)
                presenter?.changeSource()
                popupWindow.dismiss()

                EventBus.getDefault().post(EventSetting(EventSetting.Type.MENU_STATE_CHANGE, false))
            }

            inflate.read_option_pop_mark.setOnClickListener { v ->

                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_add_book_mark_btn)
                val result = presenter?.bookMark()
                val data = HashMap<String, String>()

                when (result) {
                    1 -> {
                        v.context.applicationContext.showToastMessage("书签添加成功")
                        inflate.read_option_pop_mark.text = "删除书签"
                        data.put("type", "1")
                    }
                    2 -> {
                        v.context.applicationContext.showToastMessage("书签已删除")
                        inflate.read_option_pop_mark.text = "添加书签"
                        data.put("type", "2")

                    }
                    else -> {
                        v.context.applicationContext.showToastMessage("书签添加失败")
                    }
                }

                popupWindow.dismiss()
                StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.LABELEDIT, data)
            }

            inflate.read_option_pop_info.setOnClickListener {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_read_head_bookinfo)
                presenter?.bookInfo()
                popupWindow.dismiss()
            }


        }

        // 初始化动画
        menuDownInAnimation = AnimationUtils.loadAnimation(context.applicationContext, R.anim.menu_push_down_in)
        menuUpOutAnimation = AnimationUtils.loadAnimation(context.applicationContext, R.anim.menu_push_up_out)

        this.visibility = View.GONE
    }

    fun setBookSource(source: String) {
        novel_source_url?.text = source
    }

    fun setBookMarkImg(@DrawableRes id: Int) {
        novel_bookmark?.setImageResource(id)
    }

    fun updateStatus() {
        var typeChangeMark:Int
        val isMarkPage = BookDataProviderHelper.loadBookDataProviderHelper(BaseBookApplication.getGlobalContext()).isBookMarkExist(ReaderStatus.book.book_id, ReaderStatus.position.group,ReaderStatus.position.offset)

        if (novel_bookmark != null && novel_bookmark.visibility == View.VISIBLE) {
            typeChangeMark = if (isMarkPage) {
                /*novel_bookmark.setImageResource(R.drawable.read_bookmarked);*/
                R.mipmap.read_bookmarked
            } else {
                /*novel_bookmark.setImageDrawable(resources.getDrawable(ResourceUtil.getResourceId(this, Constants
                            .DRAWABLE, "_bookmark_selector")));*/
                R.mipmap.read_bookmark
            }
            novel_bookmark.setImageResource(typeChangeMark)
        }

        if (novel_name != null) {
            novel_name.text = ReaderStatus.book.name
        }
        if (ReaderStatus.position.group == -1) {
            novel_source_url.visibility = View.GONE
        } else {
            //显示原网站地址
            if (Constants.QG_SOURCE == ReaderStatus.book.host) {
                novel_source_url.text = "青果阅读"
                novel_source_url.visibility = View.VISIBLE
            } else {
                if (ReaderStatus.currentChapter != null && !TextUtils.isEmpty(ReaderStatus.currentChapter!!.url)) {
                    //if (ReadState.book.dex == 1 && !TextUtils.isEmpty(dataFactory.currentChapter.curl)) {
                    novel_source_url.text = ReaderStatus.currentChapter!!.url
                    novel_source_url.visibility = View.VISIBLE
                    /*} else if (ReadState.book.dex == 0 && !TextUtils.isEmpty(dataFactory.currentChapter.curl1)) {
                        novel_source_url.setText("来源于：" + dataFactory.currentChapter.curl1);
                        novel_source_url.setVisibility(View.VISIBLE);*/
                } else {
                    novel_source_url.visibility = View.GONE
                }
                //}
            }
        }
    }
}