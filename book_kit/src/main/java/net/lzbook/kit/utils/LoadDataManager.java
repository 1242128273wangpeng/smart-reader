package net.lzbook.kit.utils;

import com.google.gson.JsonObject;

import com.quduquxie.bean.BookListMode;
import com.quduquxie.network.DataService;
import com.quduquxie.network.DataServiceNew;
import com.quduquxie.utils.DataUtil;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.NoBodyEntity;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.BookEvent;
import net.lzbook.kit.data.bean.ChapterErrorBean;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.net.custom.service.NetService;
import net.lzbook.kit.request.own.OWNParser;

import org.json.JSONException;

import android.content.Context;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;

import de.greenrobot.event.EventBus;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

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

        Observable<JsonObject> defaultBook = NetService.INSTANCE.getUserService().getDefaultBook();
        defaultBook.subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribe(new Consumer<JsonObject>() {
            @Override
            public void accept(@NonNull JsonObject jsonObject) throws Exception {
                try {
                    ArrayList<Book> iBooks = OWNParser.parserOwnDefaultBook(jsonObject.toString(), BaseBookApplication.getGlobalContext());
                    if (iBooks != null && iBooks.size() > 0) {
                        saveDefaultBooks(iBooks);
                        sharedPreferencesUtils.putBoolean(Constants.ADD_DEFAULT_BOOKS, true);
                        EventBus.getDefault().postSticky(new BookEvent(BookEvent.DEFAULTBOOK_UPDATED));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                throwable.printStackTrace();
            }
        });


    }

    /**
     * 数据库存入加入默认书籍
     */
    private void saveDefaultBooks(ArrayList<Book> iBooks) {
        BookDaoHelper daoHelper = BookDaoHelper.getInstance();
        for (Book iBook : iBooks) {
            if (!daoHelper.isBookSubed(iBook.book_id)) {
                if (daoHelper.insertBook(iBook)) {
                    AppLog.i(TAG, "iBook.last_updateSucessTime = " + iBook.last_updateSucessTime);
                }
            }
        }
    }

    public void updateShelfBooks() {
        //智能书籍完结和连载状态的更新
        updateZNBookState();

        //青果书籍完结和连载状态的更新
        final BookDaoHelper bookDaoHelper = BookDaoHelper.getInstance();
        final ArrayList<Book> qgBooks = bookDaoHelper.getBooksNotFinishQG();
        if (qgBooks == null) {
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
        if (qgBooks.isEmpty()) {
            EventBus.getDefault().post(new BookEvent(BookEvent.PULL_BOOK_STATUS));
            return;
        }
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


    private void updateZNBookState() {
        final BookDaoHelper bookDaoHelper = BookDaoHelper.getInstance();
        ArrayList<Book> books = bookDaoHelper.getOwnBooksList();
        if (books == null || books.isEmpty()) {
            return;
        }
        HashMap<String, String> parameter = new HashMap<>();
        StringBuffer idBuffer = new StringBuffer();
        StringBuffer sourceBuffer = new StringBuffer();
        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            if (!TextUtils.isEmpty(book.book_id) && !TextUtils.isEmpty(book.book_source_id)) {
                if (i == books.size() - 1) {
                    idBuffer.append(book.book_id);
                    sourceBuffer.append(book.book_source_id);
                } else {
                    idBuffer.append(book.book_id + "$$");
                    sourceBuffer.append(book.book_source_id + "$$");
                }
            }
        }
        parameter.put("book_ids", idBuffer.toString());
        parameter.put("book_source_ids", sourceBuffer.toString());

    }


    //阅读页错误反馈
    public void submitBookError(ChapterErrorBean chapterErrorBean) {

        HashMap<String, String> data = new HashMap<>();
        data.put("bookSourceId", chapterErrorBean.bookSourceId);
        data.put("bookName", chapterErrorBean.bookName);
        data.put("author", chapterErrorBean.author);
        data.put("bookChapterId", chapterErrorBean.bookChapterId);
        data.put("chapterId", chapterErrorBean.chapterId);
        data.put("chapterName", chapterErrorBean.chapterName);
        data.put("serial", String.valueOf(chapterErrorBean.serial));
        data.put("host", chapterErrorBean.host);
        data.put("type", String.valueOf(chapterErrorBean.type));


        Observable<NoBodyEntity> send = NetService.INSTANCE.getUserService().sendFeedBack(data);
        send.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<NoBodyEntity>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onNext(@NonNull NoBodyEntity noBodyEntity) {
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        try {
                            e.toString();
                        } finally {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });


    }
}