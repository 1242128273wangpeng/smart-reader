package com.intelligent.reader.widget.drawer

import android.content.Context
import android.graphics.Canvas
import android.support.v4.view.ViewCompat
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.intelligent.reader.widget.drawer.DrawerLayout.MenuState.MENU_CLOSED
import com.intelligent.reader.widget.drawer.DrawerLayout.MenuState.MENU_OPENED


/**
 * Desc 抽屉菜单布局
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/2/24
 */

class DrawerLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {
    private val screenWidth: Int
    private val screenHeight: Int

    private var menuView: View? = null
    private var mainView: DrawerMain? = null
    private val shadowView: ShadowView

    private val viewDragHelper: ViewDragHelper
    private var menuState: Int = MENU_CLOSED

    private var dragOrientation: Int = 0
    private val springBackDistance: Int
    private val menuWidth: Int
    private val shadowWidth: Int

    private var mainLeft: Int = 0

    private var isLock = false

    val isOpened: Boolean
        get() = menuState == MENU_OPENED

    private var onMenuStateChangeListener: ((menuState: Int) -> Unit)? = null

    init {

        val density = resources.displayMetrics.density//屏幕密度

        screenWidth = resources.displayMetrics.widthPixels
        screenHeight = resources.displayMetrics.heightPixels

        springBackDistance = (SPRING_BACK_DISTANCE * density + 0.5f).toInt()

        mainLeft = 0

        menuWidth = screenWidth - (MENU_MARGIN_RIGHT * density + 0.5f).toInt()

        shadowView = ShadowView(context)
        shadowWidth = (SHADOW_WIDTH * density + 0.5f).toInt()

        viewDragHelper = ViewDragHelper.create(this, TOUCH_SLOP_SENSITIVITY, CoordinatorCallback())
        viewDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT)
    }

    private inner class CoordinatorCallback : ViewDragHelper.Callback() {

        override fun onEdgeDragStarted(edgeFlags: Int, pointerId: Int) {
            viewDragHelper.captureChildView(mainView ?: return, pointerId)
        }

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return if (menuState == MENU_OPENED) {
                menuView === child || mainView === child
            } else {
                menuView === child
            }
        }

        override fun onViewCaptured(capturedChild: View, activePointerId: Int) {
            if (capturedChild === menuView) {
                viewDragHelper.captureChildView(mainView ?: return, activePointerId)
            }
        }

        override fun getViewHorizontalDragRange(child: View): Int {
            return measuredWidth - child.measuredWidth
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            var l = left
            if (l < 0) {
                l = 0
            } else if (l > menuWidth) {
                l = menuWidth
            }
            return if (isLock) {
                0
            } else {
                l
            }
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)
            if (dragOrientation == LEFT_TO_RIGHT) {
                if (xvel > SPRING_BACK_VELOCITY || mainView?.left ?: 0 > springBackDistance) {
                    openMenu()
                } else {
                    closeMenu()
                }
            } else if (dragOrientation == RIGHT_TO_LEFT) {
                if (xvel < -SPRING_BACK_VELOCITY || mainView?.left ?: 0 < menuWidth - springBackDistance) {
                    closeMenu()
                } else {
                    openMenu()
                }
            }

        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            mainLeft = left

            moveView(screenHeight)

            if (dx > 0) {
                dragOrientation = LEFT_TO_RIGHT
            } else if (dx < 0) {
                dragOrientation = RIGHT_TO_LEFT
            }
        }
    }

    //加载完布局文件后调用
    override fun onFinishInflate() {
        super.onFinishInflate()
        menuView = getChildAt(0)
        val menuParams = menuView?.layoutParams as ViewGroup.MarginLayoutParams
        menuParams.width = menuWidth
        menuView?.layoutParams = menuParams

        mainView = getChildAt(1) as DrawerMain
        mainView?.setParent(this)

        val shadowParams = FrameLayout.LayoutParams(shadowWidth, FrameLayout.LayoutParams.MATCH_PARENT)
        addView(shadowView, shadowParams)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return viewDragHelper.shouldInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        //将触摸事件传递给ViewDragHelper，此操作必不可少
        viewDragHelper.processTouchEvent(event)
        return true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        moveView(bottom)
    }

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        val restoreCount = canvas.save()//保存画布当前的剪裁信息

        val clipLeft = 0
        val clipRight = mainView?.left ?: 0
        if (child === menuView && clipRight == 0 && menuState == MENU_CLOSED) {
            canvas.clipRect(clipLeft, 0, clipRight, height)//剪裁显示的区域
        }

        val result = super.drawChild(canvas, child, drawingTime)//绘制当前view

        //恢复画布之前保存的剪裁信息
        //以正常绘制之后的view
        canvas.restoreToCount(restoreCount)

        return result
    }

    override fun computeScroll() {
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
        if (mainView?.left == 0 && menuState != MENU_CLOSED) {
            menuState = MENU_CLOSED
            onMenuStateChangeListener?.invoke(MENU_CLOSED)
        } else if (mainView?.left == menuWidth && menuState != MENU_OPENED) {
            menuState = MENU_OPENED
            onMenuStateChangeListener?.invoke(MENU_OPENED)
        }
    }

    fun openMenu() {
        viewDragHelper.smoothSlideViewTo(mainView ?: return, menuWidth, 0)
        ViewCompat.postInvalidateOnAnimation(this@DrawerLayout)
    }

    fun closeMenu() {
        viewDragHelper.smoothSlideViewTo(mainView ?: return, 0, 0)
        ViewCompat.postInvalidateOnAnimation(this@DrawerLayout)
    }

    fun setOnMenuStateChangeListener(listener: (state: Int) -> Unit) {
        onMenuStateChangeListener = listener
    }

    fun lock() {
        isLock = true
    }

    fun unlock() {
        isLock = false
    }

    private fun moveView(bottom: Int) {
        mainView?.layout(mainLeft, 0, mainLeft + screenWidth, bottom)

        val shadowLeft = mainLeft - shadowWidth
        shadowView.layout(shadowLeft, 0, mainLeft, bottom)
    }

    companion object {

        private val LEFT_TO_RIGHT = 3
        private val RIGHT_TO_LEFT = 4

        private val SPRING_BACK_VELOCITY = 1500f
        private val SPRING_BACK_DISTANCE = 80

        private val MENU_MARGIN_RIGHT = 70

        private val SHADOW_WIDTH = 20

        private val TOUCH_SLOP_SENSITIVITY = 1f
    }

    object MenuState {
        val MENU_CLOSED = 1
        val MENU_OPENED = 2
    }

}