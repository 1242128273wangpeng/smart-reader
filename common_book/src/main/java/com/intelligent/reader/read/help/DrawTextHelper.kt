package com.intelligent.reader.read.help

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.FontMetrics
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import com.intelligent.reader.R
import com.intelligent.reader.read.mode.NovelPageBean
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.NovelLineBean
import net.lzbook.kit.data.bean.ReadConfig
import net.lzbook.kit.data.bean.SensitiveWords
import java.util.*

class DrawTextHelper(private val context: Context) {

    private val textPaint: Paint

    private var readSensitiveWords: List<String>? = null
    private var noReadSensitive = false

    init {
        val readSensitiveWord = SensitiveWords.getReadSensitiveWords()
        if (readSensitiveWord != null && readSensitiveWord.list.size > 0) {
            readSensitiveWords = readSensitiveWord.getList()
            noReadSensitive = false
        } else {
            noReadSensitive = true
        }

        textPaint = Paint(Paint.FILTER_BITMAP_FLAG)
        textPaint.style = Paint.Style.FILL
        textPaint.isAntiAlias = true
        textPaint.isDither = true
        textPaint.color = Color.RED
        textPaint.textSize = ReadConfig.FONT_CHAPTER_SIZE * ReadConfig.screenScaledDensity
    }

    /**
     * paint
     * type  0: 设置画笔颜色
     */
    private fun setPaintColor(paint: Paint, type: Int): Paint {

        val color = when (ReadConfig.MODE) {
            51 -> if (type == 0) R.color.reading_backdrop_first else R.color.reading_text_color_first
            52 -> if (type == 0) R.color.reading_backdrop_second else R.color.reading_text_color_second
            53 -> if (type == 0) R.color.reading_backdrop_third else R.color.reading_text_color_third
            54 -> if (type == 0) R.color.reading_backdrop_fourth else R.color.reading_text_color_fourth
            55 -> if (type == 0) R.color.reading_backdrop_fifth else R.color.reading_text_color_fifth
            56 -> if (type == 0) R.color.reading_backdrop_sixth else R.color.reading_text_color_sixth
            61 -> if (type == 0) R.color.reading_backdrop_night else R.color.reading_text_color_night
            else -> if (type == 0) R.color.reading_backdrop_first else R.color.reading_text_color_first
        }

        paint.color = ContextCompat.getColor(context, color)
        return paint

    }

    //上下滑动
    @Synchronized
    fun drawVerticalText(canvas: Canvas, pageBean: NovelPageBean) {
        setPaintColor(ReadConfig.mPaint!!, 1)

        val pageLines = pageBean.lines
        val chapterNameList = pageBean.chapterNameLines

        if (!pageLines.isEmpty()) {
            when {
            // 封面页
                pageLines[0].lineContent.startsWith("txtzsydsq_homepage") -> {//pageHeight = drawHomePage(canvas);
                }
            // 章节首页
                pageLines[0].lineContent.startsWith("chapter_homepage") -> drawChapterPage(canvas, pageLines, chapterNameList)
                else -> drawPageLine(canvas, pageLines)

            }
        }
    }

    @Synchronized
    fun drawText(canvas: Canvas?, pageBean: NovelPageBean): Float {
        setPaintColor(ReadConfig.mPaint!!, 1)

        val pageLines = pageBean.lines

        if (!pageLines.isEmpty()) {
            when {
            // 封面页
                pageLines[0].lineContent.startsWith("txtzsydsq_homepage") -> ReadConfig.screenHeight.toFloat()
            // 章节首页
                pageLines[0].lineContent.startsWith("chapter_homepage") -> drawChapterPage(canvas, pageBean)
                else -> {
                    drawPageLine(canvas, pageLines)
                    return pageBean.height
                }
            }
        }

        return ReadConfig.screenHeight.toFloat()
    }


    /**
     * 画行
     */
    private fun drawPageLine(canvas: Canvas?, pageLines: ArrayList<NovelLineBean>) {

        for (i in pageLines.indices) {
            val text = pageLines[i]
            replaceSensitiveWords(text)
            if (" " != text.lineContent) {
                if (text.type == 1) {
                    drawLineIntervalText(canvas, text, text.indexY)//开始画行
                } else {
                    canvas?.drawText(text.lineContent, ReadConfig.mLineStart, text.indexY, ReadConfig.mPaint)//每段最后一行
                }
            }
        }
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

        //until：区间包头不包尾
        for (i in 0 until novelLineBean.lineContent.length) {
            val c = novelLineBean.lineContent[i]
            canvas?.drawText(c.toString(), novelLineBean.arrLenths[i], total_y, ReadConfig.mPaint)
        }

    }

    /**
     * 上下滑动首页
     */
    private fun drawChapterPage(canvas: Canvas, pageLines: List<NovelLineBean>, chapterNameList: ArrayList<NovelLineBean>?) {
        textPaint.textSize = ReadConfig.FONT_CHAPTER_SIZE * ReadConfig.screenScaledDensity
        var fmChapter: FontMetrics
        var fontHeightChapter: Float

        // 章节头顶部间距
        val headSpacing = 39 * ReadConfig.screenScaledDensity

        setPaintColor(textPaint, 1)
        ReadConfig.mPaint?.let {
            setPaintColor(it, 1)
        }



        // 章节头
        if (chapterNameList != null && !chapterNameList.isEmpty()) {
            val size = chapterNameList.size
            for (i in 0 until size) {
                if (i == 0) {
                    if (!TextUtils.isEmpty(chapterNameList[0].lineContent)) {
                        val chapterNameRemain = chapterNameList[0]

                        textPaint.textSize = 16 * ReadConfig.screenScaledDensity
                        canvas.drawText(chapterNameRemain.lineContent, ReadConfig.READ_CONTENT_PAGE_LEFT_SPACE * ReadConfig.screenScaledDensity, headSpacing, textPaint)
                    }
                } else {
                    textPaint.textSize = 23 * ReadConfig.screenScaledDensity
                    fmChapter = textPaint.fontMetrics
                    fontHeightChapter = fmChapter.descent - fmChapter.ascent + 0.5f * ReadConfig.FONT_CHAPTER_DEFAULT.toFloat() * ReadConfig.screenScaledDensity
                    canvas.drawText(chapterNameList[i].lineContent, ReadConfig.READ_CONTENT_PAGE_LEFT_SPACE * ReadConfig.screenScaledDensity, headSpacing + fontHeightChapter * i, textPaint)
                }
            }

            var i = 0
            val pageLineSize = pageLines.size
            while (i < pageLineSize) {
                val text = pageLines[i]
                replaceSensitiveWords(text)
                if (!TextUtils.isEmpty(text.lineContent)) {
                    if (text.lineContent != " " && text.lineContent != "chapter_homepage  ") {
                        if (text.type == 1) {
                            drawLineIntervalText(canvas, text, text.indexY)
                        } else {
                            canvas.drawText(text.lineContent, ReadConfig.mLineStart, text.indexY, ReadConfig.mPaint!!)
                        }
                    }
                }
                i++
            }
        }
    }

    /**
     * 章节首页提示效果
     */
    private fun drawChapterPage(canvas: Canvas?, pageBean: NovelPageBean): Float {
        textPaint.textSize = ReadConfig.FONT_CHAPTER_SIZE * ReadConfig.screenScaledDensity
        var fmChapter: FontMetrics
        var fontHeightChapter: Float
        val headSpacing = 65 * ReadConfig.screenScaledDensity

        setPaintColor(textPaint, 1)
        setPaintColor(ReadConfig.mPaint!!, 1)

        val chapterNameList = pageBean.chapterNameLines
        var hasContent = false
        val pageLines = pageBean.lines

        // 章节头
        if (chapterNameList.isNotEmpty()) {
            val listSize = chapterNameList.size
            for (i in 0 until listSize) {
                if (i == 0) {
                    val chapterNameRemain = chapterNameList[0].lineContent
                    textPaint.textSize = 16 * ReadConfig.screenScaledDensity
                    canvas?.drawText(chapterNameRemain, ReadConfig.READ_CONTENT_PAGE_LEFT_SPACE * ReadConfig.screenScaledDensity, headSpacing, textPaint)
                } else {
                    textPaint.textSize = 23 * ReadConfig.screenScaledDensity
                    fmChapter = textPaint.fontMetrics
                    fontHeightChapter = fmChapter.descent - fmChapter.ascent + 0.5f * ReadConfig.FONT_CHAPTER_DEFAULT.toFloat() * ReadConfig.screenScaledDensity
                    canvas?.drawText(chapterNameList[i].lineContent, ReadConfig.READ_CONTENT_PAGE_LEFT_SPACE * ReadConfig.screenScaledDensity, headSpacing + fontHeightChapter * i, textPaint)
                }
            }

            var i = 0
            val j = pageLines.size
            while (i < j) {
                hasContent = true
                val text = pageLines[i]
                replaceSensitiveWords(text)
                if (!TextUtils.isEmpty(text.lineContent)) {
                    if (" " != text.lineContent && "chapter_homepage  " != text.lineContent) {
                        if (text.type == 1) {
                            drawLineIntervalText(canvas, text, text.indexY)
                        } else {
                            canvas?.drawText(text.lineContent, ReadConfig.mLineStart, text.indexY, ReadConfig.mPaint!!)
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
        for (i in 0 until len) {
            stringBuffer.append('*')
        }
        return stringBuffer.toString()
    }

}
