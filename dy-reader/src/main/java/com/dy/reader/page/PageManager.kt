package com.dy.reader.page

import android.annotation.SuppressLint
import com.dingyue.statistics.DyStatService
import com.dy.reader.data.DataProvider
import com.dy.reader.helper.AppHelper
import com.dy.reader.setting.ReaderStatus
import com.orhanobut.logger.Logger

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

    lateinit var leftPage: GLPage
    lateinit var currentPage: GLPage
    lateinit var rightPage: GLPage

    var isReady = false
        private set

    var destroy = false
        private set

    fun prepare(position: Position) {
        if (PageManager::leftPage.isInitialized) {
            Logger.e("unloadTexture")
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

    fun init() {
        destroy = false
    }

    fun destroy() {
        destroy = true
    }

    fun forwardPage() {
//        leftPage.unloadTexture()

        var temp = leftPage
        leftPage = currentPage
        currentPage = rightPage
        rightPage = temp
        rightPage.position = currentPage.position.next()
        rightPage.loadTexture()

        if (ReaderStatus.position.group != -1 && ReaderStatus.position.index == ReaderStatus.position.groupChildCount - 1) {
            Logger.e("打点统计PV: " + ReaderStatus.position.group + " : " + ReaderStatus.position.groupChildCount + " : " + ReaderStatus.position.index)

            DyStatService.sendPVData(ReaderStatus.startTime, ReaderStatus.book.book_id, ReaderStatus.currentChapter?.chapter_id.toString(), ReaderStatus.book.book_source_id, if ("zn" == ReaderStatus.book.book_type) {
                "2"
            } else {
                "1"
            }, ReaderStatus.position.groupChildCount)

            ReaderStatus.startTime = System.currentTimeMillis()
        }

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
        if (!rightPage.isLoaded.get() && rightPage.position.group < ReaderStatus.chapterList.size) {
            if (!DataProvider.isGroupExist(rightPage.position.group)) {
                DataProvider.loadGroupWithBusyUI(rightPage.position.book_id, rightPage.position.group)
            } else {
                rightPage.refresh()
            }
        }
        return rightPage.isLoaded.get()
    }

    fun isReadyBack(): Boolean {
        if (!leftPage.isLoaded.get() && leftPage.position.group >= 0) {
            if (!DataProvider.isGroupExist(leftPage.position.group)) {
                DataProvider.loadGroupWithBusyUI(leftPage.position.book_id, leftPage.position.group)
            } else {
                leftPage.refresh()
            }
        }
        return leftPage.isLoaded.get()
    }

    fun refreshLeftAndRightPage() {
        try {
            leftPage.refresh()
            rightPage.refresh()
        } catch (ex: Exception) {
            Logger.e("refreshLeftAndRightPage: " + ex.message)
        }
    }
}