package com.dy.reader.helper

import android.graphics.Paint
import android.text.TextPaint
import android.text.TextUtils
import com.dy.reader.constants.ReadConstants
import com.dy.reader.mode.NovelLineBean
import com.dy.reader.page.GLReaderView
import com.dy.reader.setting.ReaderSettings
import com.dy.reader.setting.ReaderStatus
import com.dy.reader.mode.NovelPageBean
import com.dy.reader.util.TypefaceUtil
import java.util.ArrayList

/**
 * 分页帮助类
 * Created by wt on 2017/12/20.
 */

object ReadSeparateHelper {

    val readerSettings = ReaderSettings.instance
    /**
     * 章节内容,章节名分行
     * *
     * @return
     */
    fun initTextSeparateContent(content: String, chapterName: String): ArrayList<NovelPageBean> {
        var content = content
        val chapterHeight = 75 * AppHelper.screenScaledDensity
        val hideHeight = 15 * AppHelper.screenScaledDensity

        initPaint()

        val mchapterPaint = TextPaint()

        // 显示文字区域高度
        val height = AppHelper.screenHeight - AppHelper.screenDensity * readerSettings.readContentPageTopSpace.toFloat() * 2f

        readerSettings.mWidth = AppHelper.screenWidth - AppHelper.screenDensity * readerSettings.readContentPageLeftSpace.toFloat() * 2f
        readerSettings.mLineStart = readerSettings.readContentPageLeftSpace * AppHelper.screenScaledDensity

        val lineHeight = readerSettings.mFontHeight
        val m_duan = readerSettings.mDuan

        // 添加转换提示
        val sb = StringBuilder()

        sb.append("chapter_homepage \n")
        sb.append("chapter_homepage \n")
        sb.append("chapter_homepage \n")
        var novelText: ArrayList<NovelLineBean> = arrayListOf()

        if (!TextUtils.isEmpty(chapterName)) {
            val chapterNameSplit = ArrayList<String>()
            if (chapterName.contains("章")) {
                chapterNameSplit.add(chapterName.substringBefore("章", chapterName).trim() + "章")
                val strAft = chapterName.substringAfter("章", chapterName).trim()
                if (strAft.isNotEmpty()){
                    chapterNameSplit.add(strAft)
                }
            } else {
                chapterNameSplit.add(chapterName)
            }

            if (chapterNameSplit.isNotEmpty()) {
                mchapterPaint.textSize = 16 * AppHelper.screenScaledDensity
                val chapterTitleList = getNovelText(mchapterPaint, chapterNameSplit[0], readerSettings.mWidth - AppHelper.screenDensity * 10)

                novelText.addAll(chapterTitleList)

                if (chapterNameSplit.size > 1) {
                    for (i in 1 until chapterNameSplit.size) {
                        mchapterPaint.textSize = 23 * AppHelper.screenScaledDensity
                        val chapterNameList = getNovelText(mchapterPaint, chapterNameSplit[i].trim { it <= ' ' }, readerSettings.mWidth - AppHelper.screenDensity * 10)
                        for (j in 0 until chapterNameList.size) {
                            novelText.add(chapterNameList[j])
                        }
                    }
                }
            }

        }

        var contentOffset = 0


        // 去除章节开头特殊符号
        if (content.startsWith(" \"")) {
            content = content.replaceFirst(" \"".toRegex(), "")
        } else if (content.startsWith("\"")) {
            content = content.replaceFirst("\"".toRegex(), "")
        }

        val contents = content.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (temp in contents) {
            var tmp = temp.replace("\\s+".toRegex(), "")
            if ("" != tmp) {
                sb.append("\u3000\u3000" + tmp + "\n")
            }
        }
        val text = sb.toString()
        if (ReaderStatus.position.offset > text.length) {
            ReaderStatus.position.offset = 0
        } else if (ReaderStatus.position.offset < 0) {
            ReaderStatus.position.offset = 0
        }

        val contentList = getNovelText(readerSettings.mPaint!!, text, readerSettings.mWidth)
        val size = contentList.size
        var textSpace = 0.0f
        var textLength: Long = 0
        var pageLines = ArrayList<NovelLineBean>()
        val lists = ArrayList<NovelPageBean>()


        var mNovelPageBean = NovelPageBean(pageLines, contentOffset, novelText)


        lists.add(mNovelPageBean)

        var chapterNameSize = novelText.size
        if (chapterNameSize > 1) {
            textSpace += chapterHeight
        }

        var lastLineHeight: Float
        for (i in 0..size - 1) {
            var isDuan = false
            val lineText = contentList[i]
            if (lineText.lineContent == " ") {// 段间距
                isDuan = true
                textSpace += m_duan
                lastLineHeight = m_duan
            } else if (lineText.lineContent == "chapter_homepage  ") {
                textSpace += hideHeight
                textLength += lineText.lineContent!!.length.toLong()
                lastLineHeight = hideHeight
            } else {
                textSpace += lineHeight
                textLength += lineText.lineContent!!.length.toLong()
                lastLineHeight = lineHeight
            }

            if (textSpace < height) {
                pageLines.add(lineText)
                contentOffset += lineText.lineContent?.length ?: 0
            } else {
                if (isDuan) {// 开始是空行
                    textSpace -= m_duan
                    pageLines.add(lineText)
                    contentOffset += lineText.lineContent?.length ?: 0
                } else {
                    pageLines = ArrayList<NovelLineBean>()
                    textSpace = lastLineHeight
                    pageLines.add(lineText)
                    contentOffset += lineText.lineContent?.length ?: 0

                    mNovelPageBean = NovelPageBean(pageLines, contentOffset, ArrayList<NovelLineBean>())

                    lists.add(mNovelPageBean)//
                }
                // }
            }
        }
        addLineIndexY(lists)
        return lists
    }

    fun separateBookName(): ArrayList<NovelLineBean>{
        var width = AppHelper.screenWidth - AppHelper.screenDensity * readerSettings.readContentPageLeftSpace * 2
        val mbookNamePaint = TextPaint()
        mbookNamePaint.isAntiAlias = true
        mbookNamePaint.textSize = readerSettings.FONT_BOOKNAME_DEFAULT * AppHelper.screenScaledDensity
        return getNovelText(mbookNamePaint, ReaderStatus.book.name, width)
    }

    private fun initPaint() {
        if (readerSettings.mPaint == null) {
            readerSettings.mPaint = Paint(Paint.FILTER_BITMAP_FLAG)
            readerSettings.mPaint!!.style = Paint.Style.FILL
            readerSettings.mPaint!!.isAntiAlias = true
            readerSettings.mPaint!!.isDither = true
        }
        readerSettings.mPaint!!.textSize = readerSettings.fontSize * AppHelper.screenScaledDensity

        readerSettings.mPaint!!.typeface = TypefaceUtil.loadTypeface(readerSettings.fontTypeface)

        val fm = readerSettings.mPaint!!.fontMetrics

        readerSettings.mLineSpace = readerSettings.readInterlineaSpace * readerSettings.fontSize.toFloat() * AppHelper.screenScaledDensity
        readerSettings.mFontHeight = fm.descent - fm.ascent + readerSettings.mLineSpace
        readerSettings.mDuan = readerSettings.readParagraphSpace * readerSettings.mLineSpace
    }

    private fun addLineIndexY(lists: ArrayList<NovelPageBean>?) {
        if (lists == null || lists.isEmpty()) {
            return
        }
        for (bean in lists) {
            val lineBeans = bean.lines
            if (lineBeans != null && !lineBeans.isEmpty()) {
                // 页的开始行是段间距,移除该段间距
                if (" " == lineBeans[0].lineContent && GLReaderView.AnimationType.LIST != readerSettings.animation) {
                    lineBeans.removeAt(0)
                }
                if (lineBeans[0].lineContent!!.startsWith("txtzsydsq_homepage")) {// 封面页
                } else if (lineBeans[0].lineContent!!.startsWith("chapter_homepage")) {// 章节首页
                    if (GLReaderView.AnimationType.LIST == readerSettings.animation) {
                        disVerticalFirstPage(bean)
                    } else {
                        disFirstPageHeight(bean)
                    }
                } else {// 章节内容
                    if (GLReaderView.AnimationType.LIST == readerSettings.animation) {
                        disVerticalPage(bean)
                    } else {
                        disPageHeight(bean)
                    }
                }
            }
        }
        if (lists.size == 1 && GLReaderView.AnimationType.LIST == readerSettings.animation) {
            lists[0].height = AppHelper.screenHeight.toFloat()
        }
    }

    private fun disVerticalFirstPage(bean: NovelPageBean) {
        val fm = readerSettings.mPaint!!.fontMetrics
        var total_y = -fm.ascent + 3f * 15f * AppHelper.screenScaledDensity + readerSettings.readContentPageTopSpace * AppHelper.screenDensity
        if (bean.chapterNameLines != null && bean.chapterNameLines.size > 1) {
            total_y += 75 * AppHelper.screenScaledDensity
        }
        val lineBeans = bean.lines
        var contentLength = 0
        for (b in lineBeans) {
            if (" " == b.lineContent) {
                total_y += readerSettings.mDuan
            } else if ("chapter_homepage  " != b.lineContent) {
                b.indexY = total_y
                total_y += readerSettings.mFontHeight
                contentLength += b.lineContent?.length ?: 0
            }
        }
        bean.height = total_y + fm.ascent
        bean.contentLength = contentLength
    }

    private fun disVerticalPage(bean: NovelPageBean) {
        val fm = readerSettings.mPaint!!.fontMetrics
        var total_y = -fm.ascent

        val lineBeans = bean.lines
        var contentLength = 0
        for (b in lineBeans) {
            if (" " == b.lineContent) {
                total_y += readerSettings.mDuan
            } else {
                b.indexY = total_y
                total_y += readerSettings.mFontHeight
                contentLength += b.lineContent?.length ?: 0
            }
        }
        bean.height = total_y + fm.ascent
        bean.contentLength = contentLength
    }

    private fun disFirstPageHeight(bean: NovelPageBean) {
        val fm = readerSettings.mPaint!!.fontMetrics
        var total_y = -fm.ascent + 3f * 15f * AppHelper.screenScaledDensity
        val font_height = fm.descent - fm.ascent
        var textHeight = 0f
        var duan = 0f
        val m_duan = readerSettings.mDuan
        var m_iFontHeight = readerSettings.mFontHeight
        var lastIsDuan = false
        val lineBeans = bean.lines
        if (lineBeans != null) {
            val size = lineBeans.size
            for (i in 0..size - 1) {
                val b = lineBeans[i]
                val text = b.lineContent
                if (!TextUtils.isEmpty(text) && text == " ") {
                    textHeight += m_duan
                    duan += m_duan
                    lastIsDuan = true
                } else if (text != "chapter_homepage  ") {
                    textHeight += m_iFontHeight
                    lastIsDuan = false
                }
            }

        }
        if (lastIsDuan) {
            total_y += m_duan
        }

        var height: Float

        height = AppHelper.screenHeight.toFloat() - AppHelper.screenDensity * readerSettings.readContentPageTopSpace.toFloat() * 2f - 3f * 15f * AppHelper.screenScaledDensity + readerSettings.mLineSpace
        total_y += readerSettings.readContentPageTopSpace * AppHelper.screenDensity


        if (bean.chapterNameLines != null && bean.chapterNameLines.size > 1) {
            total_y += 75 * AppHelper.screenScaledDensity
            height -= 75 * AppHelper.screenScaledDensity
        }
        if (height - textHeight > 2 && height - textHeight < 3 * (fm.descent - fm.ascent)) {
            val distanceExtra = height - textHeight
            total_y += distanceExtra
        } else if (textHeight - height > 2) {
            val n = Math.floor(((height - duan) / m_iFontHeight).toDouble()).toInt()// 行数
            val distance = (textHeight - height) / n
            m_iFontHeight = fm.descent - fm.ascent + readerSettings.readInterlineaSpace * readerSettings.fontSize.toFloat() * AppHelper.screenScaledDensity - distance
        }
        var contentLength = 0
        for (b in lineBeans) {
            if (" " == b.lineContent) {
                total_y += m_duan
            } else if ("chapter_homepage  " != b.lineContent) {
                b.indexY = total_y
                total_y += m_iFontHeight
                contentLength += b.lineContent?.length ?: 0
            }
        }
        bean.height = total_y + fm.ascent
        bean.contentLength = contentLength
    }

    private fun disPageHeight(bean: NovelPageBean) {
        var textHeight = 0f
        var duan = 0f//段落
        var lastIsDuan = false
        var m_iFontHeight = readerSettings.mFontHeight
        var m_duan = readerSettings.mDuan
        val height = AppHelper.screenHeight - AppHelper.screenDensity * readerSettings.readContentPageTopSpace.toFloat() * 2f + readerSettings.mLineSpace

        // 计算页字符内容所占的高度
        var lineBeans = bean.lines
        val size = lineBeans.size
        for (i in 0..size - 1) {
            val text = lineBeans[i].lineContent
            if (!TextUtils.isEmpty(text) && text == " ") {
                textHeight += m_duan
                duan += m_duan
                lastIsDuan = true//最后一段画一半，另一半画下页
            } else {
                textHeight += m_iFontHeight
                lastIsDuan = false
            }
        }

        if (lastIsDuan) {
            textHeight -= m_duan
        }

        val fm = readerSettings.mPaint!!.fontMetrics

        if (height - textHeight > 2 && height - textHeight < 4 * (fm.descent - fm.ascent)) {
            val numLine = Math.round((height - duan) / m_iFontHeight).toInt()// 行数
            val numDuan = Math.round(duan / m_duan).toInt()// 段间距数
            val distanceExtra = height - textHeight//
            val distanceDuan = duan * distanceExtra / height
            val distanceLine = distanceExtra - distanceDuan
            m_iFontHeight = m_iFontHeight + distanceLine / numLine
            m_duan = m_duan + distanceDuan / numDuan
        } else if (textHeight - height > 2) {
            val n = Math.round((height - duan) / m_iFontHeight).toInt()// 行数 21
            val distance = (textHeight - height) / n
            m_iFontHeight = m_iFontHeight - distance
        }

        var total_y = readerSettings.readContentPageTopSpace * AppHelper.screenDensity - fm.ascent
        var contentLength = 0
        for (b in lineBeans) {
            if (" " == b.lineContent) {
                total_y += m_duan
            } else {
                b.indexY = total_y
                total_y += m_iFontHeight
                contentLength += b.lineContent?.length ?: 0
            }
        }
        bean.height = total_y + fm.ascent
        bean.contentLength = contentLength
    }

    /**
     * getNovelText
     * 划分章节内容
     * textPaint
     * text
     * width
     * 设定文件
     * ArrayList<String> 返回类型
    </String> */
    private fun getNovelText(textPaint: Paint, text: String?, width: Float): ArrayList<NovelLineBean> {
        val list = ArrayList<NovelLineBean>()
        var charWidths = ArrayList<Float>()
        charWidths.add(0.0f)
        var w = 0f
        var istart = 0
        var mChar: Char
        val widths = FloatArray(1)
        val chineseWidth = FloatArray(1)
        textPaint.getTextWidths("正", chineseWidth)
        ReadConstants.chineseWth = chineseWidth[0]
        val wordSpace = chineseWidth[0] / 2
        if (text == null) {
            return list
        }
        var duan_coount = 0
        var i = 0
        while (i < text.length) {
            mChar = text[i]
            if (mChar == '\n') {
                widths[0] = 0f
            } else if (isChinese(mChar) || mChar == '，' || mChar == '。') {
                widths[0] = chineseWidth[0]
            } else {
                val srt = mChar.toString()
                textPaint.getTextWidths(srt, widths)
            }
            if (mChar == '\n') {
                duan_coount++
                val txt = text.substring(istart, i)
                if ("" != txt) {
                    list.add(NovelLineBean(text.substring(istart, i) + " ", w, 0, false, charWidths))
                }
                if (duan_coount > 3) {
                    list.add(NovelLineBean(" ", w, 0, false, charWidths))// 段间距
                }
                istart = i + 1
                w = 0f
                charWidths = ArrayList<Float>()
                charWidths.add(0.0f)
            } else {
                w += widths[0]
                charWidths.add(w)
                if (w > width - wordSpace) {
                    val lineWth = w - widths[0]
                    if (checkIsPunct(mChar)) {
                        // 下一行开始字符为标点的处理
                        // 将标点移动到本行
                        // 为标点分配宽度 => 半个中文字符宽度
                        // 为了保证移动后的文本宽度不超出文本绘制的理论宽度 width , 需满足行间距 wordSpace >= chineseWidth[0] / 2
                        val substring = text.substring(istart, i)
                        list.add(NovelLineBean(substring + mChar, lineWth + chineseWidth[0] / 2, 1, true, charWidths))
                        istart = i + 1
                    } else {
                        if (lastIsPunct(text, i)) {
                            // 本行的结束字符为标点的处理
                            // 为标点分配宽度 => 半个中文字符宽度
                            // 结束字符为'"'时需单独处理
                            list.add(NovelLineBean(text.substring(istart, i), lineWth - chineseWidth[0] / 2, 1, true, charWidths))
                        } else {
                            list.add(NovelLineBean(text.substring(istart, i), lineWth, 1, false, charWidths))
                        }
                        istart = i
                        i--
                    }
                    w = 0f
                    charWidths = ArrayList<Float>()
                    charWidths.add(0.0f)
                } else {
                    if (i == text.length - 1) {
                        list.add(NovelLineBean(text.substring(istart, text.length), w, 0, false, charWidths))
                    }
                }
            }
            i++
        }
        return list
    }


    private fun checkIsPunct(ch: Char): Boolean {
        var isInclude = false
        for (c in ReadConstants.puncts) {
            if (ch == c) {
                isInclude = true
                break
            }
        }
        return isInclude
    }

    private fun lastIsPunct(text: String, i: Int): Boolean {
        if (i > 0) {
            val ch = text[i - 1]
            if (ch == '”') {
                return false
            }
            if (checkIsPunct(ch)) {
                return true
            }
        }
        return false
    }

    fun getCurrentPage(offest: Int, currentChapter: ArrayList<NovelPageBean>): Int {
        val filter = currentChapter.filter {
            it.offset <= offest
        }
        return if(filter.size - 1 > 0) return filter.size - 1 else 1
    }

    fun getChapterNameList(chapterName: String): ArrayList<NovelLineBean> {
        val splitTag = "章"
        val newChapterList = ArrayList<NovelLineBean>()

        if (TextUtils.isEmpty(chapterName)) {
            newChapterList.add(NovelLineBean("无章节名", 0f, 0, false, null))
            return newChapterList
        }

        if (chapterName.contains(splitTag)) {
            val chapterNumAndName = chapterName.split(splitTag.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in chapterNumAndName.indices) {
                if (i == 0) {
                    newChapterList.add(NovelLineBean(chapterNumAndName[i] + splitTag, 0f, 0, false, null))
                } else {
                    newChapterList.add(NovelLineBean(chapterNumAndName[i].trim { it <= ' ' }, 0f, 0, false, null))
                }
            }
        } else {
            newChapterList.add(NovelLineBean(chapterName + splitTag, 0f, 0, false, null))
            newChapterList.add(NovelLineBean(chapterName, 0f, 0, false, null))
        }

        return newChapterList
    }

    /**
     * 字符是否为中文
     */
    fun isChinese(c: Char): Boolean {
        val ub = Character.UnicodeBlock.of(c)
        return if (ub === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub === Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub === Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            true
        } else false
    }
}