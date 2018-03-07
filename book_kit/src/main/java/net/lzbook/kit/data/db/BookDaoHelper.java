package net.lzbook.kit.data.db;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.book.download.CacheManager;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.Bookmark;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.repair_books.bean.BookFix;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.BaseBookHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 维护book表
 */
public class BookDaoHelper {

    private static BookDaoHelper mInstance;
    private List<Book> books;
    private Dao mDao;
    private Context mContext;

    private BookDaoHelper(Context context) {

        mContext = context;
        mDao = Dao.getInstance(context);
        books = mDao.getBooks();
    }

    public synchronized static BookDaoHelper getInstance() {
        if (mInstance == null) {
            mInstance = new BookDaoHelper(BaseBookApplication.getGlobalContext());
        }
        return mInstance;
    }


    public synchronized ArrayList<Bookmark> getBookMarks(String book_id) {
        return mDao.getBookMarks(book_id);
    }

    public synchronized boolean isBookMarkExist(String book_id, int sequence, int offset, int type) {
        return mDao.isBookMarkExist(book_id, sequence, offset);
    }

    public synchronized void insertBookMark(Bookmark bookMark, int type) {
        mDao.insertBookMark(bookMark);
    }

    public synchronized void deleteBookMark(String book_id, int sequence, int offset, int type) {
        mDao.deleteBookMark(book_id, sequence, offset);
    }

    public void deleteBookMark(String book_id) {
        mDao.deleteBookMark(book_id);
    }

    public synchronized void deleteBookMark(ArrayList<Integer> ids, int type) {
        mDao.deleteBookMark(ids);
    }

    public synchronized boolean deleteAllBook() {
        return deleteBookCache(this.books);
    }

    public synchronized boolean deleteBookCache(List<Book> lsitBook) {
        this.books.clear();
        this.books = this.mDao.getBooks();
        for (int i = 0; i < lsitBook.size(); i++) {
            CacheManager.INSTANCE.remove(((Book) lsitBook.get(i)).book_id);
        }
        return lsitBook.size() > 0;
    }

    /**
     * 获取书架线上书籍
     */
    public synchronized ArrayList<Book> getBooksOnLineList() {
        ArrayList<Book> extendsBooks = new ArrayList<>();
        books.clear();
        books.addAll(mDao.getBooks());
        extendsBooks.addAll(books);

        Collections.sort(extendsBooks);
        return extendsBooks;
    }

    /**
     * 获取书架线上青果源的所有已完结的书籍
     */
    public synchronized ArrayList<Book> getBooksNotFinishQG() {
        ArrayList<Book> extendsBooks = new ArrayList<>();
        for (Book book : books) {
            if (Constants.QG_SOURCE.equals(book.site) && book.status != 2) {
                extendsBooks.add(book);
            }
        }
        Collections.sort(extendsBooks);
        return extendsBooks;
    }

    /**
     * 获取书架上自有书籍
     */
    public synchronized ArrayList<Book> getOwnBooksList() {
        ArrayList<Book> extendsBooks = new ArrayList<>();
        for (Book book : books) {
            if (!Constants.QG_SOURCE.equals(book.site) && !Constants.SG_SOURCE.equals(book.site)) {
                extendsBooks.add(book);
            }
        }
        Collections.sort(extendsBooks);
        return extendsBooks;
    }

    /**
     * 获取书架线上宜搜源的所有书籍
     */
    public synchronized ArrayList<Book> getBooksOnLineListYS() {
        ArrayList<Book> extendsBooks = new ArrayList<>();
        int size = books.size();
        for (int i = 0; i < size; i++) {
            Book iBook = books.get(i);
            if (Constants.YS_SOURCE.equals(iBook.site)) {
                extendsBooks.add(iBook);
            } else {
                BookChapterDao bookChapterDao = new BookChapterDao(mContext, iBook.book_id);
                Chapter firstChapter = bookChapterDao.getChapterBySequence(0);
                if (firstChapter != null && firstChapter.curl != null && firstChapter.curl.contains(Constants.FILTER_WORD)) {
                    extendsBooks.add(iBook);
                }
            }
        }
        Collections.sort(extendsBooks);
        return extendsBooks;
    }

    public synchronized ArrayList<Book> getBooksList() {
        ArrayList<Book> extendsBooks = new ArrayList<>();
        final int size = books.size();
        for (int i = 0; i < size; i++) {
            Book book = books.get(i);
            if (book.readed == 1) {
                extendsBooks.add(book);
            }
        }
        Collections.sort(extendsBooks);
        return extendsBooks;
    }

    /**
     * 获取书架线上书籍
     */
    public synchronized ArrayList<Book> getInitBooksOnLineList() {
        ArrayList<Book> extendsBooks = new ArrayList<>();
        int size = books.size();
        for (int i = 0; i < size; i++) {
            extendsBooks.add(books.get(i));
        }
        Collections.sort(extendsBooks);
        return extendsBooks;
    }

    public synchronized ArrayList<Book> getInitBooksList() {
        ArrayList<Book> extendsBooks = new ArrayList<>();
        final int size = books.size();
        for (int i = 0; i < size; i++) {
            Book book = books.get(i);
            if (book.readed == 1 && book.initialization_status == 0) {
                extendsBooks.add(book);
            }
        }
        Collections.sort(extendsBooks);
        return extendsBooks;
    }

    /**
     * 订阅书籍，并有50本的限制
     * book
     */
    public synchronized boolean insertBook(Book book) {
        int MAX_COUNT = 49;
        if (book == null || TextUtils.isEmpty(book.book_id) || TextUtils.isEmpty(book.book_source_id)) {
            Toast.makeText(mContext, "订阅失败，资源有误", Toast.LENGTH_SHORT).show();
            return false;
        }
        switch (book.book_type) {
            case Book.TYPE_ONLINE:
                Book tempBook = (Book) book;
                if (books.size() > MAX_COUNT) {
                    Toast.makeText(mContext, "书架已满，请整理书架", Toast.LENGTH_SHORT).show();
                    return false;
                } else if (books.contains(book)) {
                    Toast.makeText(mContext, "已在书架中", Toast.LENGTH_SHORT).show();
                    return false;
                } else if (TextUtils.isEmpty(tempBook.book_id) || tempBook.name == null || tempBook.name.equals("")) {
                    Toast.makeText(mContext, "订阅失败，资源有误", Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    // 这里是为了在已经有目录的前提下，添加到书架的时候引起的chapter_count不一致做的修复
                    if (AppUtils.isChapterDBexist(mContext, book.book_id)) {
                        mContext.deleteDatabase("localbook_chapter_" + book.book_id);
                    }
                    if (tempBook.dex == -1) {
                        tempBook.dex = 1;
                    }
                    if (mDao.insertBook(tempBook)) {
                        if (tempBook.sequence < -1) {
                            tempBook.sequence = -1;
                        }
                        books.add(tempBook);
                        return true;
                    } else {
                        return false;
                    }
                }
            default:
                return false;
        }
    }

    public synchronized boolean isBookSubed(String book_id) {
        int size = books.size();
        for (int i = 0; i < size; i++) {
            if (books.get(i) != null && !TextUtils.isEmpty(books.get(i).book_id)) {
                if (books.get(i).book_id.equals(book_id)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isBookSubed(int gid) {
        int size = books.size();
        for (int i = 0; i < size; i++) {
            if (books.get(i).gid == gid) {
                return true;
            }
        }
        return false;
    }

    public int getBooksCount() {
        return books.size();
    }

    /*****************重构的方法*****************/

    /**
     * 根据gid取book对象
     */
    public synchronized Book getBook(int gid, int type) {
        switch (type) {
            case 0:
                int size = books.size();
                for (int i = 0; i < size; i++) {
                    if (books.get(i).gid == gid) {
                        return books.get(i);
                    }
                }
                return mDao.getBook(gid);
            default:
                return new Book();
        }
    }


    /**
     * 根据parameter取book对象
     */
    public synchronized Book getBook(String parameter) {

        int size = books.size();
        for (int i = 0; i < size; i++) {
            if (books.get(i).parameter.equals(parameter)) {
                return books.get(i);
            }
        }
        return mDao.getBook(parameter, 0);

    }


    /**
     * 根据book_id取book对象
     */
    public synchronized Book getBook(String book_id, int type) {
        if (book_id == null) {
            return new Book();
        }
        switch (type) {
            case 0:
                int size = books.size();
                for (int i = 0; i < size; i++) {
                    if (book_id.equals(books.get(i).book_id)) {
                        return books.get(i);
                    }
                }
                return mDao.getBook(book_id);
            default:
                return new Book();
        }
    }

    /**
     * 根据gid更新书籍
     **/
    public synchronized boolean updateBook(Book book, int gid) {
        switch (book.book_type) {
            case 0:
                Book onLineBook = (Book) book;
                int index = -1;
                for (int i = 0; i < books.size(); i++) {
                    Book dbBook = books.get(i);
                    if (dbBook.gid == book.gid) {
                        index = i;
                    }
                }

                if (mDao.updateBook(onLineBook, gid) && index != -1) {
                    books.remove(index);
                    Book db_book = mDao.getBook(book.gid);
                    books.add(index, db_book);
                    return true;
                }
                break;
            default:
                break;
        }

        return false;
    }

    /**
     * 根据parameter更新书籍
     **/
    public synchronized boolean updateBook(Book book, String parameter) {
        switch (book.book_type) {
            case 0:
                Book onLineBook = book;
                int index = -1;
                for (int i = 0; i < books.size(); i++) {
                    Book dbBook = books.get(i);
                    if (!TextUtils.isEmpty(dbBook.parameter) && dbBook.parameter.equals(book.parameter)) {
                        index = i;
                    }
                }

                if (mDao.updateBook(onLineBook, parameter) && index != -1) {
                    books.remove(index);
                    Book db_book = mDao.getBook(book.parameter, 0);
                    books.add(index, db_book);
                    return true;
                }
                break;
            default:
                break;
        }

        return false;
    }

    /**
     * 根据book_id更新书籍
     **/
    public synchronized boolean updateBook(Book book) {
        switch (book.book_type) {
            case 0:
                Book onLineBook = book;
                int index = books.indexOf(onLineBook);
                if (mDao.updateBook(onLineBook) && index != -1) {
                    books.remove(onLineBook);
                    Book db_book = mDao.getBook(onLineBook.book_id);
                    books.add(index, db_book);
                    return true;
                }
                break;
            default:
                break;
        }
        return false;
    }

    public synchronized boolean updateBookNew(Book book) {
        switch (book.book_type) {
            case 0:
                Book onLineBook = book;
                int index = books.indexOf(onLineBook);
                if (mDao.updateBookNew(onLineBook) && index != -1) {
                    books.remove(onLineBook);
                    Book db_book = mDao.getBook(onLineBook.book_id);
                    books.add(index, db_book);
                    return true;
                }
                break;
            default:
                break;
        }
        return false;
    }

    public synchronized boolean deleteBook(Book book, boolean clearCache) {
        boolean z = true;
        synchronized (this) {
            String[] delete_ids = this.mDao.deleteSubBook(book.book_id);
            this.books.clear();
            this.books = this.mDao.getBooks();

            for (String remove : delete_ids) {
                if (clearCache) {
                    BaseBookHelper.removeChapterCacheFile(book);
                }

                if(remove != null){
                    CacheManager.INSTANCE.stop(remove);
                    CacheManager.INSTANCE.resetTask(remove);
                }
                this.mContext.deleteDatabase("book_chapter_" + book.book_id);
            }

            if (delete_ids.length <= 0) {
                z = false;
            }
        }
        return z;
    }

    public synchronized boolean deleteBook(List<Book> bookList) {
        for (Book b : bookList) {
            this.mDao.deleteSubBook(b.book_id);
        }
        this.books.clear();
        this.books = this.mDao.getBooks();
        for (int i = 0; i < bookList.size(); i++) {
            CacheManager.INSTANCE.remove(((Book) bookList.get(i)).book_id);
            this.mContext.deleteDatabase("book_chapter_" + bookList.get(i));

            BaseBookHelper.removeChapterCacheFile((Book) bookList.get(i));

            if (!TextUtils.isEmpty(this.mDao.getBookFix(((Book) bookList.get(i)).book_id).book_id)) {
                this.mDao.deleteBookFix(((Book) bookList.get(i)).book_id);
            }
        }
        return bookList.size() > 0;
    }


    /**
     * 新增修复书籍状态信息
     */
    public synchronized boolean insertBookFix(BookFix bookFix) {
        if (TextUtils.isEmpty(mDao.getBookFix(bookFix.book_id).book_id)) {
            return mDao.insertBookFix(bookFix);
        } else {
            return mDao.updateBookFix(bookFix);
        }
    }

    /**
     * 查询所有修复状态信息
     */
    public synchronized ArrayList<BookFix> getBookFixs() {
        return mDao.getBookFixs();
    }

    /**
     * 根据book_id查询修复状态信息
     */
    public synchronized BookFix getBookFix(String book_id) {
        return mDao.getBookFix(book_id);
    }

    /**
     * 根据book_id更新bookFix
     */
    public synchronized boolean updateBookFix(BookFix bookFix) {
        return mDao.updateBookFix(bookFix);
    }

    /**
     * 删除修复状态信息
     */
    public synchronized boolean deleteBookFix(String... book_id) {
        return mDao.deleteBookFix(book_id).length > 0;
    }

//    /**
//     * 新增搜索页推荐书籍
//     */
//    public synchronized void insertSearchBook(List<SearchRecommendBook.DataBean> recommendBooks) {
//        deleteSearchBooks();
//        for (int i = 0; i < recommendBooks.size(); i++) {
//            mDao.insertSearchBook(recommendBooks.get(i));
//        }
//
//    }
//
//    /**
//     * 获取搜索页推荐的书籍
//     */
//
//    public synchronized ArrayList<SearchRecommendBook.DataBean> getSearchBooks() {
//        return mDao.getSearchBooks();
//    }
//
//
//    /**
//     * 删除表里的数据
//     */
//
//    public synchronized void deleteSearchBooks() {
//        mDao.deleteSearchBook();
//    }

    public synchronized void deleteBook(String book_id) {
        this.mDao.deleteSubBook(book_id);
    }
}
