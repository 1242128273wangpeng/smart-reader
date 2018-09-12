package com.dy.reader.helper

import android.graphics.*
import android.text.TextUtils
import com.dy.reader.R
import com.dy.reader.Reader
import com.dy.reader.mode.NovelLineBean
import com.dy.reader.setting.ReaderSettings
import com.dy.reader.mode.NovelPageBean
import java.util.ArrayList
import com.dy.reader.setting.ReaderStatus
import com.dy.reader.util.TypefaceUtil

/**
 *  阅读页绘制辅助类
 *  左右滑动模式
 */
object DrawTextHelper {

    private val readerSettings = ReaderSettings.instance

    private val textPaint: Paint by lazy {
        val textPaint = Paint(Paint.FILTER_BITMAP_FLAG)
        textPaint.style = Paint.Style.FILL
        textPaint.isAntiAlias = true
        textPaint.isDither = true
        textPaint.color = Color.RED
        textPaint.textSize = readerSettings.FONT_CHAPTER_SIZE * AppHelper.screenScaledDensity
        textPaint.typeface = TypefaceUtil.loadTypeface(readerSettings.fontTypeface)
        textPaint
    }

    /**
     * paint
     * type  0: 设置画笔颜色
     */
    private fun setPaintColor(paint: Paint, type: Int): Paint {
        val color: Int

        if (readerSettings.readThemeMode == 51) {
            color = if (type == 0) {
                R.color.reading_backdrop_first
            } else {
                R.color.reading_text_color_first
            }
        } else if (readerSettings.readThemeMode == 511) {
            color = if (type == 0) {
                R.color.reading_backdrop_second
            } else {
                R.color.reading_text_color_blue
            }
        } else if (readerSettings.readThemeMode == 512) {
            color = if (type == 0) {
                R.color.reading_backdrop_second
            } else {
                R.color.reading_text_color_pink
            }
        } else if (readerSettings.readThemeMode == 513) {
            color = if (type == 0) {
                R.color.reading_backdrop_second
            } else {
                R.color.reading_text_color_green
            }
        } else if (readerSettings.readThemeMode == 514) {
            color = if (type == 0) {
                R.color.reading_backdrop_second
            } else {
                R.color.reading_text_color_dark
            }
        } else if (readerSettings.readThemeMode == 515) {
            color = if (type == 0) {
                R.color.reading_backdrop_second
            } else {
                R.color.reading_text_color_dim
            }
        } else if (readerSettings.readThemeMode == 52) {
            color = if (type == 0) {
                R.color.reading_backdrop_second
            } else {
                R.color.reading_text_color_second
            }
        } else if (readerSettings.readThemeMode == 53) {
            if (type == 0) {
                color = R.color.reading_backdrop_third
            } else {
                color = R.color.reading_text_color_third
            }
        } else if (readerSettings.readThemeMode == 54) {
            color = if (type == 0) {
                R.color.reading_backdrop_fourth
            } else {
                R.color.reading_text_color_fourth
            }
        } else if (readerSettings.readThemeMode == 55) {
            color = if (type == 0) {
                R.color.reading_backdrop_fifth
            } else {
                R.color.reading_text_color_fifth
            }
        } else if (readerSettings.readThemeMode == 56) {
            color = if (type == 0) {
                R.color.reading_backdrop_sixth
            } else {
                R.color.reading_text_color_sixth
            }
        } else if (readerSettings.readThemeMode == 61) {
            color = if (type == 0) {
                R.color.reading_backdrop_night
            } else {
                R.color.reading_text_color_night
            }
        } else {
            color = if (type == 0) {
                R.color.reading_backdrop_first
            } else {
                R.color.reading_text_color_first
            }
        }
        paint.color = Reader.context.resources.getColor(color)
        return paint
    }

    //上下滑动
    @Synchronized
    fun drawVerticalText(canvas: Canvas, pageBean: NovelPageBean) {
        setPaintColor(readerSettings.mPaint!!, 1)
        val pageLines = pageBean.lines
        val chapterNameList = pageBean.chapterNameLines

        if (pageLines != null && !pageLines.isEmpty()) {

            if (pageLines[0].lineContent!!.startsWith("txtzsydsq_homepage")) {// 封面页
                //                pageHeight = drawHomePage(canvas);
            } else if (pageLines[0].lineContent!!.startsWith("chapter_homepage")) {// 章节首页
                drawChapterPage(canvas, pageLines, chapterNameList)
            } else {
                for (i in pageLines.indices) {
                    val text = pageLines[i]
                    if (" " != text.lineContent) {
                        if (text.type == 1) {
                            drawLineIntervalText(canvas, text, text.indexY)
                        } else {
                            canvas.drawText(text.lineContent, readerSettings.mLineStart, text.indexY, readerSettings.mPaint!!)
                        }
                    }
                }
            }
        }
    }

    @Synchronized
    fun drawText(canvas: Canvas?, pageBean: NovelPageBean): Float {
        val pageLines = pageBean.lines
        if (readerSettings.mPaint != null) {
            setPaintColor(readerSettings.mPaint!!, 1)
        }
        if (pageLines != null && !pageLines.isEmpty()) {
            if (pageLines[0].lineContent!!.startsWith("txtzsydsq_homepage")) {// 封面页
                return drawHomePage(canvas)
            } else if (pageLines[0].lineContent!!.startsWith("chapter_homepage")) {// 章节首页
                return drawChapterPage(canvas, pageBean)
            } else {
                for (i in pageLines.indices) {
                    val text = pageLines[i]
                    if (" " != text.lineContent) {
                        if (text.type == 1) {
                            drawLineIntervalText(canvas, text, text.indexY)//开始画行
                        } else {
                            canvas?.drawText(text.lineContent, readerSettings.mLineStart, text.indexY, readerSettings.mPaint!!)//每段最后一行
                        }
                    }
                }
                return pageBean.height
            }
        }
        return AppHelper.screenHeight.toFloat()
    }

    /**
     * 完整行单个字符绘制
     */
    private fun drawLineIntervalText(canvas: Canvas?, novelLineBean: NovelLineBean?, total_y: Float) {
        if (novelLineBean == null || novelLineBean.arrLenths.size != novelLineBean.lineContent!!.length) {
            return
        }
        val content = novelLineBean.lineContent
        for (i in 0..content!!.length - 1) {
            val c = content[i]
            canvas?.drawText(c.toString(), novelLineBean.arrLenths[i], total_y, readerSettings.mPaint!!)
        }

    }

    //上下滑动首页
    private fun drawChapterPage(canvas: Canvas, pageLines: List<NovelLineBean>, chapterNameList: ArrayList<NovelLineBean>?) {
        textPaint.textSize = readerSettings.FONT_CHAPTER_SIZE * AppHelper.screenScaledDensity
        var fm_chapter: Paint.FontMetrics
        var m_iFontHeight_chapter: Float

        val y_chapter: Float

        // 章节头顶部间距
        y_chapter = 39 * AppHelper.screenScaledDensity

        setPaintColor(textPaint, 1)
        setPaintColor(readerSettings.mPaint!!, 1)

        val size_c: Int

        // 章节头
        if (chapterNameList != null && !chapterNameList.isEmpty()) {
            size_c = chapterNameList.size
            for (i in 0..size_c - 1) {
                if (i == 0) {
                    if (chapterNameList[0] != null && !TextUtils.isEmpty(chapterNameList[0].lineContent)) {
                        val chapterNameRemain = chapterNameList[0]

                        textPaint.textSize = 16 * AppHelper.screenScaledDensity
                        canvas.drawText(chapterNameRemain.lineContent, readerSettings.readContentPageLeftSpace * AppHelper.screenScaledDensity, y_chapter, textPaint)
                    }
                } else {
                    textPaint.textSize = 23 * AppHelper.screenScaledDensity
                    fm_chapter = textPaint.fontMetrics
                    m_iFontHeight_chapter = fm_chapter.descent - fm_chapter.ascent + 0.5f * readerSettings.FONT_CHAPTER_DEFAULT.toFloat() * AppHelper.screenScaledDensity
                    canvas.drawText(chapterNameList[i].lineContent, readerSettings.readContentPageLeftSpace * AppHelper.screenScaledDensity, y_chapter + m_iFontHeight_chapter * i, textPaint)
                }
            }

            var i = 0
            val j = pageLines.size
            while (i < j) {
                val text = pageLines[i]
                if (text != null && !TextUtils.isEmpty(text.lineContent)) {
                    if (text.lineContent != " " && text.lineContent != "chapter_homepage  ") {
                        if (text.type == 1) {
                            drawLineIntervalText(canvas, text, text.indexY)
                        } else {
                            canvas.drawText(text.lineContent, readerSettings.mLineStart, text.indexY, readerSettings.mPaint!!)
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
        textPaint.textSize = readerSettings.FONT_CHAPTER_SIZE * AppHelper.screenScaledDensity
        var fm_chapter: Paint.FontMetrics
        var m_iFontHeight_chapter: Float
        val y_chapter: Float
        y_chapter = 65 * AppHelper.screenScaledDensity

        setPaintColor(textPaint, 1)
        setPaintColor(readerSettings.mPaint!!, 1)
        val size_c: Int

        val chapterNameList = pageBean.chapterNameLines
        var hasContent = false
        val pageLines = pageBean.lines
        // 章节头
        if (chapterNameList != null && !chapterNameList.isEmpty()) {
            size_c = chapterNameList.size
            for (i in 0..size_c - 1) {
                if (i == 0) {
                    val chapterNameRemain = chapterNameList[0].lineContent
                    textPaint.textSize = 16 * AppHelper.screenScaledDensity
                    canvas?.drawText(chapterNameRemain, readerSettings.readContentPageLeftSpace * AppHelper.screenScaledDensity, y_chapter, textPaint)
                } else {
                    textPaint.textSize = 23 * AppHelper.screenScaledDensity
                    fm_chapter = textPaint.fontMetrics
                    m_iFontHeight_chapter = fm_chapter.descent - fm_chapter.ascent + 0.5f * readerSettings.FONT_CHAPTER_DEFAULT.toFloat() * AppHelper.screenScaledDensity
                    canvas?.drawText(chapterNameList[i].lineContent, readerSettings.readContentPageLeftSpace * AppHelper.screenScaledDensity, y_chapter + m_iFontHeight_chapter * i, textPaint)
                }
            }

            var i = 0
            val j = pageLines.size
            while (i < j) {
                hasContent = true
                val text = pageLines[i]
                if (text != null && !TextUtils.isEmpty(text.lineContent)) {
                    if (" " != text.lineContent && "chapter_homepage  " != text.lineContent) {
                        if (text.type == 1) {
                            drawLineIntervalText(canvas, text, text.indexY)
                        } else {
                            canvas?.drawText(text.lineContent, readerSettings.mLineStart, text.indexY, readerSettings.mPaint!!)
                        }
                    }
                }
                i++
            }
        }
        if (hasContent) {
            return pageBean.height
        }
        return AppHelper.screenHeight.toFloat()
    }


    /**
     *  绘制封面页效果 左右滑动
     */
    private fun drawHomePage(canvas: Canvas?): Float {
        val title_height = AppHelper.screenHeight / 3
        textPaint.textSize = 30 * AppHelper.screenScaledDensity
        val fm = textPaint.fontMetrics
        val y = fm.descent - fm.ascent + title_height
        val d_line = fm.descent - fm.ascent
        val nameList = ReadSeparateHelper.separateBookName()

        var paddingBottom = AppHelper.screenHeight - 25 * AppHelper.screenScaledDensity
        var bookNamePaddingY = AppHelper.screenHeight / 2 - d_line
        val sloganPaddingY = 40 * AppHelper.screenScaledDensity

        if (nameList.isEmpty()) {
            return 0f
        }
        var name_length = nameList.size
        name_length = if (name_length > 4) 4 else name_length
        var x_with = 0

        //封面页居中
        textPaint.textAlign = Paint.Align.CENTER
        if (textPaint.textAlign === Paint.Align.LEFT) {
        } else if (textPaint.textAlign === Paint.Align.CENTER) {
            x_with = AppHelper.screenWidth / 2
        }

        // 顶部slogan
        textPaint.textSize = 11 * AppHelper.screenScaledDensity
        textPaint.color = Color.parseColor("#80000000")
        textPaint.textAlign = Paint.Align.LEFT
        if (canvas != null) {
            drawSpacingText(canvas, Reader.context.resources.getString(R.string.reader_slogan), Reader.context.resources.getDimension(R.dimen.reading_cover_top_padding).toInt(), 11f, sloganPaddingY)
        }

        // 书籍名称
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = readerSettings.FONT_BOOKNAME_DEFAULT * AppHelper.screenScaledDensity
        val bookNameHeight = textPaint.fontMetrics.descent - textPaint.fontMetrics.ascent
        textPaint.color = Color.parseColor("#E6000000")
        for (i in 0 until name_length) {
            if (i > 0) {
                bookNamePaddingY += bookNameHeight + 10 * AppHelper.screenScaledDensity
            }
            val content = nameList[i].lineContent
            if (content != null) {
                canvas?.drawText(content, x_with.toFloat(), bookNamePaddingY, setPaintColor(textPaint, 1))
            }
        }

        // 作者
        val authHeight = bookNamePaddingY + bookNameHeight + 10 * AppHelper.screenScaledDensity
        textPaint.textSize = 14 * AppHelper.screenScaledDensity
        textPaint.color = Color.parseColor("#8C000000")
        if (!TextUtils.isEmpty(ReaderStatus.book.author)) {
            canvas?.drawText(ReaderStatus.book.author, x_with.toFloat(), authHeight, setPaintColor(textPaint, 1))
        }

        //底部icon及名称
        textPaint.textSize = 12 * AppHelper.screenScaledDensity
        textPaint.color = Color.parseColor("#80000000")
        textPaint.textAlign = Paint.Align.LEFT
        if (canvas != null) {
            drawSpacingText(canvas, Reader.context.resources.getString(R.string.application_name), Reader.context.resources.getDimension(R.dimen.reading_cover_bottom_padding).toInt(), 11f, paddingBottom)
        }
        textPaint.textAlign = Paint.Align.CENTER
        paddingBottom -= textPaint.fontMetrics.descent - textPaint.fontMetrics.ascent

        // 底部App灰色图标
        val iconBitmap = BitmapFactory.decodeResource(Reader.context.resources, R.drawable.reader_application_icon)

        // 计算左边位置
        val left = AppHelper.screenWidth / 2 - iconBitmap.getWidth() / 2
        // 计算上边位置
        val top = (paddingBottom - iconBitmap.getHeight() - 5 * AppHelper.screenScaledDensity).toInt()
        canvas?.drawBitmap(iconBitmap, Rect(0, 0, iconBitmap.getWidth(), iconBitmap.getHeight()),
                Rect(left, top, left + iconBitmap.getWidth(), top + iconBitmap.getHeight()),
                Paint())

        //默认情况
        textPaint.textAlign = Paint.Align.LEFT

        return AppHelper.screenHeight.toFloat()
    }

    /**
     * 绘制带间距文本
     */
    private fun drawSpacingText(canvas: Canvas, text: String, spacing: Int, textSize: Float, y: Float) {
        if (TextUtils.isEmpty(text)) return
        val textWidth = textPaint.measureText(text[0].toString())
        val textTotalWidth = textWidth * text.length + AppHelper.px2dp(spacing) * (text.length - 1)
        val drawTextStart = (AppHelper.screenWidth - textTotalWidth) / 2
        var drawTextX = 0f
        for (i in 0 until text.length) {
            drawTextX += if (i == 0) drawTextStart else textWidth + AppHelper.px2dp(spacing)
            canvas.drawText(text[i].toString(), drawTextX, y, setPaintColor(textPaint, 1))
        }
    }

}