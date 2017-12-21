package com.intelligent.reader.read.help;

import android.graphics.Paint;
import android.text.TextPaint;
import android.text.TextUtils;

import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.NovelLineBean;
import net.lzbook.kit.data.bean.ReadStatus;
import net.lzbook.kit.utils.Tools;

import java.util.ArrayList;

/**
 * 分页帮助类
 * Created by wt on 2017/12/20.
 */

public class ReadSeparateHelper {

    private ReadStatus readStatus;

    private ReadSeparateHelper (){
    }
    private static class Singleton {
        private static final ReadSeparateHelper INSTANCE = new ReadSeparateHelper();
    }

    public static final ReadSeparateHelper getInstance(ReadStatus mReadStatus) {
        Singleton.INSTANCE.readStatus = mReadStatus;
        return Singleton.INSTANCE;
    }

    public ArrayList<ArrayList<NovelLineBean>> initTextSeparateContent(String content) {
        float chapterHeight = 75 * readStatus.screenScaledDensity;
        float hideHeight = 15 * readStatus.screenScaledDensity;

        TextPaint mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(Constants.FONT_SIZE * readStatus.screenScaledDensity);
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();

        TextPaint mchapterPaint = new TextPaint();
        mchapterPaint.setTextSize(20 * readStatus.screenScaledDensity);

        TextPaint mbookNamePaint = new TextPaint();
        mbookNamePaint.setAntiAlias(true);
        mbookNamePaint.setTextSize(Constants.FONT_CHAPTER_SIZE * readStatus.screenScaledDensity);

        // 显示文字区域高度
        float height = readStatus.screenHeight - readStatus.screenDensity * Constants.READ_CONTENT_PAGE_TOP_SPACE * 2;

        float width = readStatus.screenWidth - readStatus.screenDensity * Constants.READ_CONTENT_PAGE_LEFT_SPACE * 2;

        float lineSpace = Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE * readStatus.screenScaledDensity;
        float lineHeight = fm.descent - fm.ascent + lineSpace;
        float m_duan = Constants.READ_PARAGRAPH_SPACE * lineSpace;

        if (Constants.IS_LANDSCAPE) {
            width = readStatus.screenWidth - readStatus.screenDensity * Constants.READ_CONTENT_PAGE_LEFT_SPACE * 2;
        }

        // 添加转换提示
        StringBuilder sb = new StringBuilder();
        if (readStatus.sequence != -1) {
            sb.append("chapter_homepage \n");
            sb.append("chapter_homepage \n");
            sb.append("chapter_homepage \n");

            if (!TextUtils.isEmpty(readStatus.chapterName)) {
                readStatus.chapterNameList = getNovelText(mchapterPaint, readStatus.chapterName, width
                        - readStatus.screenDensity * 10);
            }

            String[] chapterNumAndName = readStatus.chapterNameList.get(0).getLineContent().split("章");
            ArrayList<NovelLineBean> newChapterList = new ArrayList<>();

            for (int i = 0; i < chapterNumAndName.length; i++) {
                if (i == 0) {
                    newChapterList.add(new NovelLineBean(chapterNumAndName[i] + "章", 0, 0, false, null));
                } else {
                    newChapterList.add(new NovelLineBean(chapterNumAndName[i].trim(), 0, 0, false, null));
                }
            }
            if (readStatus.chapterNameList.size() > 1) {
                readStatus.chapterNameList.remove(0);
                newChapterList.addAll(readStatus.chapterNameList);
            }
            readStatus.chapterNameList = newChapterList;

            if (readStatus.chapterNameList.size() > 2) {
                ArrayList<NovelLineBean> temp = new ArrayList<NovelLineBean>();
                for (int i = 0; i < 2; i++) {
                    temp.add(readStatus.chapterNameList.get(i));
                }
                readStatus.chapterNameList = temp;
            }
        }

        // 去除章节开头特殊符号
        if (content.startsWith(" \"")) {
            content = content.replaceFirst(" \"", "");
        } else if (content.startsWith("\"")) {
            content = content.replaceFirst("\"", "");
        }

        String[] contents = content.split("\n");
        for (String temp : contents) {
            temp = temp.replaceAll("\\s+", "");
            if (!"".equals(temp)) {
                sb.append("\u3000\u3000" + temp + "\n");
            }
        }
        String text = "";
        if (readStatus.sequence == -1) {
            readStatus.bookNameList = getNovelText(mbookNamePaint, readStatus.bookName, width);
            String homeText = "txtzsydsq_homepage\n";
            StringBuilder s = new StringBuilder();
            s.append(homeText);
            s.append(sb);
            text = s.toString();
        } else {
            text = sb.toString();
        }
        if (readStatus.offset > text.length()) {
            readStatus.offset = 0;
        } else if (readStatus.offset < 0) {
            readStatus.offset = 0;
        }

        ArrayList<NovelLineBean> contentList = getNovelText(mTextPaint, text, width);
        final int size = contentList.size();
        float textSpace = 0.0f;
        long textLength = 0;
        boolean can = true;
        ArrayList<NovelLineBean> pageLines = new ArrayList<NovelLineBean>();
        ArrayList<ArrayList<NovelLineBean>> lists = new ArrayList<ArrayList<NovelLineBean>>();
        lists.add(pageLines);
        int chapterNameSize = 0;
        if (readStatus.chapterNameList != null) {
            chapterNameSize = readStatus.chapterNameList.size();
        }
        if (chapterNameSize > 1) {
            textSpace += chapterHeight;
        }

        float lastLineHeight;
        for (int i = 0; i < size; i++) {
            boolean isDuan = false;
            NovelLineBean lineText = contentList.get(i);
            if (lineText.getLineContent().equals(" ")) {// 段间距
                isDuan = true;
                textSpace += m_duan;
                lastLineHeight = m_duan;
            } else if (lineText.getLineContent().equals("chapter_homepage  ")) {
                textSpace += hideHeight;
                textLength += lineText.getLineContent().length();
                lastLineHeight = hideHeight;
            } else {
                textSpace += lineHeight;
                textLength += lineText.getLineContent().length();
                lastLineHeight = lineHeight;
            }

            if (textSpace < height) {
                pageLines.add(lineText);
            } else {
                if (isDuan) {// 开始是空行
                    textSpace -= m_duan;
                    pageLines.add(lineText);
                } else {
                    pageLines = new ArrayList<NovelLineBean>();
                    textSpace = lastLineHeight;
                    pageLines.add(lineText);
                    lists.add(pageLines);
                }
                // }
            }
            if (textLength >= readStatus.offset && can) {
                readStatus.currentPage = lists.size();
                can = false;
            }
        }
        readStatus.pageCount = lists.size();
        if (readStatus.currentPage == 0) {
            readStatus.currentPage = 1;
        }
        return lists;
    }

    /**
     * getNovelText
     * 划分章节内容
     * textPaint
     * text
     * width
     * 设定文件
     * ArrayList<String> 返回类型
     */
    private ArrayList<NovelLineBean> getNovelText(TextPaint textPaint, String text, float width) {
        ArrayList<NovelLineBean> list = new ArrayList<NovelLineBean>();
        ArrayList<Float> charWidths = new ArrayList<Float>();
        charWidths.add(0.0f);
        float w = 0;
        int istart = 0;
        char mChar;
        float[] widths = new float[1];
        float[] chineseWidth = new float[1];
        textPaint.getTextWidths("正", chineseWidth);
        ReadConstants.chineseWth = chineseWidth[0];
        float wordSpace = chineseWidth[0] / 2;
        if (text == null) {
            return list;
        }
        int duan_coount = 0;
        for (int i = 0; i < text.length(); i++) {
            mChar = text.charAt(i);
            if (mChar == '\n') {
                widths[0] = 0;
            } else if (Tools.isChinese(mChar) || mChar == '，' || mChar == '。') {
                widths[0] = chineseWidth[0];
            } else {
                String srt = String.valueOf(mChar);
                textPaint.getTextWidths(srt, widths);
            }
            if (mChar == '\n') {
                duan_coount++;
                String txt = text.substring(istart, i);
                if (!"".equals(txt)) {
                    list.add(new NovelLineBean(text.substring(istart, i) + " ", w, 0, false, charWidths));
                }
                if (duan_coount > 3) {
                    list.add(new NovelLineBean(" ", w, 0, false, charWidths));// 段间距
                }
                istart = i + 1;
                w = 0;
                charWidths = new ArrayList<Float>();
                charWidths.add(0.0f);
            } else {
                w += widths[0];
                charWidths.add(w);
                if (w > width - wordSpace) {
                    float lineWth = w - widths[0];
                    if (checkIsPunct(mChar)) {
                        // 下一行开始字符为标点的处理
                        // 将标点移动到本行
                        // 为标点分配宽度 => 半个中文字符宽度
                        // 为了保证移动后的文本宽度不超出文本绘制的理论宽度 width , 需满足行间距 wordSpace >= chineseWidth[0] / 2
                        String substring = text.substring(istart, i);
                        list.add(new NovelLineBean(substring + mChar, lineWth + chineseWidth[0] / 2, 1, true, charWidths));
                        istart = i + 1;
                    } else {
                        if (lastIsPunct(text, i)) {
                            // 本行的结束字符为标点的处理
                            // 为标点分配宽度 => 半个中文字符宽度
                            // 结束字符为'"'时需单独处理
                            list.add(new NovelLineBean(text.substring(istart, i), lineWth - chineseWidth[0] / 2, 1, true, charWidths));
                        } else {
                            list.add(new NovelLineBean(text.substring(istart, i), lineWth, 1, false, charWidths));
                        }
                        istart = i;
                        i--;
                    }
                    w = 0;
                    charWidths = new ArrayList<Float>();
                    charWidths.add(0.0f);
                } else {
                    if (i == (text.length() - 1)) {
                        list.add(new NovelLineBean(text.substring(istart, text.length()), w, 0, false, charWidths));
                    }
                }
            }
        }
        return list;
    }


    private boolean checkIsPunct(char ch) {
        boolean isInclude = false;
        for (char c : ReadConstants.puncts) {
            if (ch == c) {
                isInclude = true;
                break;
            }
        }
        return isInclude;
    }

    private boolean lastIsPunct(String text, int i) {
        if (i > 0) {
            char ch = text.charAt(i - 1);
            if (ch == '”') {
                return false;
            }
            if (checkIsPunct(ch)) {
                return true;
            }
        }
        return false;
    }
}
