package com.dy.reader.page

import android.annotation.SuppressLint
import com.dy.reader.data.DataProvider
import com.dy.reader.helper.AppHelper
import com.dy.reader.setting.ReaderStatus
import com.intelligent.reader.reader.v2.GLPage

@SuppressLint("StaticFieldLeak")
/**
 * Created by xian on 18-3-22.
 */
object PageManager : GLPage.RefreshListener, DataProvider.GroupRefreshListener {
    override fun onFontRefreshed(position: Position, callback: (() -> Unit)?) {
        currentPage.position = DataProvider.queryPosition(position.book_id, position.group
                , position.offset)
        leftPage.position = currentPage.position.previous()
        rightPage.position = currentPage.position.next()

        currentPage.refresh{
            rightPage.refresh{
                leftPage.refresh{
                    callback?.invoke()
                }
            }
        }

        ReaderStatus.position = currentPage.position
    }

    override fun onPageRefreshed(callback: (() -> Unit)?) {
//        currentPage.position = DataProvider.queryPosition(currentPage.position.book_id, currentPage.position.group
//                , currentPage.position.offset)
//        leftPage.position = currentPage.position.previous()
//        rightPage.position = currentPage.position.next()

        currentPage.refresh {
            rightPage.refresh {
                leftPage.refresh {
                    callback?.invoke()
                }
            }
        }

        ReaderStatus.position = currentPage.position
    }

    override fun onGroupRefreshed(position: Position, callback: (() -> Unit)?) {
        val position = Position(ReaderStatus.book.book_id, position.group, position.index, -1)
        DataProvider.revisePosition(position)

        leftPage.position = position.previous()
        currentPage.position = position
        rightPage.position = position.next()

        currentPage.refresh {

            ReaderStatus.position = currentPage.position
            rightPage.refresh {
                leftPage.refresh {
                    callback?.invoke()
                }
            }
        }

    }

    override fun onRefresh(position: Position) {
        AppHelper.glSurfaceView?.requestRender()
    }

    lateinit var leftPage:GLPage
    lateinit var currentPage:GLPage
    lateinit var rightPage:GLPage

    var isReady = false
        private set

    fun prepare(position: Position) {
        if (PageManager::leftPage.isInitialized) {
            leftPage.unloadTexture()
            currentPage.unloadTexture()
            rightPage.unloadTexture()
        }
        leftPage = GLPage(position.previous(), this)
        currentPage = GLPage(position, this)
        rightPage = GLPage(position.next(), this)

        DataProvider.groupListeners.add(this)

        ReaderStatus.position = currentPage.position
        isReady = true
    }

    fun clear() {
        isReady = false
        GLPage.destroy()
    }

    fun forwardPage() {
//        leftPage.unloadTexture()

        var temp = leftPage
        leftPage = currentPage
        currentPage = rightPage
        rightPage = temp
        rightPage.position = currentPage.position.next()
        rightPage.loadTexture()

        ReaderStatus.position = currentPage.position
    }

    fun backPage() {
//        rightPage.unloadTexture()

        var temp = rightPage
        rightPage = currentPage
        currentPage = leftPage
        leftPage = temp
        leftPage.position = currentPage.position.previous()
        leftPage.loadTexture()

        ReaderStatus.position = currentPage.position
    }

    fun isReadyForward(): Boolean {
        if (!rightPage.isLoaded.get() && rightPage.position.group < ReaderStatus.chapterList.size && !DataProvider.isGroupExist(rightPage.position.group)) {
            DataProvider.loadGroupWithBusyUI(rightPage.position.book_id, rightPage.position.group, {
                rightPage.refresh()
            })
        }
        return rightPage.isLoaded.get()
    }

    fun isReadyBack(): Boolean {
        if (!leftPage.isLoaded.get() && leftPage.position.group >= 0 && !DataProvider.isGroupExist(leftPage.position.group)) {
            DataProvider.loadGroupWithBusyUI(leftPage.position.book_id, leftPage.position.group, {
                leftPage.refresh()
            })
        }
        return leftPage.isLoaded.get()
    }
}