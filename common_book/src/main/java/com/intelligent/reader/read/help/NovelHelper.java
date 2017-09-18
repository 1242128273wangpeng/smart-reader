package com.intelligent.reader.read.help;

import com.dingyueads.sdk.NativeInit;
import com.intelligent.reader.R;
import com.intelligent.reader.activity.ReadingActivity;
import com.intelligent.reader.adapter.SourceAdapter;
import com.intelligent.reader.read.page.PageInterface;

import net.lzbook.kit.ad.OwnNativeAdManager;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.download.DownloadState;
import net.lzbook.kit.book.view.LoadingPage;
import net.lzbook.kit.book.view.MyDialog;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.NullCallBack;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.BookTask;
import net.lzbook.kit.data.bean.Chapter;
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
import android.os.Handler;
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
    private boolean checked;
    private boolean showDialog;
    private int lineNum;
    private WeakReference<Activity> actReference;
    private Handler handler;
    private MyDialog myDialog;
    private ReadStatus readStatus;
    private PageInterface pageView;

    public NovelHelper(Activity activity, ReadStatus readStatus, Handler handler) {
        this.actReference = new WeakReference<>(activity);
        this.readStatus = readStatus;
        this.handler = handler;
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

    public void showSourceDialog(final IReadDataFactory dataFactory, String curl, final ArrayList<Source> sources) {
        if (actReference == null || actReference.get() == null || actReference.get().isFinishing()) {
            return;
        }
        final Activity activity = actReference.get();
        final MyDialog sourceDialog = new MyDialog(activity, R.layout.dialog_read_source, Gravity.CENTER);
        sourceDialog.setCanceledOnTouchOutside(true);
        TextView change_source_disclaimer_message = (TextView) sourceDialog.findViewById(R.id.change_source_disclaimer_message);
        TextView change_source_original_web = (TextView) sourceDialog.findViewById(R.id.change_source_original_web);
        TextView change_source_continue = (TextView) sourceDialog.findViewById(R.id.change_source_continue);

        if (dataFactory.currentChapter.status != Chapter.Status.CONTENT_NORMAL) {
            change_source_disclaimer_message.setText(dataFactory.currentChapter.status.tips);
            change_source_original_web.setVisibility(View.INVISIBLE);
            change_source_continue.setText(R.string.jump_next_chapter);
        }

        ListView change_source_list = (ListView) sourceDialog.findViewById(R.id.change_source_list);

        final SourceAdapter sourceListAdapter = new SourceAdapter(activity, sources);
        change_source_list.setAdapter(sourceListAdapter);

        if (Constants.IS_LANDSCAPE) {
            if (sources.size() > 2) {
                change_source_list.getLayoutParams().height = activity.getResources().getDimensionPixelOffset(R.dimen.dimen_view_height_120);
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
                if (dataFactory.currentChapter.status != Chapter.Status.CONTENT_NORMAL) {
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

    private ArrayList<ArrayList<String>> initTextContent2(String content) {
        float chapterHeight = 35 * readStatus.screenScaledDensity + 100;
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
        final float tHeight = fm.descent - fm.ascent;
        float height = 0;
        if (Constants.isSlideUp) {
            height = readStatus.screenHeight;
        } else {
            height = readStatus.screenHeight - tHeight - readStatus.screenDensity
                    * Constants.READ_CONTENT_PAGE_TOP_SPACE * readStatus.screenScaledDensity;
        }

        float width = readStatus.screenWidth - readStatus.screenDensity * Constants.READ_CONTENT_PAGE_LEFT_SPACE * 2;

        // 获取行高 文字高度+0.5倍行间距
        float lineHeight = fm.descent - fm.ascent + Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE
                * readStatus.screenScaledDensity;
        float m_duan = (Constants.READ_PARAGRAPH_SPACE - Constants.READ_INTERLINEAR_SPACE) * Constants.FONT_SIZE
                * readStatus.screenScaledDensity;

        // lineHeight = fm.descent - fm.ascent +
        // Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE
        // * readStatus.screenScaledDensity;

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


            String[] chapterNumAndName = readStatus.chapterNameList.get(0).split("章");
            ArrayList<String> newChapterList = new ArrayList<>();

            for (int i = 0; i < chapterNumAndName.length; i++) {
                if (i == 0) {
                    newChapterList.add(chapterNumAndName[i] + "章");
                } else {
                    newChapterList.add(chapterNumAndName[i].trim());
                }
            }
            if (readStatus.chapterNameList.size() > 1) {
                readStatus.chapterNameList.remove(0);
                newChapterList.addAll(readStatus.chapterNameList);
            }
            readStatus.chapterNameList = newChapterList;

            if (readStatus.chapterNameList.size() > 2) {
                ArrayList<String> temp = new ArrayList<String>();
                for (int i = 0; i < 2; i++) {
                    temp.add(readStatus.chapterNameList.get(i));
                }
                readStatus.chapterNameList = temp;
            }
        }

        if (readStatus != null && readStatus.book != null && Constants.QG_SOURCE.equals(readStatus.book.site)) {
            String[] contents = content.split("\n");
            for (String temp : contents) {
                sb.append("\u3000\u3000" + temp + "\n");
            }
        } else {
            sb.append(content);
        }
        String text = "";
        if (readStatus.sequence == -1) {
            readStatus.bookNameList = getNovelText(mbookNamePaint, readStatus.bookName, width);
            String homeText = "txtzsydsq_homepage\n";
            StringBuilder s = new StringBuilder();
            s.append(homeText);
            text = s.toString() + sb.toString();
        } else {
            text = sb.toString();
        }
        if (readStatus.offset > text.length()) {
            readStatus.offset = 0;
        } else if (readStatus.offset < 0) {
            readStatus.offset = 0;
        }

        ArrayList<String> contentList = new ArrayList<String>();
        contentList = getNovelText(mTextPaint, text, width);
        final int size = contentList.size();
        int textSpace = 0;
        long textLength = 0;
        boolean can = true;
        ArrayList<String> pageLines = new ArrayList<String>();
        ArrayList<ArrayList<String>> lists = new ArrayList<ArrayList<String>>();
        lists.add(pageLines);
        int chapterNameSize = 0;
        if (readStatus.chapterNameList != null) {
            chapterNameSize = readStatus.chapterNameList.size();
        }
        if (chapterNameSize > 1) {
            textSpace += chapterHeight;
        }

        // boolean isLastDuan = false;
        for (int i = 0; i < size; i++) {
            boolean isDuan = false;
            String lineText = contentList.get(i);
            if (lineText.equals(" ")) {// 段间距
                isDuan = true;
                textSpace += m_duan;
            } else if (lineText.equals("chapter_homepage  ")) {
                textSpace += hideHeight;
                textLength += lineText.length();
            } else {
                textSpace += lineHeight;
                textLength += lineText.length();
            }

            if (textSpace < height) {
                pageLines.add(lineText);
            } else {
                // if (isLastDuan) {
                // textSpace -= m_duan;
                // pageLines.remove(lineText);
                // }else {
                if (isDuan) {// 开始是空行
                    textSpace -= m_duan;
                } else {
                    pageLines = new ArrayList<String>();
                    textSpace = 0;
                    pageLines.add(lineText);
                    lists.add(pageLines);
                }
                // }
            }
            if (textLength >= readStatus.offset && can) {
                readStatus.currentPage = lists.size();
                can = false;
            }
            // isLastDuan = isDuan;
        }

        // 去除章节开头特殊符号
        if ((readStatus.sequence >= 0) && lists.size() > 3) {
            String chapterTitle = lists.get(0).get(3);
            if (!TextUtils.isEmpty(chapterTitle) && chapterTitle.contains("\"")) {
                lists.get(0).set(3, chapterTitle.replace("\"", "").trim());
            }
        }

        if (isNativeAdAvailable() && lists.size() >= 3) {
            lists.add(addList(empty_page_ad));
        }
        //添加章节内广告
//        if (isNativeAdAvailableNew() && lists.size() >= Constants.native_ad_page_in_chapter_limit) {
//            lists.add(lists.size()/2, addList(empty_page_ad_inChapter));
//        }
        if (isNativeAdAvailableNew() && lists.size() >= 2 * Constants.native_ad_page_in_chapter_limit) {
            int i2 = lists.size() / Constants.native_ad_page_in_chapter_limit;
            boolean isFirst = true;
            for (int i = 1; i <= i2; i++) {
                int i1 = i * Constants.native_ad_page_in_chapter_limit;
                if (i1 < lists.size() && (lists.size() - i1) >= Constants.native_ad_page_in_chapter_limit) {
                    lists.add(i1, addList(empty_page_ad_inChapter + i));
                    if (actReference != null && actReference.get() != null && !actReference.get().isFinishing()) {
                        OwnNativeAdManager.getInstance(actReference.get()).loadAd(NativeInit.CustomPositionName.READING_IN_CHAPTER_POSITION);
                    }
                }
                if ((readStatus.currentPage >= i * Constants.native_ad_page_in_chapter_limit) && (readStatus.currentPage < ((i + 1) * Constants.native_ad_page_in_chapter_limit/* + i - 1*/)) && isFirst) {
                    if (readStatus.currentPage == Constants.native_ad_page_in_chapter_limit) {
                        continue;
                    }
                    readStatus.currentPage += i;
                    isFirst = false;
                }
            }
        }

        readStatus.pageCount = lists.size();
        if (readStatus.currentPage == 0) {
            readStatus.currentPage = 1;
        }

        return lists;
    }

    private boolean isNativeAdAvailable() {
        AppLog.e(TAG, "readedCount:" + Constants.readedCount + " native_ad_page_interstitial_count:" + Constants.native_ad_page_interstitial_count
                + " chapterCount:" + readStatus.chapterCount + " NetworkUtils.NATIVE_AD_TYPE:" + NetWorkUtils.NATIVE_AD_TYPE);
        boolean isAvailable = !Constants.isSlideUp && readStatus.sequence != -1 && NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE
                /*&& NetWorkUtils.NATIVE_AD_TYPE == NetWorkUtils.NATIVE_AD_READY*/
                && !Constants.isHideAD
                && Constants.readedCount != 0 && Constants.native_ad_page_interstitial_count != 0 && Constants.readedCount % Constants.native_ad_page_interstitial_count == 0 && Constants.dy_page_middle_ad_switch;
        AppLog.e(TAG, "isAvailable:" + isAvailable);
        return isAvailable;
    }

    private boolean isNativeAdAvailableNew() {
        AppLog.e(TAG, "readedCount:" + Constants.readedCount + " native_ad_page_gap_in_chapter:" + Constants.native_ad_page_gap_in_chapter
                + " chapterCount:" + readStatus.chapterCount + " NetworkUtils.NATIVE_AD_TYPE:" + NetWorkUtils.NATIVE_AD_TYPE);
        boolean isAvailable = !Constants.isSlideUp && readStatus.sequence != -1 && NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE
                /*&& NetWorkUtils.NATIVE_AD_TYPE == NetWorkUtils.NATIVE_AD_READY*/
                && !Constants.isHideAD
                && Constants.readedCount != 0 && Constants.native_ad_page_gap_in_chapter != 0 && Constants.readedCount % Constants.native_ad_page_gap_in_chapter == 0 && Constants.dy_page_in_chapter_ad_switch;
        AppLog.e(TAG, "isAvailable:" + isAvailable);
        return isAvailable;
    }
//	private static final ArrayList<String> emptyList = new ArrayList<String>() {{
//		add(empty_page_ad);
//	}};

    private ArrayList<String> addList(String adString) {
        ArrayList<String> list = new ArrayList<>();
        list.add(adString);
        return list;
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
    private ArrayList<String> getNovelText(TextPaint textPaint, String text, float width) {
        ArrayList<String> list = new ArrayList<String>();
        float w = 0;
        int istart = 0;
        char mChar;
        float[] widths = new float[1];
        float[] chineseWidth = new float[1];
        textPaint.getTextWidths("正", chineseWidth);
        if (text == null) {
            return list;
        }
        int duan_coount = 0;
        for (int i = 0; i < text.length(); i++) {
            mChar = text.charAt(i);
            if (mChar == '\n') {
                widths[0] = 0;
            } else if (Tools.isChinese(mChar)) {
                widths[0] = chineseWidth[0];
            } else {
                String srt = String.valueOf(mChar);
                textPaint.getTextWidths(srt, widths);
            }
            if (mChar == '\n') {
                duan_coount++;
                list.add(text.substring(istart, i) + " ");
                if (duan_coount > 3) {
                    list.add(" ");// 段间距
                }
                istart = i + 1;
                w = 0;
            } else {
                w += widths[0];
                if (w > width) {
                    list.add(text.substring(istart, i));
                    istart = i;
                    i--;
                    w = 0;
                } else {
                    if (i == (text.length() - 1)) {
                        list.add(text.substring(istart, text.length()));
                    }
                }
            }
        }
        return list;
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

    public synchronized List<String> getPageContent() {
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
        ArrayList<String> pageContent = null;
        if (readStatus.currentPage - 1 < readStatus.mLineList.size()) {
            pageContent = readStatus.mLineList.get(readStatus.currentPage - 1);
        } else {
            pageContent = new ArrayList<String>();
        }

        for (int i = 0; i < readStatus.currentPage - 1 && i < readStatus.mLineList.size(); i++) {
            ArrayList<String> pageList = readStatus.mLineList.get(i);
            final int size = pageList.size();
            // AppLog.d("initTextContent2", "size:" + size);
            for (int j = 0; j < size; j++) {
                String string = pageList.get(j);
                if (!TextUtils.isEmpty(string) && !string.equals(" ")) {
                    readStatus.offset += string.length();
                }
            }
        }
        readStatus.currentPageConentLength = 0;
        for (int i = 0; i < pageContent.size(); i++) {
            readStatus.currentPageConentLength += pageContent.get(i).length();

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
            ArrayList<String> pageList = readStatus.mLineList.get(i);
            final int size = pageList.size();
            // AppLog.d("initTextContent2", "size:" + size);
            for (int j = 0; j < size; j++) {
                String string = pageList.get(j);
                if (!TextUtils.isEmpty(string) && !string.equals(" ")) {
                    readStatus.offset += string.length();
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
     */
    public synchronized void getChapterContent(Activity activity, Chapter currentChapter, Book mBook, boolean isResize) {
        if (mBook == null)
            return;
        // 更新chapter状态
        BaseBookHelper.setChapterStatus(currentChapter);
        if (currentChapter != null && readStatus != null) {
            if (currentChapter.status != Chapter.Status.CONTENT_NORMAL) {
                handler.sendEmptyMessage(5);
            }
            readStatus.chapterName = currentChapter.chapter_name;
        }
        if (readStatus != null) {
            readStatus.bookName = mBook.name;
            readStatus.bookAuthor = mBook.author;
            //  小说来源
            readStatus.bookSource = mBook.name;
        }

        if (!isResize && mBook != null && mBook.book_type == 0 && activity != null && activity instanceof ReadingActivity
                && currentChapter != null && currentChapter.content != null) {
            ((ReadingActivity) activity).addTextLength(currentChapter.content.length());
        }

        switch (currentChapter.status) {
            case CONTENT_EMPTY:
            case CONTENT_ERROR:
            case SOURCE_ERROR:
                if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE && mBook.book_type == 0) {
                    Toast.makeText(activity, R.string.err_no_net, Toast.LENGTH_SHORT).show();
                    readStatus.mLineList = initTextContent2("");
                    break;
                }
                readStatus.mLineList = initTextContent2("");
                break;
            case CONTENT_NORMAL:
                readStatus.mLineList = initTextContent2(currentChapter.content);
                break;
            default:
                break;
        }

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

    /**
     * loading 页面显示原网页地址
     */
    private void setLoadingCurl(int time, Chapter currentChapter, LoadingPage loadingPage) throws InterruptedException {
        if (currentChapter != null && !TextUtils.isEmpty(currentChapter.curl)) {
            //if (readStatus.book.dex == 1 && !TextUtils.isEmpty(currentChapter.curl)) {
            AppLog.e("setNovelSource", "SetNovelSource: " + currentChapter.curl);
            loadingPage.setNovelSource(currentChapter.curl);
            Thread.sleep(time);
            /*} else if (readStatus.book.dex == 0 && !TextUtils.isEmpty(currentChapter.curl1)) {
                AppLog.e("setNovelSource", "SetNovelSource: " + currentChapter.curl1);
                loadingPage.setNovelSource(currentChapter.curl1);
                Thread.sleep(time);
            }*/
        }
    }

    private LoadingPage getCustomLoadingPage() {
        Activity activity = actReference.get();
        if (activity == null) {
            return null;
        }
        LoadingPage loadingPage = new LoadingPage(activity, true, "", LoadingPage.setting_result);
        loadingPage.setCustomBackgroud();

        return loadingPage;
    }

    public interface OnHelperCallBack {

        void jumpNextChapter();

        void showDisclaimerActivity();

        void addBookShelf(boolean isAddShelf);

        void showCatalogActivity(Source source);

        void deleteBook();

        void openAutoReading(boolean open);
    }
}
