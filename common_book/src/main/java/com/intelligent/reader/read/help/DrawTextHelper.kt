package com.intelligent.reader.read.help

import com.intelligent.reader.R
import com.intelligent.reader.app.BookApplication
import com.intelligent.reader.read.mode.NovelPageBean

import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.NovelLineBean
import net.lzbook.kit.data.bean.ReadConfig
import net.lzbook.kit.data.bean.ReadStatus
import net.lzbook.kit.data.bean.SensitiveWords
import net.lzbook.kit.utils.AppLog

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.FontMetrics
import android.text.TextUtils

import java.util.ArrayList

class DrawTextHelper(private val resources: Resources) {

    private val textPaint: Paint
    private val readStatus: ReadStatus

    private val readSensitiveWord: SensitiveWords?
    private var readSensitiveWords: List<String>? = null
    private var noReadSensitive = false

    init {
        this.readSensitiveWord = SensitiveWords.getReadSensitiveWords()
        if (readSensitiveWord != null && readSensitiveWord.list.size > 0) {
            readSensitiveWords = readSensitiveWord.getList()
            noReadSensitive = false
        } else {
            noReadSensitive = true
        }

        readStatus = BookApplication.getGlobalContext().readStatus

        textPaint = Paint(Paint.FILTER_BITMAP_FLAG)
        textPaint.style = Paint.Style.FILL
        textPaint.isAntiAlias = true
        textPaint.isDither = true
        textPaint.color = Color.RED
        textPaint.textSize = Constants.FONT_CHAPTER_SIZE * readStatus.screenScaledDensity
    }

    /**
     * paint
     * type  0: 画背景
     */
    private fun setPaintColor(paint: Paint, type: Int): Paint {

        val color_int: Int
        if (Constants.MODE == 51) {
            if (type == 0) {
                color_int = R.color.reading_backdrop_first
            } else {
                color_int = R.color.reading_text_color_first
            }
        } else if (Constants.MODE == 52) {
            if (type == 0) {
                color_int = R.color.reading_backdrop_second
            } else {
                color_int = R.color.reading_text_color_second
            }
        } else if (Constants.MODE == 53) {
            if (type == 0) {
                color_int = R.color.reading_backdrop_third
            } else {
                color_int = R.color.reading_text_color_third
            }
        } else if (Constants.MODE == 54) {
            if (type == 0) {
                color_int = R.color.reading_backdrop_fourth
            } else {
                color_int = R.color.reading_text_color_fourth
            }
        } else if (Constants.MODE == 55) {
            if (type == 0) {
                color_int = R.color.reading_backdrop_fifth
            } else {
                color_int = R.color.reading_text_color_fifth
            }
        } else if (Constants.MODE == 56) {
            if (type == 0) {
                color_int = R.color.reading_backdrop_sixth
            } else {
                color_int = R.color.reading_text_color_sixth
            }
        } else if (Constants.MODE == 61) {
            if (type == 0) {
                color_int = R.color.reading_backdrop_night
            } else {
                color_int = R.color.reading_text_color_night
            }
        } else {
            if (type == 0) {
                color_int = R.color.reading_backdrop_first
            } else {
                color_int = R.color.reading_text_color_first
            }
        }
        paint.color = resources.getColor(color_int)
        return paint
    }

    //上下滑动
    @Synchronized
    fun drawVerticalText(canvas: Canvas, pageBean: NovelPageBean) {
        readStatus.currentPageConentLength = 0

        setPaintColor(ReadConfig.mPaint!!, 1)
        val pageLines = pageBean.lines
        val chapterNameList = pageBean.chapterNameLines

        if (pageLines != null && !pageLines.isEmpty()) {

            if (pageLines[0].lineContent.startsWith("txtzsydsq_homepage")) {// 封面页
                //                pageHeight = drawHomePage(canvas);
            } else if (pageLines[0].lineContent.startsWith("chapter_homepage")) {// 章节首页
                drawChapterPage(canvas, pageLines, chapterNameList)
            } else {
                for (i in pageLines.indices) {
                    val text = pageLines[i]
                    replaceSensitiveWords(text)
                    if (" " != text.lineContent) {
                        if (text.type == 1) {
                            drawLineIntervalText(canvas, text, text.indexY)
                        } else {
                            canvas.drawText(text.lineContent, ReadConfig.mLineStart, text.indexY, ReadConfig.mPaint!!)
                        }

                        readStatus.currentPageConentLength += text.lineContent.length
                    }
                }
            }
        }
    }

    @Synchronized
    fun drawText(canvas: Canvas?, pageBean: NovelPageBean): Float {
        val pageLines = pageBean.lines
        setPaintColor(ReadConfig.mPaint!!, 1)
        if (pageLines != null && !pageLines.isEmpty()) {
            if (pageLines[0].lineContent.startsWith("txtzsydsq_homepage")) {// 封面页
                return ReadConfig.screenHeight.toFloat()
            } else if (pageLines[0].lineContent.startsWith("chapter_homepage")) {// 章节首页
                return drawChapterPage(canvas, pageBean)
            } else {
                for (i in pageLines.indices) {
                    val text = pageLines[i]
                    replaceSensitiveWords(text)
                    if (" " != text.lineContent) {
                        if (text.type == 1) {
                            drawLineIntervalText(canvas, text, text.indexY)//开始画行
                        } else {
                            canvas?.drawText(text.lineContent, ReadConfig.mLineStart, text.indexY, ReadConfig.mPaint!!)//每段最后一行
                        }
                    }
                }
                return pageBean.height
            }
        }
        return ReadConfig.screenHeight.toFloat()
    }

    private fun replaceSensitiveWords(bean: NovelLineBean) {
        if (Constants.isShielding && !noReadSensitive) {
            for (word in readSensitiveWords!!) {
                bean.lineContent = bean.lineContent.replace(word, getChangeWord(word.length))
            }
        }
    }

    /**
     * 完整行单个字符绘制
     */
    private fun drawLineIntervalText(canvas: Canvas?, novelLineBean: NovelLineBean?, total_y: Float) {
        if (novelLineBean == null || novelLineBean.arrLenths.size != novelLineBean.lineContent.length) {
            return
        }
        for (i in 0..novelLineBean.lineContent.length - 1) {
            val c = novelLineBean.lineContent[i]
            canvas?.drawText(c.toString(), novelLineBean.arrLenths[i], total_y, ReadConfig.mPaint!!)
        }

    }

    //上下滑动首页
    private fun drawChapterPage(canvas: Canvas, pageLines: List<NovelLineBean>, chapterNameList: ArrayList<NovelLineBean>?) {
        textPaint.textSize = Constants.FONT_CHAPTER_SIZE * readStatus.screenScaledDensity
        var fm_chapter: FontMetrics
        var m_iFontHeight_chapter: Float

        val y_chapter: Float

        // 章节头顶部间距
        y_chapter = 39 * readStatus.screenScaledDensity

        setPaintColor(textPaint, 1)
        setPaintColor(ReadConfig.mPaint!!, 1)

        val size_c: Int

        // 章节头
        if (chapterNameList != null && !chapterNameList.isEmpty()) {
            size_c = chapterNameList.size
            for (i in 0..size_c - 1) {
                if (i == 0) {
                    if (chapterNameList[0] != null && !TextUtils.isEmpty(chapterNameList[0].lineContent)) {
                        val chapterNameRemain = chapterNameList[0]

                        textPaint.textSize = 16 * readStatus.screenScaledDensity
                        canvas.drawText(chapterNameRemain.lineContent, Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity, y_chapter, textPaint)
                    }
                } else {
                    textPaint.textSize = 23 * readStatus.screenScaledDensity
                    fm_chapter = textPaint.fontMetrics
                    m_iFontHeight_chapter = fm_chapter.descent - fm_chapter.ascent + 0.5f * Constants.FONT_CHAPTER_DEFAULT.toFloat() * readStatus.screenScaledDensity
                    canvas.drawText(chapterNameList[i].lineContent, Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity, y_chapter + m_iFontHeight_chapter * i, textPaint)
                }
            }

            var i = 0
            val j = pageLines.size
            while (i < j) {
                if (i > size_c) {
                    val text = pageLines[i]
                    replaceSensitiveWords(text)
                    if (text != null && !TextUtils.isEmpty(text.lineContent)) {
                        if (text.lineContent != " " && text.lineContent != "chapter_homepage  ") {
                            if (text.type == 1) {
                                drawLineIntervalText(canvas, text, text.indexY)
                            } else {
                                canvas.drawText(text.lineContent, ReadConfig.mLineStart, text.indexY, ReadConfig.mPaint!!)
                            }

                            readStatus.currentPageConentLength += text.lineContent.length
                        }
                    }
                }
                i++
            }
        }
    }

    /*
     * 章节首页提示效果
     */
    private fun drawChapterPage(canvas: Canvas?, pageBean: NovelPageBean): Float {
        textPaint.textSize = Constants.FONT_CHAPTER_SIZE * readStatus.screenScaledDensity
        var fm_chapter: FontMetrics
        var m_iFontHeight_chapter: Float
        val y_chapter: Float
        y_chapter = 65 * readStatus.screenScaledDensity

        setPaintColor(textPaint, 1)
        setPaintColor(ReadConfig.mPaint!!, 1)
        val size_c: Int

        val chapterNameList = pageBean.chapterNameLines
        var hasContent = false
        val pageLines = pageBean.lines
        // 章节头
        if (chapterNameList != null && !chapterNameList.isEmpty()) {
            size_c = chapterNameList.size
            for (i in 0..size_c - 1) {
                if (i == 0) {
                    AppLog.e(TAG, "DrawChapterPage: " + readStatus.chapterName)
                    if (!TextUtils.isEmpty(readStatus.chapterName)) {
                        val chapterNameRemain = chapterNameList[0].lineContent

                        textPaint.textSize = 16 * readStatus.screenScaledDensity
                        canvas?.drawText(chapterNameRemain, Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity, y_chapter, textPaint)
                    }
                } else {
                    textPaint.textSize = 23 * readStatus.screenScaledDensity
                    fm_chapter = textPaint.fontMetrics
                    m_iFontHeight_chapter = fm_chapter.descent - fm_chapter.ascent + 0.5f * Constants.FONT_CHAPTER_DEFAULT.toFloat() * readStatus.screenScaledDensity
                    canvas?.drawText(chapterNameList[i].lineContent, Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity, y_chapter + m_iFontHeight_chapter * i, textPaint)
                }
            }

            var i = 0
            val j = pageLines.size
            while (i < j) {
                if (i > size_c) {
                    hasContent = true
                    val text = pageLines[i]
                    replaceSensitiveWords(text)
                    if (text != null && !TextUtils.isEmpty(text.lineContent)) {
                        if (" " != text.lineContent && "chapter_homepage  " != text.lineContent) {
                            if (text.type == 1) {
                                drawLineIntervalText(canvas, text, text.indexY)
                            } else {
                                canvas?.drawText(text.lineContent, ReadConfig.mLineStart, text.indexY, ReadConfig.mPaint!!)
                            }
                        }
                    }

                }
                i++
            }
        }
        if (hasContent) {
            return pageBean.height
        }
        return ReadConfig.screenHeight.toFloat()
    }

    private fun getChangeWord(len: Int): String {
        val stringBuffer = StringBuilder(len)
        for (i in 0..len - 1) {
            stringBuffer.append('*')
        }
        return stringBuffer.toString()
    }

    companion object {
        private val TAG = "DrawTextHelper"
    }
}
