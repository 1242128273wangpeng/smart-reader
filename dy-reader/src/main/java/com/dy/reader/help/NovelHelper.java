package com.dy.reader.help;

import android.app.Activity;
import android.text.TextUtils;

import com.ding.basic.bean.Book;
import com.ding.basic.bean.ChapterState;
import com.ding.basic.bean.Source;
import com.ding.basic.RequestRepositoryFactory;
import com.dy.reader.dialog.ReaderAddShelfDialog;
import com.dy.reader.dialog.ReaderAutoReadDialog;
import com.dy.reader.dialog.ReaderChangeSourceDialog;
import com.dy.reader.event.EventSetting;
import com.dy.reader.listener.SourceClickListener;
import com.dy.reader.setting.ReaderStatus;

import net.lzbook.kit.app.base.BaseBookApplication;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.StatServiceUtils;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

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

        final ReaderAutoReadDialog readerAutoReadDialog = new ReaderAutoReadDialog(activity);

        readerAutoReadDialog.setCancelListener(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                StatServiceUtils.statAppBtnClick(activity,
                        StatServiceUtils.rb_click_flip_auto_cancel);
                try {
                    readerAutoReadDialog.dismiss();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return null;
            }
        });


        readerAutoReadDialog.setConfirmListener(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                try {
                    readerAutoReadDialog.dismiss();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                if (helperCallBack != null) {
                    helperCallBack.openAutoReading(true);
                }
                return null;
            }
        });

        readerAutoReadDialog.setReceiverPromptListener(new Function1<Boolean, Unit>() {
            @Override
            public Unit invoke(Boolean isChecked) {
                StatServiceUtils.statAppBtnClick(activity,
                        StatServiceUtils.rb_click_flip_auto_not_tip);
                AppUtils.setBooleanPreferences(activity, "auto_read_hint", isChecked);
                return null;
            }
        });


        if (!activity.isFinishing()) {
            try {
                readerAutoReadDialog.show();
            } catch (Exception exception) {
                exception.printStackTrace();
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

        final ReaderAddShelfDialog readerAddShelfDialog = new ReaderAddShelfDialog(activity);

        readerAddShelfDialog.setCancelListener(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                try {
                    readerAddShelfDialog.dismiss();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                if (helperCallBack != null) {
                    helperCallBack.addBookShelf(false);
                }
                return null;
            }
        });

        readerAddShelfDialog.setConfirmListener(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                try {
                    readerAddShelfDialog.dismiss();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                if (helperCallBack != null) {
                    helperCallBack.addBookShelf(true);
                }
                return null;
            }
        });

        if (!activity.isFinishing()) {
            try {
                readerAddShelfDialog.show();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }


    public void showSourceDialog(final ArrayList<Source> sources) {
        if (actReference == null || actReference.get() == null
                || actReference.get().isFinishing()) {
            return;
        }

        final Activity activity = actReference.get();
        final ReaderChangeSourceDialog readerChangeSourceDialog = new ReaderChangeSourceDialog(
                activity);

        if (ReaderStatus.INSTANCE.getCurrentChapter() != null
                && ReaderStatus.INSTANCE.getCurrentChapter().getStatus()
                != ChapterState.CONTENT_NORMAL) {
            readerChangeSourceDialog.insertChangeSourcePrompt(
                    ReaderStatus.INSTANCE.getCurrentChapter().getStatus().getState());
        }

        readerChangeSourceDialog.showSourceList(activity, sources, new SourceClickListener() {

            @Override
            public void clickedSource(@NotNull Source source) {
                if (!TextUtils.isEmpty(source.getHost())) {
                    if (sourceCallBack != null) {
                        sourceCallBack.showCatalogActivity(source);
                    }
                }

                Map<String, String> map1 = new HashMap<>();
                map1.put("type", "2");
                StartLogClickUtil.upLoadEventLog(actReference.get(),
                        StartLogClickUtil.READPAGEMORE_PAGE, StartLogClickUtil.READ_SOURCECHANGE,
                        map1);
                readerChangeSourceDialog.dismiss();
            }
        });

        readerChangeSourceDialog.insertCancelListener(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                Map<String, String> map1 = new HashMap<>();
                map1.put("type", "1");
                StartLogClickUtil.upLoadEventLog(actReference.get(),
                        StartLogClickUtil.READPAGEMORE_PAGE, StartLogClickUtil.READ_SOURCECHANGE,
                        map1);
                readerChangeSourceDialog.dismiss();
                return null;
            }
        });

        readerChangeSourceDialog.insertContinueListener(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                Map<String, String> map1 = new HashMap<>();
                map1.put("type", "1");
                StartLogClickUtil.upLoadEventLog(actReference.get(),
                        StartLogClickUtil.READPAGEMORE_PAGE, StartLogClickUtil.READ_SOURCECHANGE,
                        map1);
                readerChangeSourceDialog.dismiss();

                if (ReaderStatus.INSTANCE.getCurrentChapter() != null
                        && ReaderStatus.INSTANCE.getCurrentChapter().getStatus()
                        != ChapterState.CONTENT_NORMAL) {
                    EventBus.getDefault().post(
                            new EventSetting(EventSetting.Type.MENU_STATE_CHANGE, null));
                }

                return null;
            }
        });

        readerChangeSourceDialog.show();
    }


    /**
     * 保存书签
     */
    public void saveBookmark(String book_id, int sequence, int offset) {

        Book book = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).loadBook(book_id);

        if (book != null) {
            book.setBook_id(book_id);
            book.setOffset(offset);
            book.setSequence(sequence);
            book.setReaded(1);
            book.setLast_read_time(System.currentTimeMillis());

            if (book.getChapter_count() <= 0) {
                book.setChapter_count(ReaderStatus.INSTANCE.getChapterCount());
            }

            RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                    BaseBookApplication.getGlobalContext()).updateBook(book);
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
