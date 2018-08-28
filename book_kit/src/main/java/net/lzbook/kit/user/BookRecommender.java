package net.lzbook.kit.user;

import android.util.Log;

import com.ding.basic.bean.Book;
import com.ding.basic.bean.RecommendBooksEndResp;
import com.ding.basic.bean.RecommendBooksResp;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 创建人：Zach
 * 创建时间：2017/11/4 0004
 * 推荐书籍管理者：负责书架和书末页的推荐书籍管理
 */
@Deprecated
public class BookRecommender {

    //智能书籍推荐策略
    private ZNRecommendBookStrategy mZnStrategy;
    //青果书籍推荐策略
    private QGRecommendBookStrategy mQgStrategy;
    //todo 付费书籍推荐策略

    //书架页推荐书籍的容器
    private ArrayList<Book> mBookListForBookshelf;
    private int mZnCount;
    private int mQgCount;

    //书末页推荐喜欢书籍的容器
    private ArrayList<Book> mBookListForBookend1;
    private int mZnCountEnd1;
    private int mQgCountEnd1;

    //书末页推荐新书籍的容器
    private ArrayList<Book> mBookListForBookend2;
    private int mZnCountEnd2;
    private int mQgCountEnd2;

    /**
     * @param rate 智能书籍和青果书籍的权重，由动态参数从自有后端中获取
     */
    public BookRecommender(RecommendBooksResp books, String rate){
        mBookListForBookshelf = new ArrayList<>();
        String[] split = rate.split(",");
        mZnCount = Integer.parseInt(split[0]);
        mQgCount = Integer.parseInt(split[1]);
        mZnStrategy =  new ZNRecommendBookStrategy(books);
        mQgStrategy =  new QGRecommendBookStrategy(books);
    }

    /**
     * @param rate 智能书籍和青果书籍的权重，由动态参数从自有后端中获取
     */
    public BookRecommender(RecommendBooksEndResp books, String rate){
        mBookListForBookend1 = new ArrayList<>();
        mBookListForBookend2 = new ArrayList<>();
        String[] split = rate.split(",");

        //添加容错判断 如果后台动态参数配置错误 则采用默认配比
        //付费项目需另外配置
        if(split.length != 4 ){
            split= new String[]{"1", "2", "1", "2"};
        }
        mZnCountEnd1 = Integer.parseInt(split[0]);
        mQgCountEnd1 = Integer.parseInt(split[1]);
        mZnCountEnd2 = Integer.parseInt(split[2]);
        mQgCountEnd2 = Integer.parseInt(split[3]);
        mZnStrategy =  new ZNRecommendBookStrategy(books);
        mQgStrategy =  new QGRecommendBookStrategy(books);
        Log.e("BookRecommender","书末页书籍比例："+ Arrays.toString(split));
    }


    /***********************书架页的书籍推荐****************************/

    /**
     * 获取分配好的Book集合
     */
    public ArrayList<Book> getRecommendBook(){
        if(mBookListForBookshelf!=null){
            mBookListForBookshelf.clear();
        }
        addZnBooks();
        addQgBooks();
        return mBookListForBookshelf;
    }

    private void addZnBooks(){
        for (int i = 0; i < mZnCount; i++) {
            mBookListForBookshelf.add(mZnStrategy.getRecommendBook());
        }
    }

    /**
     * //如果青果书籍不够则用智能书籍来填充
     */
    private void addQgBooks(){
        for (int i = 0; i < mQgCount; i++) {
            Book qgBook = mQgStrategy.getRecommendBook();
            if(qgBook==null){
                mBookListForBookshelf.add(mZnStrategy.getRecommendBook());
            }else{
                mBookListForBookshelf.add(qgBook);
            }

        }
    }

    /***********************书末页的喜欢书籍推荐****************************/

    /**
     * 获取分配好的Book集合
     */
    public ArrayList<Book> getRecommendBookendBooks1(){
        if(mBookListForBookend1 == null){
            return null;
        }
        if(!mBookListForBookend1.isEmpty()){
            mBookListForBookend1.clear();
        }

        for (int i = 0; i < mZnCountEnd1; i++) {
            Book bookendRecommendBook = mZnStrategy.getBookendRecommendBook();
            if(bookendRecommendBook==null){
                return null;
            }
            mBookListForBookend1.add(bookendRecommendBook);
        }

        for (int i = 0; i < mQgCountEnd1; i++) {
            Book bookendRecommendBook = mQgStrategy.getBookendRecommendBook();
            if(bookendRecommendBook==null){
                return null;
            }
            mBookListForBookend1.add(bookendRecommendBook);
        }

        return mBookListForBookend1;
    }


    /***********************书末页的新书推荐****************************/

    /**
     * 获取分配好的Book集合
     */
    public ArrayList<Book> getRecommendBookendBooks2(){
        if(mBookListForBookend2 == null){
            return null;
        }
        if(!mBookListForBookend2.isEmpty()){
            mBookListForBookend2.clear();
        }

        for (int i = 0; i < mZnCountEnd2; i++) {
            Book bookendRecommendBook = mZnStrategy.getBookendRecommendBook();
            if(bookendRecommendBook==null){
                return null;
            }
            mBookListForBookend2.add(bookendRecommendBook);
        }

        for (int i = 0; i < mQgCountEnd2; i++) {
            Book bookendRecommendBook = mQgStrategy.getBookendNewBook();
            if(bookendRecommendBook==null){
                return null;
            }
            mBookListForBookend2.add(bookendRecommendBook);
        }

        return mBookListForBookend2;
    }
}