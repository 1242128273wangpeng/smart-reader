package com.intelligent.reader.read.help;

import com.intelligent.reader.R;
import com.intelligent.reader.read.mode.ReadState;
import com.intelligent.reader.widget.ConfirmDialog;
import com.intelligent.reader.widget.ChangeSourcePopWindow;

import net.lzbook.kit.book.view.MyDialog;
import net.lzbook.kit.constants.ReadConstants;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.NovelLineBean;
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
import android.text.TextPaint;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

/**
 * 阅读页工具类
 */
public class NovelHelper {
    public static final String empty_page_ad = "empty_page_ad";
    private static final String TAG = "NovelHelper";
    public boolean isShown = false;
    private OnHelperCallBack helperCallBack;
    private WeakReference<Activity> actReference;

    public NovelHelper(Activity activity) {
        this.actReference = new WeakReference<>(activity);
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


        final ConfirmDialog confirmDialog = new ConfirmDialog(activity);
        confirmDialog.setTitle(activity.getString(R.string.prompt));
        confirmDialog.setContent("喜欢就加入书架吧！");
        confirmDialog.setConfirmName("加入书架");
        confirmDialog.setOnConfirmListener(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                confirmDialog.dismiss();
                if (helperCallBack != null) {
                    helperCallBack.addBookShelf(true);
                }
                return null;
            }
        });
        confirmDialog.setOnCancelListener(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                if (helperCallBack != null) {
                    helperCallBack.addBookShelf(false);
                }
                return null;
            }
        });
        confirmDialog.show();

//        final MyDialog myDialog = new MyDialog(activity, R.layout.pop_confirm_layout);
//        TextView tv_update_info_dialog = (TextView) myDialog.findViewById(R.id.publish_content);
//        TextView dialog_title = (TextView) myDialog.findViewById(R.id.dialog_title);
//        dialog_title.setText(R.string.prompt);
//        tv_update_info_dialog.setText("喜欢就加入书架吧！");
//        tv_update_info_dialog.setGravity(Gravity.CENTER);
//        Button bt_cancel = (Button) myDialog.findViewById(R.id.publish_stay);
//        Button bt_ok = (Button) myDialog.findViewById(R.id.publish_leave);
//        bt_ok.setText("加入书架");
//        bt_cancel.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (myDialog != null) {
//                    try {
//
//
//                        myDialog.dismiss();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//                if (helperCallBack != null) {
//                    helperCallBack.addBookShelf(false);
//                }
//            }
//        });
//        bt_ok.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (myDialog != null) {
//                    try {
//                        myDialog.dismiss();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//                if (helperCallBack != null) {
//                    helperCallBack.addBookShelf(true);
//                }
//            }
//        });
//        if (myDialog != null && !myDialog.isShowing() && !activity.isFinishing()) {
//            try {
//                myDialog.show();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }

    private void dismissDialog(MyDialog sourceDialog) {
        if (sourceDialog != null && sourceDialog.isShowing()) {
            sourceDialog.dismiss();
        }
    }

    public void showSourceDialog(String curl, final ArrayList<Source> sources) {
        if (actReference == null || actReference.get() == null || actReference.get().isFinishing()) {
            return;
        }
        ChangeSourcePopWindow.newBuilder(actReference.get()).setSourceData(sources)
                .setOnSourceItemClick(new ChangeSourcePopWindow.OnSourceItemClickListener() {
                    @Override
                    public void onSourceItemClick(Source source) {
                        helperCallBack.showCatalogActivity(source);
                    }
                }).build().show();

//        final Activity activity = actReference.get();
//        final MyDialog sourceDialog = new MyDialog(activity, R.layout.dialog_read_source, Gravity.CENTER);
//        sourceDialog.setCanceledOnTouchOutside(true);
//        TextView change_source_disclaimer_message = (TextView) sourceDialog.findViewById(R.id.change_source_disclaimer_message);
//        TextView change_source_original_web = (TextView) sourceDialog.findViewById(R.id.change_source_original_web);
//        TextView change_source_continue = (TextView) sourceDialog.findViewById(R.id.change_source_continue);
//
//        if (ReadState.INSTANCE.getCurrentChapter() != null && ReadState.INSTANCE.getCurrentChapter().status != Chapter.Status.CONTENT_NORMAL) {
//            change_source_disclaimer_message.setText(ReadState.INSTANCE.getCurrentChapter().status.tips);
//            change_source_original_web.setVisibility(View.INVISIBLE);
//            change_source_continue.setText(R.string.jump_next_chapter);
//        }
//
//        ListView change_source_list = (ListView) sourceDialog.findViewById(R.id.change_source_list);
//
//        final SourceAdapter sourceListAdapter = new SourceAdapter(activity, sources);
//        change_source_list.setAdapter(sourceListAdapter);
//
//        if (ReadConfig.INSTANCE.getIS_LANDSCAPE()) {
//            if (sources.size() > 1) {
//                change_source_list.getLayoutParams().height = activity.getResources().getDimensionPixelOffset(R.dimen.dimen_view_height_80);
//            }
//        } else {
//            if (sources.size() > 3) {
//                change_source_list.getLayoutParams().height = activity.getResources().getDimensionPixelOffset(R.dimen.dimen_view_height_135);
//            }
//        }
//
//        change_source_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Source source = (Source) sourceListAdapter.getItem(position);
//
//                if (source != null && !TextUtils.isEmpty(source.host)) {
//                    helperCallBack.showCatalogActivity(source);
//                }
//
//                Map<String, String> map1 = new HashMap<String, String>();
//                map1.put("type", "2");
//                StartLogClickUtil.upLoadEventLog(actReference.get(), StartLogClickUtil.READPAGEMORE_PAGE, StartLogClickUtil.READ_SOURCECHANGE, map1);
//                dismissDialog(sourceDialog);
//            }
//        });
//
//        change_source_original_web.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Map<String, String> map1 = new HashMap<String, String>();
//                map1.put("type", "1");
//                StartLogClickUtil.upLoadEventLog(actReference.get(), StartLogClickUtil.READPAGEMORE_PAGE, StartLogClickUtil.READ_SOURCECHANGE, map1);
//                dismissDialog(sourceDialog);
//            }
//        });
//        change_source_continue.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Map<String, String> map1 = new HashMap<String, String>();
//                map1.put("type", "1");
//                StartLogClickUtil.upLoadEventLog(actReference.get(), StartLogClickUtil.READPAGEMORE_PAGE, StartLogClickUtil.READ_SOURCECHANGE, map1);
//                dismissDialog(sourceDialog);
//                if (ReadState.INSTANCE.getCurrentChapter() != null && ReadState.INSTANCE.getCurrentChapter().status != Chapter.Status.CONTENT_NORMAL) {
//                    if (helperCallBack != null) {
//                        helperCallBack.jumpNextChapter();
//                    }
//                }
//            }
//        });
//        if (!sourceDialog.isShowing()) {
//            try {
//                sourceDialog.show();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
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
    public void saveBookmark(String book_id, int sequence, int offset,
                             BookDaoHelper mBookDaoHelper) {

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
        if (mBook == null)
            return false;
        // 更新chapter状态
        BaseBookHelper.setChapterStatus(currentChapter);
        if (currentChapter != null) {
            if (currentChapter.status != Chapter.Status.CONTENT_NORMAL) {
                if (helperCallBack != null) {
                    helperCallBack.changSource();
                }
            }
            ReadState.INSTANCE.setChapterName(currentChapter.chapter_name);
        }

        if (currentChapter != null) {
            switch (currentChapter.status) {
                case CONTENT_EMPTY:
                case CONTENT_ERROR:
                case SOURCE_ERROR:
                    if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE && mBook.book_type == 0) {
                        Toast.makeText(activity, R.string.err_no_net, Toast.LENGTH_SHORT).show();
                    }
                    return true;
                case CONTENT_NORMAL:
                    return true;
                default:
                    break;
            }
        }
        return false;
    }

    public void setOnHelperCallBack(OnHelperCallBack callBack) {
        this.helperCallBack = callBack;
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
