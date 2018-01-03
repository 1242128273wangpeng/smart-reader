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
import android.graphics.PorterDuff
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
    @Synchronized fun drawVerticalText(canvas: Canvas, pageBean: NovelPageBean): Float {
        readStatus.currentPageConentLength = 0
        val fm = ReadConfig.mPaint!!.fontMetrics

        val lineSpace = Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE.toFloat() * readStatus.screenScaledDensity
        val m_iFontHeight = fm.descent - fm.ascent + lineSpace
        val m_duan = Constants.READ_PARAGRAPH_SPACE * lineSpace

        var total_y = -fm.ascent
        var pageHeight = 0f

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        val pageLines = pageBean.lines
        val chapterNameList = pageBean.chapterNameLines

        if (pageLines != null && !pageLines.isEmpty()) {

            if (pageLines[0].lineContent.startsWith("txtzsydsq_homepage")) {// 封面页
                //                pageHeight = drawHomePage(canvas);
            } else if (pageLines[0].lineContent.startsWith("chapter_homepage")) {// 章节首页
                pageHeight = drawChapterPage(canvas, pageLines, chapterNameList)
            } else {
                for (i in pageLines.indices) {
                    val text = pageLines[i]
                    replaceSensitiveWords(text)
                    if (text != null && !TextUtils.isEmpty(text.lineContent) && text.lineContent == " ") {
                        total_y += m_duan
                    } else {

                        if (text.type == 1) {
                            drawLineIntervalText(canvas, text, total_y)
                        } else {
                            canvas.drawText(text.lineContent, ReadConfig.mLineStart, total_y, ReadConfig.mPaint!!)
                        }

                        total_y += m_iFontHeight
                        readStatus.currentPageConentLength += text.lineContent.length
                    }
                    if (i == pageLines.size - 1) {
                        pageHeight = total_y + fm.ascent
                    }
                }
            }
            return pageHeight
        }
        return pageHeight
    }

    @Synchronized fun drawText(canvas: Canvas?, pageBean: NovelPageBean): Float {
        val pageLines = pageBean.lines

        if (pageLines != null && !pageLines.isEmpty()) {
            if (pageLines[0].lineContent.startsWith("txtzsydsq_homepage")) {// 封面页
                return ReadConfig.screenHeight.toFloat()
            } else if (pageLines[0].lineContent.startsWith("chapter_homepage")) {// 章节首页
                return drawChapterPage(canvas, pageBean)
            } else {
                var lastY: Float = 0.0f
                for (i in pageLines.indices) {
                    val text = pageLines[i]
                    replaceSensitiveWords(text)
                    if (" " != text.lineContent) {
                        if (text.type == 1) {
                            drawLineIntervalText(canvas, text, text.indexY)//开始画行
                            lastY = text.indexY
                        } else {
                            canvas?.drawText(text.lineContent, ReadConfig.mLineStart, text.indexY, ReadConfig.mPaint!!)//每段最后一行
                            lastY = text.indexY
                        }
                    }
                }
                return lastY
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
    private fun drawChapterPage(canvas: Canvas, pageLines: List<NovelLineBean>, chapterNameList: ArrayList<NovelLineBean>?): Float {
        textPaint.textSize = Constants.FONT_CHAPTER_SIZE * readStatus.screenScaledDensity
        var fm_chapter: FontMetrics
        var m_iFontHeight_chapter: Float

        val fm = ReadConfig.mPaint!!.fontMetrics

        val lineSpace = Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE.toFloat() * readStatus.screenScaledDensity
        val m_iFontHeight = fm.descent - fm.ascent + lineSpace
        val m_duan = Constants.READ_PARAGRAPH_SPACE * lineSpace

        // 正文行首与顶部间距
        var total_y = -fm.ascent
        val y_chapter: Float
        var pageHeight = 0f

        // 章节头顶部间距
        y_chapter = 39 * readStatus.screenScaledDensity

        setPaintColor(textPaint, 1)

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

            total_y += 3f * 15f * readStatus.screenScaledDensity
            total_y += Constants.READ_CONTENT_PAGE_TOP_SPACE * readStatus.screenDensity

            // 章节头与正文间距
            if (size_c > 1) {
                total_y += 75 * readStatus.screenScaledDensity
            }

            var i = 0
            val j = pageLines.size
            while (i < j) {
                if (i > size_c) {
                    val text = pageLines[i]
                    replaceSensitiveWords(text)
                    if (text != null && !TextUtils.isEmpty(text.lineContent)) {
                        if (text.lineContent == " ") {
                            total_y += m_duan
                        } else if (text.lineContent == "chapter_homepage  ") {

                        } else {

                            if (text.type == 1) {
                                drawLineIntervalText(canvas, text, total_y)
                            } else {
                                canvas.drawText(text.lineContent, ReadConfig.mLineStart, total_y, ReadConfig.mPaint!!)
                            }

                            total_y += m_iFontHeight
                            readStatus.currentPageConentLength += text.lineContent.length
                        }
                    }
                }

                if (i == pageLines.size - 1) {
                    pageHeight = total_y + fm.ascent
                }
                i++
            }
        }
        return pageHeight
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
        val size_c: Int

        val chapterNameList = pageBean.chapterNameLines
        var hasContent = false
        var lastY: Float = 0.0f
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
                                lastY = text.indexY
                            } else {
                                canvas?.drawText(text.lineContent, ReadConfig.mLineStart, text.indexY, ReadConfig.mPaint!!)
                                lastY = text.indexY
                            }
                        }
                    }

                }
                i++
            }
        }
        if (hasContent) {
            return lastY
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
