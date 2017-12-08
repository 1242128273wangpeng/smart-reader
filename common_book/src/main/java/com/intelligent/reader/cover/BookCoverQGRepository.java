package com.intelligent.reader.cover;

import com.intelligent.reader.repository.BookCoverRepository;
import com.quduquxie.bean.BookMode;
import com.quduquxie.network.DataParser;
import com.quduquxie.network.DataService;
import com.quduquxie.utils.DataUtil;

import net.lzbook.kit.data.bean.Bookmark;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.CoverPage;
import net.lzbook.kit.data.bean.RequestItem;
import net.lzbook.kit.utils.BeanParser;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Function;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author lijun Lee
 * @desc 书籍数据 青果
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/11/16 17:40
 */

public class BookCoverQGRepository implements BookCoverRepository {

    private static volatile BookCoverQGRepository INSTANCE;

    private final String mUdId;

    private BookCoverQGRepository(String udId) {
        this.mUdId = udId;
    }

    public static BookCoverQGRepository getInstance(@NonNull String udId) {
        if (INSTANCE == null) {
            synchronized (BookCoverQGRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BookCoverQGRepository(udId);
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public Observable<CoverPage> getCoverDetail(String bookId, String sourceId, String host) {
        String url = DataUtil.QGBuildUrl(null, DataUtil.BOOK_INFO_URL + "/" + bookId, mUdId, false);
        return createCoverToObservable(url);
    }

    @Override
    public Observable<List<Chapter>> getChapterList(RequestItem requestItem) {
        return getChapterListData(requestItem.book_id);
    }

    @Override
    public boolean isBookSubscribe(String bookId) {
        return false;
    }

    @Override
    public boolean saveBookChapterList(List<Chapter> chapterList, RequestItem requestItem) {
        return false;
    }

    @Override
    public Observable<ArrayList<Bookmark>> getBookMarkList(String bookId) {
        return null;
    }

    @Override
    public void deleteBookMark(ArrayList<Integer> ids) {

    }


    /**
     * 书籍封面
     */
    private ResponseBody getCoverData(String url) throws Exception {
        Request request = new Request.Builder().url(url).build();
        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();
        return response.body();
    }

    private CoverPage parseToBookVoBean(BookMode bookMode) {
        CoverPage coverPage = new CoverPage();
        //防止青果后端书籍出错导致的封面页崩溃问题
        if (bookMode.model == null) {
            return new CoverPage();
        }
        CoverPage.BookVoBean bookVoBean = new CoverPage.BookVoBean();
        bookVoBean.book_source_id = BookCoverRepositoryFactory.QG_SOURCE;
        bookVoBean.host = BookCoverRepositoryFactory.QG_SOURCE;
        bookVoBean.book_id = bookMode.model.id_book;
        bookVoBean.name = bookMode.model.name;
        bookVoBean.author = bookMode.model.penname;
        if (!TextUtils.isEmpty(bookMode.model.attribute_book)) {
            bookVoBean.status = bookMode.model.attribute_book.equals("serialize") ? 1 : 2;
        }
        bookVoBean.last_chapter_name = bookMode.model.id_last_chapter_name;
        bookVoBean.serial_number = bookMode.model.id_last_chapter_serial_number;//总章数
        bookVoBean.img_url = bookMode.model.image_book;
        bookVoBean.labels = bookMode.model.category;
        bookVoBean.desc = bookMode.model.description;
        bookVoBean.update_time = bookMode.model.id_last_chapter_create_time;
        bookVoBean.wordCountDescp = bookMode.model.word_count + "";
        bookVoBean.readerCountDescp = bookMode.model.follow_count + "";
        bookVoBean.score = Double.valueOf(bookMode.model.score + "");
        coverPage.bookVo = bookVoBean;


        return coverPage;
    }

    private Observable<CoverPage> createCoverToObservable(final String url) {
        Observable<CoverPage> newObservable = Observable.create(new ObservableOnSubscribe<BookMode>() {
            @Override
            public void subscribe(ObservableEmitter<BookMode> e) throws Exception {
                e.onNext(DataParser.getBookMode(getCoverData(url).string()));
                e.onComplete();
            }
        }).map(new Function<BookMode, CoverPage>() {
            @Override
            public CoverPage apply(BookMode bookMode) throws Exception {
                return parseToBookVoBean(bookMode);
            }
        });
        return newObservable;
    }

    /********************************************************************/

    /**
     * 书籍目录
     */
    private Observable<List<Chapter>> getChapterListData(final String bookId) {
        return Observable.create(new ObservableOnSubscribe<List<com.quduquxie.bean.Chapter>>() {
            @Override
            public void subscribe(ObservableEmitter<List<com.quduquxie.bean.Chapter>> e) throws Exception {
                e.onNext(DataService.getChapterList(null, bookId, 1, Integer.MAX_VALUE - 1, mUdId));
                e.onComplete();
            }
        }).map(new Function<List<com.quduquxie.bean.Chapter>, List<Chapter>>() {
            @Override
            public ArrayList<Chapter> apply(List<com.quduquxie.bean.Chapter> chapters) throws Exception {
                return BeanParser.buildOWNChapterList(chapters, 0, chapters.size());
            }
        });
    }

    /********************************************************************/
}
