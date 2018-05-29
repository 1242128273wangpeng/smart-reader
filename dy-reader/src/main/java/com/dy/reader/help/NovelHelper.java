package com.dy.reader.help;

import com.ding.basic.bean.Book;
import com.ding.basic.bean.ChapterState;
import com.ding.basic.bean.Source;
import com.ding.basic.repository.RequestRepositoryFactory;
import com.dy.reader.R;
import com.dy.reader.event.EventSetting;
import com.dy.reader.setting.ReaderSettings;
import com.dy.reader.setting.ReaderStatus;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.adapter.SourceAdapter;
import net.lzbook.kit.book.view.MyDialog;
import net.lzbook.kit.constants.ReadConstants;
import net.lzbook.kit.data.db.help.ChapterDaoHelper;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.StatServiceUtils;

import org.greenrobot.eventbus.EventBus;

import android.app.Activity;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 阅读页工具类
 */
public class NovelHelper {
    public static final String empty_page_ad = "empty_page_ad";
    private static final String TAG = "NovelHelper";
    private OnHelperCallBack helperCallBack;
    private OnSourceCallBack sourceCallBack;
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
        final MyDialog myDialog = new MyDialog(activity, R.layout.read_addshelf_dialog);
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

        final MyDialog myDialog = new MyDialog(activity, R.layout.read_hint_dialog);
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

    private void dismissDialog(MyDialog sourceDialog) {
        if (sourceDialog != null && sourceDialog.isShowing()) {
            sourceDialog.dismiss();
        }
    }

    public void showSourceDialog(final ArrayList<Source> sources) {
        if (actReference == null || actReference.get() == null || actReference.get().isFinishing()) {
            return;
        }
        final Activity activity = actReference.get();
        final MyDialog sourceDialog = new MyDialog(activity, R.layout.dialog_read_source, Gravity.CENTER);
        sourceDialog.setCanceledOnTouchOutside(true);
        TextView change_source_disclaimer_message = (TextView) sourceDialog.findViewById(R.id.change_source_disclaimer_message);
        TextView change_source_original_web = (TextView) sourceDialog.findViewById(R.id.change_source_original_web);
        TextView change_source_continue = (TextView) sourceDialog.findViewById(R.id.change_source_continue);

        if (ReaderStatus.INSTANCE.getCurrentChapter() != null && ReaderStatus.INSTANCE.getCurrentChapter().getStatus() != ChapterState.CONTENT_NORMAL) {
            change_source_disclaimer_message.setText(ReaderStatus.INSTANCE.getCurrentChapter().getStatus().getState());
            change_source_original_web.setVisibility(View.INVISIBLE);
            change_source_continue.setText(R.string.jump_next_chapter);
        }

        ListView change_source_list = (ListView) sourceDialog.findViewById(R.id.change_source_list);

        final SourceAdapter sourceListAdapter = new SourceAdapter(activity, sources);
        change_source_list.setAdapter(sourceListAdapter);

        if (ReaderSettings.Companion.getInstance().isLandscape()) {
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

                if (source != null && !TextUtils.isEmpty(source.getHost())) {
                    if (sourceCallBack != null) {
                        sourceCallBack.showCatalogActivity(source);
                    }
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
                if (ReaderStatus.INSTANCE.getCurrentChapter() != null && ReaderStatus.INSTANCE.getCurrentChapter().getStatus() != ChapterState.CONTENT_NORMAL) {
                    EventBus.getDefault().post(new EventSetting(EventSetting.Type.MENU_STATE_CHANGE, null));
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
    public void saveBookmark(String book_id, int sequence, int offset) {

        Book book = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(book_id);
        if (book != null) {
            book.setBook_id(book_id);
            book.setSequence(sequence);
            book.setOffset(offset);
            book.setLast_read_time(System.currentTimeMillis());
            if ((RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(book.getBook_id()) != null)) {
                book.setReaded(1);
            }

//            AppLog.e(TAG, "保存书籍阅读状态: " + book.toString());
            RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).updateBook(book);

//            AppLog.e(TAG, "保存书籍阅读状态: " + ChapterDaoHelper.Companion.loadChapterDataProviderHelper(actReference.get(), book_id).getCount());
        }
    }


    public void setOnHelperCallBack(OnHelperCallBack callBack) {
        this.helperCallBack = callBack;
    }

    public void setOnSourceCallBack(OnSourceCallBack callBack) {
        this.sourceCallBack = callBack;
    }


    public interface OnHelperCallBack {

        void addBookShelf(boolean isAddShelf);

        void openAutoReading(boolean open);
    }


    public interface OnSourceCallBack {

        void showCatalogActivity(Source source);

    }
}
