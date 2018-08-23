package com.ding.basic.bean

import java.io.Serializable
import java.util.ArrayList

/**
 * Created by yuchao on 2017/11/2 0002.
 */

class ContextFixState : Serializable {

    private val chapterMsgFixStates: ArrayList<Boolean>?
    private val chapterContFixStates: ArrayList<Boolean>?

    /**
     * 判断章节目录修复完成, 且章节内容修复完成
     */
    val fixState: Boolean
        get() {
            if (chapterMsgFixStates != null && chapterContFixStates != null) {
                if (chapterMsgFixStates.isEmpty()) {
                    return false
                }
                for (item in chapterMsgFixStates) {
                    if (!item) {
                        return item
                    }
                }
                if (chapterContFixStates.isEmpty()) {
                    return true
                }
                for (item in chapterContFixStates) {
                    if (!item) {
                        return item
                    }
                }
                return true
            }
            return false
        }

    /**
     * 判断是否修复成功过章节内容
     */
    val saveFixState: Boolean
        get() {
            if (chapterContFixStates != null) {
                if (chapterContFixStates.isEmpty()) {
                    return false
                }
                for (item in chapterContFixStates) {
                    if (!item) {
                        return item
                    }
                }
                return true
            }
            return false
        }

    init {
        chapterMsgFixStates = ArrayList()
        chapterContFixStates = ArrayList()
    }

    fun addMsgState(b: Boolean) {
        chapterMsgFixStates?.add(b)
    }

    fun addContState(b: Boolean) {
        chapterContFixStates?.add(b)
    }
}
