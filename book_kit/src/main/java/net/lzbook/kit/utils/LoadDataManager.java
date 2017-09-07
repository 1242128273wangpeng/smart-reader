package net.lzbook.kit.utils;

import com.quduquxie.bean.BookListMode;
import com.quduquxie.network.DataService;
import com.quduquxie.network.DataServiceNew;
import com.quduquxie.utils.DataUtil;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.BookEvent;
import net.lzbook.kit.data.bean.ChapterErrorBean;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.net.volley.request.VolleyDataService;
import net.lzbook.kit.request.own.OtherRequestService;

import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

public class LoadDataManager {

    private static final String TAG = LoadDataManager.class.getSimpleName();

    private Context context;
    private SharedPreferencesUtils sharedPreferencesUtils;

    public LoadDataManager(Context context) {
        this.context = context;
        sharedPreferencesUtils = new SharedPreferencesUtils(PreferenceManager.getDefaultSharedPreferences(context));
    }

    //初始化书架，添加默认书籍
    public void addDefaultBooks() {

        AddDefaultBooksTask addDefaultBooksTask = new AddDefaultBooksTask();
        addDefaultBooksTask.execute();

    }

    public void updateShelfBooks() {

        UpdateBooksTask updateBooksTask = new UpdateBooksTask();
        updateBooksTask.execute();

        //青果书籍完结和连载状态的更新
        final BookDaoHelper bookDaoHelper = BookDaoHelper.getInstance(context);
        final ArrayList<Book> qgBooks = bookDaoHelper.getBooksNotFinishQG();
        if (qgBooks == null || qgBooks.isEmpty()) {
            return;
        }
        StringBuffer idBuffer = new StringBuffer();
        for (int i = 0; i < qgBooks.size(); i++) {
            Book book = qgBooks.get(i);
            if (!TextUtils.isEmpty(book.book_id)) {
                if (i == qgBooks.size() - 1) {
                    idBuffer.append("id=").append(book.book_id);
                } else {
                    idBuffer.append("id=").append(book.book_id).append("&");
                }
            }
        }
        StringBuilder builder = new StringBuilder();
        builder.append(DataUtil.BOOK_BATCH_URL).append("?" + idBuffer.toString());

        String udid = OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext());
        String url = DataUtil.QGBuildUrl(context, builder.toString(), udid, true);
        DataService.checkBookUpdate(url, new DataServiceNew.DataServiceCallBack() {
            @Override
            public void onSuccess(Object result) {
                if (result != null) {
                    BookListMode bookListMode = (BookListMode) result;
                    AppLog.e("LoadDataManager.checkBookUpdate", "booklistMode.success" + bookListMode.success);
                    for (com.quduquxie.bean.Book book : bookListMode.bookList) {
                        AppLog.e("LoadDataManager.checkBookUpdate", "book信息：" + book.id_book + "/" + book.name + "/" + book.attribute_book);
                        for (int i = 0; i < qgBooks.size(); i++) {

                            Book qgBook = qgBooks.get(i);
                            if (qgBook.book_id.equals(book.id_book)) {
                                if ("finish".equals(book.attribute_book)) {
                                    qgBook.status = 2;
                                } else if ("serialize".equals(book.attribute_book)) {
                                    qgBook.status = 1;
                                }
                                if (bookDaoHelper.updateBook(qgBook)) {
                                    AppLog.e("LoadDataManager.checkBookUpdate", "书架青果书籍: " + book.name + "完结/连载状态已更新!");
                                }
                            }

                        }
                    }

                    EventBus.getDefault().post(new BookEvent(BookEvent.PULL_BOOK_STATUS));
                }
            }

            @Override
            public void onError(Exception error) {
                error.printStackTrace();
            }
        });
    }

    public void startRequestDynamic(DynamicServiceCallBack cb) {

        UpdateDynamicParTask addDefaultBooksTask = new UpdateDynamicParTask();
        addDefaultBooksTask.execute(cb);

    }

    public void submitBookError(ChapterErrorBean chapterErrorBean) {

        ErrorSubmitTask errorSubmitTask = new ErrorSubmitTask();
        errorSubmitTask.execute(chapterErrorBean);

    }

    /**
     * 动态参数请求的回调
     **/
    public interface DynamicServiceCallBack {
        /**
         * 成功的回调
         */
        void onDynamicReceived(JSONObject result);

        /**
         * 失败的回调,包含以下情况
         * 1.请求超时
         * 2.返回空数据
         * 3.返回数据中success为false
         */
        void onError(Exception error);
    }

    private class AddDefaultBooksTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                OtherRequestService.getDefaultBook(context, new VolleyDataService.DataServiceCallBack() {

                    @Override
                    public void onSuccess(Object result) {
                        ArrayList<Book> iBooks = (ArrayList<Book>) result;
                        if (iBooks != null && iBooks.size() > 0) {
                            sharedPreferencesUtils.putBoolean(Constants.ADD_DEFAULT_BOOKS, true);
                            EventBus.getDefault().postSticky(new BookEvent(BookEvent.DEFAULTBOOK_UPDATED));
                        }
                    }

                    @Override
                    public void onError(Exception error) {

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class UpdateBooksTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                OtherRequestService.updateShelfBooks(context, new VolleyDataService.DataServiceCallBack() {

                    @Override
                    public void onSuccess(Object result) {
                        ArrayList<Book> iBooks = (ArrayList<Book>) result;
                        if (iBooks != null && iBooks.size() > 0) {
                            AppLog.i(TAG, "UpdateBooksTask onSuccess");
                        }
                    }

                    @Override
                    public void onError(Exception error) {

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class UpdateDynamicParTask extends AsyncTask<DynamicServiceCallBack, Void, Void> {

        @Override
        protected Void doInBackground(final DynamicServiceCallBack... params) {
            try {
                OtherRequestService.requestDynamicPar(new VolleyDataService.DataServiceCallBack() {

                    @Override
                    public void onSuccess(final Object result) {
                        if (result != null) {
                            if (params[0] != null) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        params[0].onDynamicReceived((JSONObject) result);
                                    }
                                }).start();
                            }
                        } else {
                            if (params[0] != null) {
                                params[0].onError(null);
                            }
                        }

                    }

                    @Override
                    public void onError(Exception error) {
                        if (params[0] != null) {
                            params[0].onError(error);
                        }
                    }
                });
            } catch (Exception e) {
                if (params[0] != null) {
                    params[0].onError(null);
                }
                e.printStackTrace();
            }
            return null;
        }
    }

    private class ErrorSubmitTask extends AsyncTask<ChapterErrorBean, Void, Void> {

        @Override
        protected Void doInBackground(ChapterErrorBean... params) {
            try {
                OtherRequestService.sendChapterErrorData(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}