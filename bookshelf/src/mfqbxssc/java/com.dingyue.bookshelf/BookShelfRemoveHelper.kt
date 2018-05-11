package com.dingyue.bookshelf

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.support.v4.view.ViewPager
import android.view.*
import android.widget.*
import de.greenrobot.event.EventBus
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.event.ChangeHomeSelectEvent
import net.lzbook.kit.pulllist.SuperSwipeRefreshLayout
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.popup.PopupWindowInterface
import java.util.*

/**
 * BookShelfRemoveHelper
 */
class BookShelfRemoveHelper(private val mContext: Activity, protected var bookShelfReAdapter: BookShelfReAdapter?) : View.OnClickListener {
    var popupWindowDetail: PopupWindow? = null
    protected var popupWindowManager: PopupWindowInterface? = null
    protected var menuStateListener: OnMenuStateListener? = null
    protected var deleteClickListener: OnMenuDeleteClickListener? = null
    protected var selectAllListener: OnMenuSelectAllListener? = null
    protected var swipeRefreshLayout: SuperSwipeRefreshLayout? = null
    internal var TAG = "BookShelfRemoveHelper"
    internal var handler = Handler()
    private var popupWindow: PopupWindow? = null
    private var delete_btn: Button? = null
    private var selectAll_btn: Button? = null
    private var btnDetail: Button? = null
    private var showView: View? = null

    private var viewPager: ViewPager? = null
    private val adapter: BookDetailAdapter? = null
    private var tvNum: TextView? = null
    private var tvCurrentNum: TextView? = null
    private var tvTotalNum: TextView? = null


    private val mlist = ArrayList<Book>()

    //判定当前菜单状态是否为删除模式
    val isRemoveMode: Boolean
        get() = if (bookShelfReAdapter != null) {
            bookShelfReAdapter!!.isRemoveMode
        } else false

    val isAllChecked: Boolean
        get() = bookShelfReAdapter!!.checkedSize == bookShelfReAdapter!!.itemCount

    init {//显示指定菜单类别
        setRemoveWindow(mContext)

    }

    fun setLayout(layout: SuperSwipeRefreshLayout) {//指定使用菜单的ListView
        this.swipeRefreshLayout = layout
    }

    ////指定回调对象
    fun setOnMenuDeleteListener(click: OnMenuDeleteClickListener) {//指定删除回调对象
        deleteClickListener = click
    }

    fun setOnSelectAllListener(selectAllListener: OnMenuSelectAllListener) {
        this.selectAllListener = selectAllListener
    }

    fun setOnMenuStateListener(shownListener: OnMenuStateListener) {//指定菜单状态监听回调对象
        menuStateListener = shownListener
    }

    fun setCheckPosition(position: Int) {//点击选中position,已自动判定全选或非全选状态
        if (bookShelfReAdapter != null) {
            bookShelfReAdapter!!.setChecked(position)
            bookShelfReAdapter!!.notifyItemRangeChanged(position, 1)
            setDeleteNum()

            if (menuStateListener != null) {
                menuStateListener!!.getAllCheckedState(isAllChecked)
            }

        }
    }


    /**
     * use to dissmiss the remove window
     */
    fun dismissRemoveMenu(): Boolean {//显示关闭菜单
        if (popupWindow != null && popupWindow!!.isShowing) {
            onShowing(false)
            popupWindow!!.dismiss()
            return true
        }
        return false
    }


    /**
     * use to show the remove window
     *
     * @param parent the view which the menu belong with
     */
    fun showRemoveMenu(parent: View?) {//显示菜单
        showView = parent
        if (popupWindow != null && popupWindow!!.isShowing) {
            onShowing(false)
            popupWindow!!.dismiss()
        } else if (parent != null && popupWindow != null) {
            onShowing(true)
            popupWindow!!.showAtLocation(parent, Gravity.BOTTOM, 0, 0)
        }
    }

    fun showRemoveMenu2(parent: View?, position: Int) {//显示菜单
        showView = parent
        if (popupWindow != null && popupWindow!!.isShowing) {
            onShowing(false)
            popupWindow!!.dismiss()
        } else if (parent != null && popupWindow != null) {
            onShowing(true)
            setDeleteNum()
            popupWindow!!.showAtLocation(parent, Gravity.BOTTOM, 0, 0)
        }
    }

    fun selectAll(checkedAll: Boolean) {
        if (bookShelfReAdapter != null) {
            bookShelfReAdapter!!.setAllChecked(checkedAll)
            bookShelfReAdapter!!.notifyDataSetChanged()
            setDeleteNum()
        }
        if (selectAllListener != null) {
            selectAllListener!!.onSelectAll()
        }
    }

    override fun onClick(v: View?) {
        if (v == null) {
            return
        }
        val i1 = v.id
        if (i1 == R.id.btn_right) {
            if (deleteClickListener != null && bookShelfReAdapter != null
                    && bookShelfReAdapter?.remove_checked_states != null) {
                deleteClickListener?.onMenuDelete(bookShelfReAdapter!!.remove_checked_states!!)
            }

        } else if (i1 == R.id.btn_left) {
            selectAll(if (isAllChecked) false else true)

        } else if (i1 == R.id.btn_detail) {
            mlist.clear()

            if (bookShelfReAdapter!!.remove_checked_states != null && bookShelfReAdapter!!.remove_checked_states?.size != 0) {
                val list = ArrayList(bookShelfReAdapter!!.remove_checked_states)
                Collections.sort(list)
                for (i in list.indices) {
                    if (list[i] < bookShelfReAdapter?.book_list?.size ?: 0) {
                        mlist.add(bookShelfReAdapter!!.book_list!![list[i]])
                    }
                }
            }
            if (mlist.size > 0) {
                val sb = StringBuilder()
                for (i in mlist.indices) {
                    sb.append(if (i == mlist.size - 1)
                        mlist[i].book_id
                    else
                        mlist[i].book_id + "$")
                }
                val data = HashMap<String, String>()
                data["bookid"] = sb.toString()
                StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.SHELFEDIT_PAGE,
                        StartLogClickUtil.UPDATEDETAIL, data)

                showBookDetailPop(showView)
            } else {
                Toast.makeText(mContext, "请先选择书籍", Toast.LENGTH_SHORT).show()
            }

        }
    }


    fun showBookDetailPop(view: View?) {
        //自定义PopupWindow的布局
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.popwindow_book_detail, null)
        popupWindowDetail = PopupWindow(contentView)
        popupWindowDetail?.width = LinearLayout.LayoutParams.MATCH_PARENT
        popupWindowDetail?.height = LinearLayout.LayoutParams.WRAP_CONTENT
        popupWindowDetail?.isFocusable = true
        popupWindowDetail?.setBackgroundDrawable(ColorDrawable(0x60000000))   //为PopupWindow设置半透明背景.
        popupWindowDetail?.isOutsideTouchable = true
        popupWindowDetail?.setOnDismissListener {
            AppLog.e("kkk", "kkk")
            setBackgroundAlpha(1.0f)
        }


        viewPager = contentView.findViewById(R.id.viewPager) as ViewPager
        tvNum = contentView.findViewById(R.id.tv_num) as TextView
        tvCurrentNum = contentView.findViewById(R.id.current_num) as TextView
        tvTotalNum = contentView.findViewById(R.id.total_num) as TextView

        tvCurrentNum!!.text = "1"
        if (mlist.size != 0) {
            tvTotalNum!!.text = mlist.size.toString() + ""
        }

        val bookDetailAdapter = BookDetailAdapter(mContext)

        bookDetailAdapter.setBooks(mlist)
        viewPager!!.adapter = bookDetailAdapter
        viewPager!!.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                tvCurrentNum!!.text = (position + 1).toString() + ""
                tvTotalNum!!.text = mlist.size.toString() + ""
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })


        //设置PopupWindow进入和退出动画
        popupWindowDetail?.animationStyle = R.style.remove_menu_anim_style
        //设置PopupWindow显示的位置
        popupWindowDetail?.showAtLocation(view, Gravity.BOTTOM, 0, 0)
        setBackgroundAlpha(0.6f)
    }

    fun setBackgroundAlpha(bgAlpha: Float) {
        val lp = mContext.window
                .attributes
        lp.alpha = bgAlpha

        mContext.window.attributes = lp
    }

    private fun setRemoveWindow(context: Context) {//实例化菜单对象

        val baseView = LayoutInflater.from(context).inflate(R.layout.book_shelf_bottom, null)
        popupWindow = PopupWindow(baseView, WindowManager.LayoutParams.MATCH_PARENT, context.resources.getDimension(R.dimen.dimen_view_height_default).toInt())
        popupWindow!!.animationStyle = R.style.remove_menu_anim_style
        delete_btn = baseView.findViewById(R.id.btn_right) as Button
        selectAll_btn = baseView.findViewById(R.id.btn_left) as Button
        btnDetail = baseView.findViewById(R.id.btn_detail) as Button
        val layout = baseView.findViewById(R.id.remove_delete_layout) as LinearLayout

        layout.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_MENU) {
                onShowing(false)
                if (popupWindow != null) {
                    popupWindow!!.dismiss()
                }
                return@OnKeyListener true
            }
            false
        })

        layout.isFocusable = true
        layout.isFocusableInTouchMode = true
        layout.requestFocus()

        delete_btn!!.setOnClickListener(this)
        selectAll_btn!!.setOnClickListener(this)
        btnDetail!!.setOnClickListener(this)
    }

    fun onShowing(isShowing: Boolean) {//菜单打开及关闭状态
        if (bookShelfReAdapter != null) {
            bookShelfReAdapter!!.isRemoveMode = isShowing
            bookShelfReAdapter!!.resetRemovedState()
            setDeleteNum()
        }
        if (menuStateListener != null) {
            menuStateListener!!.getAllCheckedState(isAllChecked)
        }
        if (isShowing) {
            if (menuStateListener != null) {
                menuStateListener!!.getMenuShownState(isShowing)
            }
            if (bookShelfReAdapter != null) {

                handler.postDelayed({
                    menuStateListener!!.doHideAd()
                    bookShelfReAdapter!!.setListPadding(swipeRefreshLayout!!, isShowing)
                    bookShelfReAdapter!!.notifyDataSetChanged()
                }, DELAY_TIME)

            }
        } else {
            if (menuStateListener != null) {
                handler.postDelayed({ menuStateListener!!.getMenuShownState(isShowing) }, 300)
            }
            handler.post {
                if (bookShelfReAdapter != null) {
                    bookShelfReAdapter!!.isRemoveMode = isShowing
                    bookShelfReAdapter!!.setListPadding(swipeRefreshLayout!!, isShowing)
                    bookShelfReAdapter!!.notifyDataSetChanged()
                }
            }
        }
    }

    private fun setDeleteNum() {

        if (bookShelfReAdapter != null && delete_btn != null && selectAll_btn != null) {
            val num = bookShelfReAdapter!!.checkedSize
            EventBus.getDefault().post(ChangeHomeSelectEvent(isAllChecked))

            if (num == 0) {

                delete_btn!!.text = "删除"
                var textColor = 0
                val theme = mContext.theme
                textColor = Color.parseColor("#b3b3b3")
                delete_btn!!.setTextColor(textColor)
            } else {
                delete_btn!!.text = "删除 ($num)"
                var textColor = 0

                textColor = Color.parseColor("#000000")
                delete_btn!!.setTextColor(textColor)

            }
        }
    }

    // ====================================================
    // callback
    // ===================================================
    interface OnMenuStateListener {//菜单状态监听

        fun getMenuShownState(isShown: Boolean) //菜单开启状态

        fun getAllCheckedState(isAll: Boolean) //菜单全选状态

        fun doHideAd() //编辑状态删除广告数据

    }


    interface OnMenuSelectAllListener {
        fun onSelectAll()
    }

    interface OnMenuDeleteClickListener {//菜单删除按钮监听

        /**
         * delete button
         *
         * @param checked_state position which the list checkbox is selected
         */
        fun onMenuDelete(checked_state: HashSet<Int>) //删除按钮回调，得到被选中的position
    }

    companion object {

        private val DELAY_TIME: Long = 500
    }


}