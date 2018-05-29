package com.dy.reader.mode

import com.dy.reader.setting.ReaderSettings

import android.graphics.Rect
import android.text.TextUtils
import com.dy.reader.constants.ReadConstants
import net.lzbook.kit.data.bean.ReadConfig.mWidth

import java.io.Serializable
import java.util.ArrayList

/**
 * Created by yuchao on 2017/9/15 0015.
 */

class NovelLineBean : Serializable {
    var lineContent: String? = null
    var lineLength: Float = 0.toFloat()
    //type:0-不是完整行 1-完整行
    var type = -1
    var isLastIsPunct: Boolean = false
    var arrLenths = ArrayList<Float>()

    var sequence: Int = 0

    var sequenceType: Int = 0

    var chapterName: String? = null

    var isLastPage: Boolean = false

    var position: Int = 0

    var indexY: Float = 0.toFloat()

    constructor() {}

    constructor(lineContent: String, lineLength: Float, completeLine: Int, lastIsPunct: Boolean, arrLenths: ArrayList<Float>?) {
        this.lineContent = lineContent
        this.lineLength = lineLength
        this.type = completeLine
        this.isLastIsPunct = lastIsPunct
        initDrawIndex(arrLenths)
    }

    private fun initDrawIndex(arrLenths: ArrayList<Float>?) {
        if (arrLenths == null || arrLenths.isEmpty() || TextUtils.isEmpty(lineContent)) {
            return
        }
        val length = lineContent!!.length
        val charNum: Int
        if (isLastIsPunct) {
            charNum = length - 2
        } else {
            charNum = length - 1
        }
        val marg = (ReaderSettings.instance.mWidth - lineLength) / charNum
        var star: Float
        if (!this.arrLenths.isEmpty()) {
            this.arrLenths.clear()
        }
        for (i in 0 until length) {
            star = ReaderSettings.instance.mLineStart + arrLenths[i] + marg * i
            val c = lineContent!![i]
            if (i == length - 1 && isEndPunct(c)) {
                val rect = Rect()
                ReaderSettings.instance.mPaint?.getTextBounds(c.toString(), 0, 1, rect)
                star -= marg
                star -= rect.left.toFloat()
                star += (ReadConstants.chineseWth / 2 - rect.width()) / 2
            }
            this.arrLenths.add(star)
        }
    }

    private fun isEndPunct(ch: Char): Boolean {
        var isInclude = false
        for (c in ReadConstants.endPuncts) {
            if (ch == c) {
                isInclude = true
                break
            }
        }
        return isInclude
    }

    override fun toString(): String {
        return "NovelLineBean{" +
                "lineContent=" + lineContent +
                '}'
    }

    companion object {
        private const val serialVersionUID = 9173402120406347893L
    }
}
