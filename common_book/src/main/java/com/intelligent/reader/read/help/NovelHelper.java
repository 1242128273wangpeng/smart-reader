package com.intelligent.reader.read.help;

import com.intelligent.reader.R;
import com.intelligent.reader.adapter.SourceAdapter;
import com.intelligent.reader.read.page.PageContentView;
import com.intelligent.reader.read.page.PageInterface;
import com.intelligent.reader.reader.ReaderViewModel;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.download.DownloadState;
import net.lzbook.kit.book.view.MyDialog;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.constants.ReadConstants;
import net.lzbook.kit.data.NullCallBack;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.BookTask;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.NovelLineBean;
import net.lzbook.kit.data.bean.ReadStatus;
import net.lzbook.kit.data.bean.Source;
import net.lzbook.kit.data.db.BookChapterDao;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.BaseBookHelper;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.StatServiceUtils;
import net.lzbook.kit.utils.Tools;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint.FontMetrics;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 阅读页工具类
 */
public class NovelHelper {
    public static final String empty_page_ad = "empty_page_ad";
    public static final String empty_page_ad_inChapter = "empty_inChapter_ad";
    private static final String TAG = "NovelHelper";
    public boolean isShown = false;
    private OnHelperCallBack helperCallBack;
    private WeakReference<Activity> actReference;
    private ReadStatus readStatus;
    private PageInterface pageView;

    public NovelHelper(Activity activity, ReadStatus readStatus) {
        this.actReference = new WeakReference<>(activity);
        this.readStatus = readStatus;
    }

    public void setPageView(PageInterface pageView) {
        this.pageView = pageView;
    }

    // 提示自动阅读
    public void showHintAutoReadDialog() {
        final Activity activity = actReference.get();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (AppUtils.getBooleanPreferences(activity, "auto_read_hint", false)) {
            return;
        }
        final MyDialog myDialog = new MyDialog(activity, R.layout.layout_addshelf_dialog);
        TextView dialog_title = (TextView) myDialog.findViewById(R.id.dialog_title);
        dialog_title.setText(R.string.prompt);
        CheckBox cb_hint = (CheckBox) myDialog.findViewById(R.id.cb_hint);
        Button bt_cancel = (Button) myDialog.findViewById(R.id.bt_cancel);
        bt_cancel.setText(R.string.cancel);
        Button bt_ok = (Button) myDialog.findViewById(R.id.bt_ok);
        bt_ok.setText(R.string.confirm);
        TextView tv_update_info_dialog = (TextView) myDialog.findViewById(R.id.tv_update_info_dialog);
        tv_update_info_dialog.setText(R.string.auto_reading_prompt);
        tv_update_info_dialog.setGravity(Gravity.CENTER);
        cb_hint.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.rb_click_flip_auto_not_tip);
                AppUtils.setBooleanPreferences(activity, "auto_read_hint", isChecked);
            }
        });
        bt_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.rb_click_flip_auto_cancel);
                if (myDialog != null) {
                    try {
                        myDialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        bt_ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.rb_click_flip_auto_ok);
                if (myDialog != null) {
                    try {
                        myDialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (helperCallBack != null) {
                    helperCallBack.openAutoReading(true);
                }
            }
        });
        if (myDialog != null && !myDialog.isShowing() && !activity.isFinishing()) {
            try {
                myDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 退出阅读 提示添加到书架
    public void showAndBookShelfDialog() {
        final Activity activity = actReference.get();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (AppUtils.getBooleanPreferences(activity, "exit_hint", false)) {
            if (helperCallBack != null) {
                helperCallBack.addBookShelf(false);
            }
            return;
        }

        final MyDialog myDialog = new MyDialog(activity, R.layout.publish_hint_dialog);
        TextView tv_update_info_dialog = (TextView) myDialog.findViewById(R.id.publish_content);
        TextView dialog_title = (TextView) myDialog.findViewById(R.id.dialog_title);
        dialog_title.setText(R.string.prompt);
        tv_update_info_dialog.setText("喜欢就加入书架吧！");
        tv_update_info_dialog.setGravity(Gravity.CENTER);
        Button bt_cancel = (Button) myDialog.findViewById(R.id.publish_stay);
        Button bt_ok = (Button) myDialog.findViewById(R.id.publish_leave);
        bt_ok.setText("加入书架");
        bt_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myDialog != null) {
                    try {


                        myDialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (helperCallBack != null) {
                    helperCallBack.addBookShelf(false);
                }
            }
        });
        bt_ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myDialog != null) {
                    try {
                        myDialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (helperCallBack != null) {
                    helperCallBack.addBookShelf(true);
                }
            }
        });
        if (myDialog != null && !myDialog.isShowing() && !activity.isFinishing()) {
            try {
                myDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void showSourceDialog(final ReaderViewModel mReaderViewModel, String curl, final ArrayList<Source> sources) {
        if (actReference == null || actReference.get() == null || actReference.get().isFinishing()) {
            return;
        }
        final Activity activity = actReference.get();
        final MyDialog sourceDialog = new MyDialog(activity, R.layout.dialog_read_source, Gravity.CENTER);
        sourceDialog.setCanceledOnTouchOutside(true);
        TextView change_source_disclaimer_message = (TextView) sourceDialog.findViewById(R.id.change_source_disclaimer_message);
        TextView change_source_original_web = (TextView) sourceDialog.findViewById(R.id.change_source_original_web);
        TextView change_source_continue = (TextView) sourceDialog.findViewById(R.id.change_source_continue);

        if (mReaderViewModel != null && mReaderViewModel.getCurrentChapter() != null && mReaderViewModel.getCurrentChapter().status != Chapter.Status.CONTENT_NORMAL) {
            change_source_disclaimer_message.setText(mReaderViewModel.getCurrentChapter().status.tips);
            change_source_original_web.setVisibility(View.INVISIBLE);
            change_source_continue.setText(R.string.jump_next_chapter);
        }

        ListView change_source_list = (ListView) sourceDialog.findViewById(R.id.change_source_list);

        final SourceAdapter sourceListAdapter = new SourceAdapter(activity, sources);
        change_source_list.setAdapter(sourceListAdapter);

        if (Constants.IS_LANDSCAPE) {
            if (sources.size() > 1) {
                change_source_list.getLayoutParams().height = activity.getResources().getDimensionPixelOffset(R.dimen.dimen_view_height_80);
            }
        } else {
            if (sources.size() > 3) {
                change_source_list.getLayoutParams().height = activity.getResources().getDimensionPixelOffset(R.dimen.dimen_view_height_135);
            }
        }

        change_source_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Source source = (Source) sourceListAdapter.getItem(position);

                if (source != null && !TextUtils.isEmpty(source.host)) {
                    helperCallBack.showCatalogActivity(source);
                }

                Map<String, String> map1 = new HashMap<String, String>();
                map1.put("type", "2");
                StartLogClickUtil.upLoadEventLog(actReference.get(), StartLogClickUtil.READPAGEMORE_PAGE, StartLogClickUtil.READ_SOURCECHANGE, map1);
                dismissDialog(sourceDialog);
            }
        });


        change_source_original_web.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> map1 = new HashMap<String, String>();
                map1.put("type", "1");
                StartLogClickUtil.upLoadEventLog(actReference.get(), StartLogClickUtil.READPAGEMORE_PAGE, StartLogClickUtil.READ_SOURCECHANGE, map1);
                dismissDialog(sourceDialog);
            }
        });
        change_source_continue.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, String> map1 = new HashMap<String, String>();
                map1.put("type", "1");
                StartLogClickUtil.upLoadEventLog(actReference.get(), StartLogClickUtil.READPAGEMORE_PAGE, StartLogClickUtil.READ_SOURCECHANGE, map1);
                dismissDialog(sourceDialog);
                if (mReaderViewModel != null && mReaderViewModel.getCurrentChapter() != null && mReaderViewModel.getCurrentChapter().status != Chapter.Status.CONTENT_NORMAL) {
                    if (helperCallBack != null) {
                        helperCallBack.jumpNextChapter();
                    }
                }
            }
        });
        if (!sourceDialog.isShowing()) {
            try {
                sourceDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void dismissDialog(MyDialog sourceDialog) {
        if (sourceDialog != null && sourceDialog.isShowing()) {
            sourceDialog.dismiss();
        }
    }

    /**
     * 点击下载按钮
     * <p/>
     * context
     * gid
     * mBook
     * sequence
     */
    public void clickDownload(final Context context, final Book mBook, final int sequence) {

        BookTask bookTask = BookHelper.getDownBookTask(context, mBook.book_id);
        if (bookTask != null && BookHelper.getStartDownIndex(context, mBook) > -1) {
            if (bookTask.state == DownloadState.DOWNLOADING) {
                Toast.makeText(context, "请耐心等待，已存在缓存队列", Toast.LENGTH_SHORT).show();
                return;
            } else if (bookTask.state == DownloadState.WAITTING || bookTask.state == DownloadState.NOSTART
                    || bookTask.state == DownloadState.PAUSEED || bookTask.state == DownloadState.REFRESH
                    || bookTask.state == DownloadState.LOCKED) {
                BookHelper.startDownBookTask(context, mBook.book_id);

                DownloadState downloadState = BookHelper.getDownloadState(context, mBook);
                if (downloadState != DownloadState.FINISH && downloadState != DownloadState.WAITTING && downloadState != DownloadState.DOWNLOADING) {
                    Toast.makeText(context, "马上开始为你缓存。。。", Toast.LENGTH_SHORT).show();
                }


                return;
            } else if (bookTask.state == DownloadState.FINISH) {
                Toast.makeText(context, "离线缓存已完成", Toast.LENGTH_SHORT).show();
            }
        } else {
            Activity activity = actReference.get();
            if (activity == null) {
                return;
            }
            final MyDialog dialog = new MyDialog(activity, R.layout.reading_cache, Gravity.BOTTOM, true);
            TextView reading_all_down = (TextView) dialog.findViewById(R.id.reading_all_down);
            reading_all_down.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_download_all);
                    if (NetWorkUtils.getNetWorkType(context) == NetWorkUtils.NETWORK_NONE) {
                        Toast.makeText(context, context.getText(R.string.game_network_none), Toast.LENGTH_LONG).show();
                        return;
                    }
                    BookHelper.addDownBookTask(context, mBook, new NullCallBack(), true);
                    BookHelper.startDownBookTask(context, mBook.book_id);
                    BookHelper.writeDownIndex(context, mBook.book_id, false, 0);
                    dialog.dismiss();
                    Toast.makeText(context, R.string.reading_cache_hint, Toast.LENGTH_SHORT).show();
                }
            });
            TextView reading_current_down = (TextView) dialog.findViewById(R.id.reading_current_down);
            reading_current_down.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_download_from_now);
                    if (NetWorkUtils.getNetWorkType(context) == NetWorkUtils.NETWORK_NONE) {
                        Toast.makeText(context, context.getText(R.string.game_network_none), Toast.LENGTH_LONG).show();
                        return;
                    }
                    BookHelper.addDownBookTask(context, mBook, new NullCallBack(), false);
                    BookHelper.startDownBookTask(context, mBook.book_id, sequence < 0 ? 0 : sequence);
                    BookHelper.writeDownIndex(context, mBook.book_id, true, sequence < 0 ? 0 : sequence);
                    dialog.dismiss();
                    //					if (mBook.is_vip == 1) {
                    //					} else {
                    Toast.makeText(context, R.string.reading_cache_hint, Toast.LENGTH_SHORT).show();
                    //					}
                }
            });
            TextView cancel = (TextView) dialog.findViewById(R.id.reading_cache_cancel);

            cancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_download_cancel);
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            });
            dialog.show();
        }
    }

    public ArrayList<ArrayList<NovelLineBean>> initTextContent2(String content) {
        float chapterHeight = 75 * readStatus.screenScaledDensity;
        float hideHeight = 15 * readStatus.screenScaledDensity;

        TextPaint mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(Constants.FONT_SIZE * readStatus.screenScaledDensity);
        FontMetrics fm = mTextPaint.getFontMetrics();

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


    /**
     * 保存书签
     */
    public void saveBookmark(ArrayList<Chapter> chapterList, String book_id, int sequence, int offset,
                             BookDaoHelper mBookDaoHelper) {
        if (chapterList == null || actReference.get() == null) {
            return;
        }

        Book book = new Book();
        book.book_id = book_id;
        book.sequence = sequence;
        book.offset = offset;
        book.sequence_time = System.currentTimeMillis();
        if (mBookDaoHelper.isBookSubed(book.book_id)) {
            book.readed = 1;
        }

        AppLog.e(TAG, "保存书籍阅读状态: " + book.toString());

        mBookDaoHelper.updateBook(book);

        AppLog.e(TAG, "保存书籍阅读状态: " + mBookDaoHelper.getBook(book_id, 0));

        AppLog.e(TAG, "保存书籍阅读状态: " + new BookChapterDao(actReference.get(), book_id).getCount());


    }

    public synchronized List<NovelLineBean> getPageContent() {
        if (readStatus.mLineList == null) {
            return null;
        }
        if (readStatus.currentPage == 0) {
            readStatus.currentPage = 1;
        }
        if (readStatus.currentPage > readStatus.pageCount) {
            readStatus.currentPage = readStatus.pageCount;
        }
        readStatus.offset = 0;
        // AppLog.d("initTextContent2", "readStatus.currentPage:" +
        // readStatus.currentPage);
        ArrayList<NovelLineBean> pageContent = null;
        if (readStatus.currentPage - 1 < readStatus.mLineList.size()) {
            pageContent = readStatus.mLineList.get(readStatus.currentPage - 1);
        } else {
            pageContent = new ArrayList<NovelLineBean>();
        }

        for (int i = 0; i < readStatus.currentPage - 1 && i < readStatus.mLineList.size(); i++) {
            ArrayList<NovelLineBean> pageList = readStatus.mLineList.get(i);
            final int size = pageList.size();
            // AppLog.d("initTextContent2", "size:" + size);
            for (int j = 0; j < size; j++) {
                NovelLineBean string = pageList.get(j);
                if (string != null && !TextUtils.isEmpty(string.getLineContent())) {
                    readStatus.offset += string.getLineContent().length();
                }
            }
        }
        readStatus.currentPageConentLength = 0;
        for (int i = 0; i < pageContent.size(); i++) {
            readStatus.currentPageConentLength += pageContent.get(i).getLineContent().length();

        }
        AppLog.e("总字数", "===" + readStatus.currentPage + "++" + readStatus.currentPageConentLength);
        readStatus.offset++;
        return pageContent;

    }

    public synchronized void getPageContentScroll() {
        if (readStatus.mLineList == null) {
            return;
        }
        if (readStatus.currentPage == 0) {
            readStatus.currentPage = 1;
        }
        if (readStatus.currentPage > readStatus.pageCount) {
            readStatus.currentPage = readStatus.pageCount;
        }
        readStatus.offset = 0;
        int count = readStatus.mLineList.size();
        // AppLog.d("initTextContent2", "readStatus.currentPage:" +
        // readStatus.currentPage);
        for (int i = 0; i < readStatus.currentPage - 1 && i < count; i++) {
            ArrayList<NovelLineBean> pageList = readStatus.mLineList.get(i);
            final int size = pageList.size();
            // AppLog.d("initTextContent2", "size:" + size);
            for (int j = 0; j < size; j++) {
                NovelLineBean string = pageList.get(j);
                if (string != null && !TextUtils.isEmpty(string.getLineContent())) {
                    readStatus.offset += string.getLineContent().length();
                }
            }
        }
        readStatus.offset++;
    }

    /**
     * 处理章节内容
     * <p/>
     * context
     * currentChapter
     * mBook
     *
     * @return true: NORMAL  false: ERROR
     */
    public synchronized boolean getChapterContent(Activity activity, Chapter currentChapter, Book mBook) {
        if (mBook == null || readStatus == null)
            return false;
        // 更新chapter状态
        BaseBookHelper.setChapterStatus(currentChapter);
        if (currentChapter != null && readStatus != null) {
            if (currentChapter.status != Chapter.Status.CONTENT_NORMAL) {
//                handler.sendEmptyMessage(5);
                if (helperCallBack != null) {
                    helperCallBack.changSource();
                }
            }
            readStatus.chapterName = currentChapter.chapter_name;
        }
        if (readStatus != null) {
            readStatus.bookName = mBook.name;
            readStatus.bookAuthor = mBook.author;
            //  小说来源
            readStatus.bookSource = mBook.name;
        }

        if (currentChapter != null) {
            switch (currentChapter.status) {
                case CONTENT_EMPTY:
                case CONTENT_ERROR:
                case SOURCE_ERROR:
                    if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE && mBook.book_type == 0) {
                        Toast.makeText(activity, R.string.err_no_net, Toast.LENGTH_SHORT).show();
//                        readStatus.mLineList = initTextContent2("");
                    }
//                    readStatus.mLineList = initTextContent2("");
                    return true;
                case CONTENT_NORMAL:
//                    readStatus.mLineList = initTextContent2(currentChapter.content);
                    return true;
                default:
                    break;
            }
        }
        return false;
    }

    public ArrayList<NovelLineBean> getChapterNameList(String chapterName) {
        String splitTag = "章";
        ArrayList<NovelLineBean> newChapterList = new ArrayList<>();

        if (TextUtils.isEmpty(chapterName)) {
            newChapterList.add(new NovelLineBean("无章节名", 0, 0, false, null));
            return newChapterList;
        }

        if (chapterName.contains(splitTag)) {
            String[] chapterNumAndName = chapterName.split(splitTag);
            for (int i = 0; i < chapterNumAndName.length; i++) {
                if (i == 0) {
                    newChapterList.add(new NovelLineBean(chapterNumAndName[i] + splitTag, 0, 0, false, null));
                } else {
                    newChapterList.add(new NovelLineBean(chapterNumAndName[i].trim(), 0, 0, false, null));
                }
            }
        } else {
            newChapterList.add(new NovelLineBean(chapterName + splitTag, 0, 0, false, null));
            newChapterList.add(new NovelLineBean(chapterName, 0, 0, false, null));
        }

        return newChapterList;
    }

    public int getPageHeight(List<NovelLineBean> pageLines) {
        TextPaint contentPaint = new TextPaint();
        TextPaint duanPaint = new TextPaint();
        TextPaint textPaint = new TextPaint();
        contentPaint.setTextSize(Constants.FONT_SIZE * readStatus.screenScaledDensity);
        duanPaint.setTextSize(1 * readStatus.screenScaledDensity);
        textPaint.setTextSize(Constants.FONT_CHAPTER_SIZE * readStatus.screenScaledDensity);
        int chapterBetweenHeight = (int) (100 * readStatus.screenScaledDensity);
        boolean lastPage = false;

        // 页面总高度
        int pageHeight = 0;

        // 章节文字Fm
        FontMetrics chapterFm;
        // 章节文字高度
        float chapterFontHeight = 0;

        // 正文文字Fm
        FontMetrics contentFm = contentPaint.getFontMetrics();
        float lineSpace = Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE * readStatus.screenScaledDensity;
        // 正文文字高度
        float contentHeight = contentFm.descent - contentFm.ascent + lineSpace;
        // 段落高度
        float paragraphHeight = Constants.READ_PARAGRAPH_SPACE * lineSpace;

        // 正文绘制起始高度
        float contentDrawY = -contentFm.ascent;

        if (PageContentView.CHAPTER_HOME_PAGE.equals(pageLines.get(0).getLineContent().trim())) {
            contentDrawY += 3 * 15 * readStatus.screenScaledDensity;
            contentDrawY += Constants.READ_CONTENT_PAGE_TOP_SPACE * readStatus.screenDensity;

            // 章节头与正文间距
            contentDrawY += 65 * readStatus.screenScaledDensity;
        }

        if (pageLines.get(0).isLastPage()) {
            lastPage = true;
        }

        if (!pageLines.isEmpty()) {
            for (int i = 0; i < pageLines.size(); i++) {
                if (!PageContentView.CHAPTER_HOME_PAGE.equals(pageLines.get(i).getLineContent().trim())) {
                    NovelLineBean text = pageLines.get(i);
                    if (text != null && !TextUtils.isEmpty(text.getLineContent()) && text.getLineContent().equals(" ")) {
                        contentDrawY += paragraphHeight;
                    } else {
                        contentDrawY += contentHeight;
                    }
                }
            }
            pageHeight = (int) (contentDrawY - lineSpace * 2);
        }

        if (lastPage) {
            pageHeight += chapterBetweenHeight;
        }
        return pageHeight;
    }

    public void setOnHelperCallBack(OnHelperCallBack callBack) {
        this.helperCallBack = callBack;
    }

    public void clear() {
        if (readStatus.bookNameList != null) {
            readStatus.bookNameList.clear();
            readStatus.bookNameList = null;
        }
        isShown = false;
        readStatus.bookName = "";
        readStatus.mLineList = null;

        if (this.pageView != null) {
            this.pageView = null;
        }
    }

    public interface OnHelperCallBack {

        void jumpNextChapter();

        void showDisclaimerActivity();

        void addBookShelf(boolean isAddShelf);

        void showCatalogActivity(Source source);

        void deleteBook();

        void openAutoReading(boolean open);

        void changSource();
    }
}
