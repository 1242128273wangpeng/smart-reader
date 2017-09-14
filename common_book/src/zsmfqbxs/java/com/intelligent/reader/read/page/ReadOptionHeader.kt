package com.intelligent.reader.read.page

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.PopupWindow
import com.intelligent.reader.R
import com.intelligent.reader.presenter.read.ReadOption
import com.intelligent.reader.read.help.IReadDataFactory
import kotlinx.android.synthetic.zsmfqbxs.read_option_header.view.*
import kotlinx.android.synthetic.zsmfqbxs.read_option_pop.view.*
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.ReadStatus
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.onEnd
import net.lzbook.kit.utils.toastShort


/**
 * Created by xian on 2017/8/8.
 */
class ReadOptionHeader : FrameLayout, ReadOption.View {


    override var presenter: ReadOption.Presenter? = null

    override fun show(flag: Boolean) {
        if (flag) {
            if (this.visibility != View.VISIBLE) {
                this.visibility = View.VISIBLE
                this.startAnimation(menuDownInAnimation)
            }
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
            presenter?.cache()
        }
        header_ibtn_more?.setOnClickListener {

            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_read_head_more)

            val inflate = LayoutInflater.from(context).inflate(R.layout.read_option_pop, null)

            val popupWindow = PopupWindow(inflate, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            popupWindow.setBackgroundDrawable(ColorDrawable(0x00000000));
            popupWindow.isFocusable = true
            popupWindow.isTouchable = true
            popupWindow.isOutsideTouchable = false
            popupWindow.showAsDropDown(header_ibtn_more)

            if (isMarkPage) {
                inflate.read_option_pop_mark.text = "删除书签"
            }

            inflate.read_option_pop_change_source.setOnClickListener {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_change_source_btn)
                presenter?.changeSource()
                popupWindow.dismiss()
            }

            inflate.read_option_pop_mark.setOnClickListener { v ->

                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_add_book_mark_btn)
                val result = presenter?.bookMark()

                when (result) {
                    1 -> {
                        v.context.toastShort("书签添加成功", false)
                        isMarkPage = true
                        inflate.read_option_pop_mark.text = "删除书签"
                    }
                    2 -> {
                        v.context.toastShort("书签已删除", false)
                        isMarkPage = false
                        inflate.read_option_pop_mark.text = "添加书签"
                    }
                    else -> {
                        v.context.toastShort(R.string.add_mark_fail, false)
                    }
                }

                popupWindow.dismiss()
            }

            inflate.read_option_pop_info.setOnClickListener {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_read_head_bookinfo)
                presenter?.bookInfo()
                popupWindow.dismiss()
            }


        }

        // 初始化动画
        menuDownInAnimation = AnimationUtils.loadAnimation(context.getApplicationContext(), R.anim.menu_push_down_in)
        menuUpOutAnimation = AnimationUtils.loadAnimation(context.getApplicationContext(), R.anim.menu_push_up_out)

        this.visibility = View.GONE
    }

    override fun setBookSource(source: String) {
        novel_source_url?.text = source
    }

    override fun setBookMarkImg(id: Int) {
        novel_bookmark?.setImageResource(id)
    }

    override fun updateStatus(readStatus: ReadStatus, dataFactory: IReadDataFactory, bookDaoHelper: BookDaoHelper) {
        var typeChangeMark = 0
        if (bookDaoHelper != null && bookDaoHelper.isBookMarkExist(readStatus.book_id, readStatus.sequence,
                readStatus.offset, readStatus.book.book_type)) {
            isMarkPage = true
        } else {
            isMarkPage = false
        }


        if (novel_bookmark != null && novel_bookmark.visibility == View.VISIBLE) {
            if (isMarkPage) {
                /*novel_bookmark.setImageResource(R.drawable.read_bookmarked);*/
                typeChangeMark = R.mipmap.read_bookmarked
            } else {
                /*novel_bookmark.setImageDrawable(resources.getDrawable(ResourceUtil.getResourceId(this, Constants
                        .DRAWABLE, "_bookmark_selector")));*/
                typeChangeMark = R.mipmap.read_bookmark

            }
            novel_bookmark.setImageResource(typeChangeMark)
        }

        if (novel_name != null) {
            novel_name.text = readStatus.bookName
        }
        if (readStatus.sequence == -1) {
            novel_source_url.visibility = View.GONE
        } else {
            //显示原网站地址
            if (Constants.QG_SOURCE == readStatus.book.site) {
                novel_source_url.text = "青果阅读"
                novel_source_url.visibility = View.VISIBLE
            } else {
                if (dataFactory != null && dataFactory.currentChapter != null && !TextUtils.isEmpty(dataFactory.currentChapter.curl)) {
                    //if (readStatus.book.dex == 1 && !TextUtils.isEmpty(dataFactory.currentChapter.curl)) {
                    novel_source_url.text = UrlUtils.buildContentUrl(dataFactory.currentChapter.curl)
                    novel_source_url.visibility = View.VISIBLE
                    /*} else if (readStatus.book.dex == 0 && !TextUtils.isEmpty(dataFactory.currentChapter.curl1)) {
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