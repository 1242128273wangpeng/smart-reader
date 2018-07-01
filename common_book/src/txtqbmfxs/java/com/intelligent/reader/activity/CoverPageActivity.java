//package com.intelligent.reader.activity;
//
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.res.Resources;
//import android.graphics.drawable.GradientDrawable;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.support.v7.widget.RecyclerView;
//import android.support.v7.widget.SimpleItemAnimator;
//import android.text.TextUtils;
//import android.view.Gravity;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ListView;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.alibaba.android.arouter.facade.annotation.Route;
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.load.engine.DiskCacheStrategy;
//import com.ding.basic.bean.Book;
//import com.ding.basic.repository.RequestRepositoryFactory;
//import com.dingyue.bookshelf.ShelfGridLayoutManager;
//import com.dingyue.contract.router.RouterConfig;
//import com.intelligent.reader.R;
//import com.intelligent.reader.adapter.CoverRecommendAdapter;
//import com.intelligent.reader.adapter.CoverSourceAdapter;
//import com.intelligent.reader.view.ExpandableTextView;
//import com.intelligent.reader.view.MyScrollView;
//
//import net.lzbook.kit.app.BaseBookApplication;
//import net.lzbook.kit.appender_loghub.StartLogClickUtil;
//import net.lzbook.kit.book.download.CacheManager;
//import net.lzbook.kit.book.download.DownloadState;
//import net.lzbook.kit.book.view.LoadingPage;
//import net.lzbook.kit.book.view.MyDialog;
//import net.lzbook.kit.book.view.RecommendItemView;
//import net.lzbook.kit.constants.Constants;
//import net.lzbook.kit.constants.ReplaceConstants;
//import net.lzbook.kit.data.bean.CoverPage;
//import net.lzbook.kit.data.db.help.ChapterDaoHelper;
//import net.lzbook.kit.data.recommend.CoverRecommendBean;
//import net.lzbook.kit.request.own.OWNParser;
//import net.lzbook.kit.utils.AppLog;
//import net.lzbook.kit.utils.AppUtils;
//import net.lzbook.kit.utils.BaseBookHelper;
//import net.lzbook.kit.utils.BookCoverUtil;
//import net.lzbook.kit.utils.NetWorkUtils;
//import net.lzbook.kit.utils.OpenUDID;
//import net.lzbook.kit.utils.StatServiceUtils;
//import net.lzbook.kit.utils.Tools;
//import net.lzbook.kit.utils.oneclick.AntiShake;
//
//import org.json.JSONException;
//
//import java.lang.ref.WeakReference;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//import java.util.concurrent.Callable;
//
//import io.reactivex.Observable;
//import io.reactivex.ObservableEmitter;
//import io.reactivex.ObservableOnSubscribe;
//import io.reactivex.Observer;
//import io.reactivex.android.schedulers.AndroidSchedulers;
//import io.reactivex.disposables.Disposable;
//import io.reactivex.functions.Consumer;
//import io.reactivex.schedulers.Schedulers;
//import iyouqu.theme.BaseCacheableActivity;
//
//@Route(path = RouterConfig.COVER_PAGE_ACTIVITY)
//public class CoverPageActivity extends BaseCacheableActivity implements OnClickListener,
//        BookCoverUtil
//                .OnDownloadState, BookCoverUtil.OnDownLoadService,
//        MyScrollView.ScrollChangedListener, CoverRecommendAdapter.RecommendItemClickListener {
//
//    final static int DOWNLOADING = 0x10;
//    final static int NO_DOWNLOAD = DOWNLOADING + 1;
//    final static int DOWNLOAD_FINISH = NO_DOWNLOAD + 1;
//    final static int DATA_OK = DOWNLOAD_FINISH + 1;
//    final static int DATA_ERROR = DATA_OK + 1;
//    public final static int MESSAGE_SHOW_TIEBA = DATA_ERROR + 1;
//    final static int COLLECT_DATA_OK = MESSAGE_SHOW_TIEBA + 1;
//    final static int COLLECT_DATA_ERROR = COLLECT_DATA_OK + 1;
//    final static int UN_COLLECT_DATA_OK = COLLECT_DATA_ERROR + 1;
//    final static int UN_COLLECT_DATA_ERROR = UN_COLLECT_DATA_OK + 1;
//    final static int HAD_COLLECT_DATA_OK = UN_COLLECT_DATA_ERROR + 1;
//    final static int HAD_COLLECT_DATA_ERROR = HAD_COLLECT_DATA_OK + 1;
//    final static int GET_CATEGORY_OK = HAD_COLLECT_DATA_ERROR + 1;
//    final static int GET_CATEGORY_ERROR = GET_CATEGORY_OK + 1;
//    UIHandler uiHandler = new UIHandler(this);
//    //private RelativeLayout book_cover_reading_view;
//    private ImageView book_cover_back;
//    private MyScrollView book_cover_content;
//    private ImageView book_cover_image;
//    private TextView book_cover_title;
//    private TextView book_cover_author;
//    private TextView book_cover_category;
//    private TextView book_cover_category2;
//    private TextView book_cover_status;
//    private TextView book_cover_update_time;
//    private LinearLayout book_cover_source_view;
//    private TextView book_cover_source_form;
//    private ViewGroup book_cover_chapter_view;
//    private TextView book_cover_last_chapter;
//
//    private TextView book_cover_bookshelf;
//    private TextView book_cover_reading;
//
//    private TextView book_cover_download;
//    private int mBackground = 0;
//    private int mTextColor = 0;
//    private ExpandableTextView book_cover_description;
//    private RelativeLayout book_cover_catalog_view;
//    private RelativeLayout book_cover_catalog_view_nobg;
//
//    private ChapterDaoHelper bookDaoHelper;
//    private BookCoverUtil bookCoverUtil;
//    private LoadingPage loadingPage;
//
//    private List<CoverPage.SourcesBean> bookSourceList = new ArrayList<>();
//
//    private Book requestItem;
//
//    private MyDialog coverSourceDialog;
//    private MyDialog readingSourceDialog;
//
//    private CoverPage.SourcesBean currentSource;
//    private MyDialog confirm_change_source_dialog;
//    private RecyclerView recycler_view;
//    private ArrayList<Book> books = new ArrayList<>();
//    private List<Book> mRecommendBooks = new ArrayList<>();
//    private Random mRandom;
//    private List<Integer> markIndexs = new ArrayList<>();//用于标记推荐书籍
//    private ShelfGridLayoutManager layoutManager;
//    private CoverRecommendAdapter coverRecommendAdapter;
//    private TextView tv_recommend_title;
//    private TextView tv_title;
//
//
//    private CoverPage.BookVoBean bookVo;
//    private SharedPreferences preferences;
//    private AntiShake shake = new AntiShake();
//    private boolean isShouldRegist = true;//推荐位点击时走onNewIntent 防止多次注册广播
//
//    public static void launcher(Context context, final String host, final String book_id,
//            final String book_source_id, final String name, final String author, final
//    String parameter, final String extra_parameter) {
//        Book requestItem = new Book();
//        requestItem.setBook_id(book_id);
//        requestItem.setBook_source_id(book_source_id);
//        requestItem.setHost(host);
//        requestItem.setName(name);
//        requestItem.setAuthor(author);
//
//        Intent intent = new Intent();
//        intent.setClass(context, CoverPageActivity.class);
//        Bundle bundle = new Bundle();
//        try {
//            bundle.putSerializable(Constants.REQUEST_ITEM, requestItem);
//            intent.putExtras(bundle);
//            context.startActivity(intent);
//        } catch (ClassCastException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        StatServiceUtils.statAppBtnClick(this, StatServiceUtils.cover_into);
//        setContentView(R.layout.act_book_cover);
//        preferences = getSharedPreferences(
//                "onlineconfig_agent_online_setting_" + AppUtils.getPackageName(), 0);
//
//        initView();
//        initData(getIntent());
//        initListener();
//
//    }
//
//    protected void initView() {
//        book_cover_back =  findViewById(R.id.book_cover_back);
//        book_cover_content =  findViewById(R.id.book_cover_content);
//        book_cover_image =  findViewById(R.id.book_cover_image);
//        book_cover_title =  findViewById(R.id.book_cover_title);
//        book_cover_author =  findViewById(R.id.book_cover_author);
//        book_cover_category =  findViewById(R.id.book_cover_category);
//        book_cover_category2 =  findViewById(R.id.book_cover_category2);
//        book_cover_status =  findViewById(R.id.book_cover_status);
//        book_cover_update_time =  findViewById(R.id.book_cover_update_time);
//
//        book_cover_source_view =  findViewById(R.id.book_cover_source_view);
//        book_cover_source_form =  findViewById(R.id.book_cover_source_form);
//
//        book_cover_chapter_view =  findViewById(R.id.book_cover_chapter_view);
//        book_cover_last_chapter =  findViewById(R.id.book_cover_last_chapter);
//
//        //book_cover_bookshelf_view = (RelativeLayout) findViewById(R.id.book_cover_bookshelf_view);
//        book_cover_bookshelf =  findViewById(R.id.book_cover_bookshelf);
//
//        // book_cover_reading_view = (RelativeLayout) findViewById(R.id.book_cover_reading_view);
//        book_cover_reading =  findViewById(R.id.book_cover_reading);
//        //book_cover_download_view = (RelativeLayout) findViewById(R.id.book_cover_download_view);
//        book_cover_download =  findViewById(R.id.book_cover_download);
//
//        book_cover_description = (ExpandableTextView) findViewById(R.id.book_cover_description);
//        book_cover_catalog_view = (RelativeLayout) findViewById(R.id.book_cover_catalog_view);
//
//        book_cover_catalog_view_nobg = (RelativeLayout) findViewById(
//                R.id.book_cover_catalog_view_nobg);
//        recycler_view = (RecyclerView) findViewById(R.id.recycler_view);
//        tv_recommend_title = (TextView) findViewById(R.id.tv_recommend_title);
//        tv_title = (TextView) findViewById(R.id.tv_title);
//        mRandom = new Random();
//
//    }
//
//    @Override
//    protected void onNewIntent(Intent intent) {
//
//        RequestRepositoryFactory factory = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext());
//        Book book = factory.checkBookSubscribe(requestItem.getBook_id());
//
//        if (book != null) {
//            if (book_cover_bookshelf != null) {
////                book_cover_bookshelf.setText(R.string.book_cover_remove_bookshelf);
//                setRemoveBtn();
//            }
//        } else {
//            if (book_cover_bookshelf != null) {
//                setAddShelfBtn();
//            }
//        }
//        isShouldRegist = false;
//        initData(intent);
//    }
//
//    protected void initListener() {
//        if (book_cover_back != null) {
//            book_cover_back.setOnClickListener(this);
//        }
//
//        if (book_cover_author != null) {
//            book_cover_author.setOnClickListener(this);
//        }
//
//        if (book_cover_source_view != null) {
//            book_cover_source_view.setOnClickListener(this);
//        }
//
//        if (book_cover_chapter_view != null) {
//            book_cover_chapter_view.setOnClickListener(this);
//        }
//
//        if (book_cover_last_chapter != null) {
//            book_cover_last_chapter.setOnClickListener(this);
////            setDebounceClickListener(book_cover_last_chapter, this, 1000);
//        }
//
//        if (book_cover_bookshelf != null) {
//            book_cover_bookshelf.setOnClickListener(this);
//        }
//
//        if (book_cover_reading != null) {
//            book_cover_reading.setOnClickListener(this);
//        }
//
//        if (book_cover_download != null) {
//            book_cover_download.setOnClickListener(this);
//        }
//
//        if (book_cover_catalog_view != null) {
//            book_cover_catalog_view.setOnClickListener(this);
//        }
//
//        if (book_cover_catalog_view_nobg != null) {
//            book_cover_catalog_view_nobg.setOnClickListener(this);
//        }
//
//    }
//
//    private void initRecyclerView() {
//
//        if (tv_recommend_title != null) {
//            if (mRecommendBooks.size() == 0) {
//                tv_recommend_title.setVisibility(View.GONE);
//            } else {
//                tv_recommend_title.setVisibility(View.VISIBLE);
//            }
//        }
//        if (recycler_view != null) {
//            coverRecommendAdapter = new CoverRecommendAdapter(this, this, mRecommendBooks);
//            recycler_view.getRecycledViewPool().setMaxRecycledViews(0, 12);
//            layoutManager = new ShelfGridLayoutManager(this, 3);
//
//            recycler_view.setLayoutManager(layoutManager);
//            recycler_view.setNestedScrollingEnabled(false);
//            recycler_view.getItemAnimator().setAddDuration(0);
//            recycler_view.getItemAnimator().setChangeDuration(0);
//            recycler_view.getItemAnimator().setMoveDuration(0);
//            recycler_view.getItemAnimator().setRemoveDuration(0);
//            ((SimpleItemAnimator) recycler_view.getItemAnimator()).setSupportsChangeAnimations(
//                    false);
//            recycler_view.setAdapter(coverRecommendAdapter);
//        }
//
//
//    }
//
//
//    /**
//     * 获取封面信息
//     */
//    public void getOwnBookCover(Book requestItem) {
//        if (requestItem != null && requestItem.getBook_id() != null
//                && requestItem.getBook_source_id() != null) {
//            RequestRepositoryFactory
//                    .Companion
//                    .loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
//                    .requestBookRecommend()
//            NetService.INSTANCE.getOwnBookService().requestBookCover(requestItem.book_id,
//                    requestItem.book_source_id)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Observer<String>() {
//                        @Override
//                        public void onSubscribe(Disposable d) {
//
//                        }
//
//                        @Override
//                        public void onNext(String value) {
//                            try {
//                                if (uiHandler == null) return;
//                                Message msg = uiHandler.obtainMessage();
//                                msg.what = REQUEST_COVER_SUCCESS;
//                                msg.obj = OWNParser.parserOwnCoverInfo(value);
//                                uiHandler.sendMessage(msg);
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onError(Throwable e) {
//                            if (uiHandler == null) return;
//                            Message msg = uiHandler.obtainMessage();
//                            msg.what = REQUEST_COVER_ERROR;
//                            msg.obj = e;
//                            uiHandler.sendMessage(msg);
//                        }
//
//                        @Override
//                        public void onComplete() {
//
//                        }
//                    });
//        }
//    }
//
//    protected void initData(Intent intent) {
//
//        if (intent != null) {
//
//            if (intent.hasExtra(Constants.REQUEST_ITEM)) {
//                requestItem = (RequestItem) intent.getSerializableExtra(Constants.REQUEST_ITEM);
//            }
//        }
//
//        if (bookDaoHelper == null) {
//            bookDaoHelper = BookDaoHelper.getInstance();
//        }
//
//        if (bookCoverUtil == null) {
//            bookCoverUtil = new BookCoverUtil(CoverPageActivity.this, this);
//        }
//        if (isShouldRegist) {
//            bookCoverUtil.registReceiver();
//        }
//
//        bookCoverUtil.setOnDownloadState(this);
//        bookCoverUtil.setOnDownLoadService(this);
//
//        loadCoverInfo();
//        if (bookDaoHelper != null && requestItem != null) {
//            getRecommendBook(requestItem, getBookOnLineIds(bookDaoHelper));
//        }
//    }
//
//    /**
//     * 获取书架上的书Id
//     */
//    public String getBookOnLineIds(BookDaoHelper bookDaoHelper) {
//        if (bookDaoHelper != null) {
//            books.clear();
//            books = bookDaoHelper.getBooksOnLineList();
//            StringBuilder sb = new StringBuilder();
//            if (books != null && books.size() > 0) {
//                for (int i = 0; i < books.size(); i++) {
//                    Book book = books.get(i);
//                    sb.append(book.book_id);
//                    sb.append((i == books.size() - 1) ? "" : ",");
//                }
//                return sb.toString();
//            }
//        }
//        return "";
//    }
//
//    protected void loadCoverInfo() {
//
//        if (loadingPage != null) {
//            loadingPage.onSuccess();
//        }
//
//        loadingPage = new LoadingPage(this, (ViewGroup) findViewById(R.id.book_cover_main),
//                LoadingPage.setting_result);
//
//
//        if (requestItem != null) {
//            AppLog.e("loadCoverInfo", "requestItem.host-->" + requestItem.host);
//            if (Constants.QG_SOURCE.equals(requestItem.host)) {//青果
//                requestItem.channel_code = 1;
//                RequestManager.init(getApplicationContext());
//                String udid = OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext());
//                DataService.getBookInfo(this, requestItem.book_id, uiHandler,
//                        RequestExecutor.REQUEST_COVER_QG_SUCCESS, RequestExecutor
//                                .REQUEST_COVER_QG_ERROR, udid);
//            } else {//自有
//                if (Constants.SG_SOURCE.equals(requestItem.host) || requestItem.book_id == null
//                        || requestItem.book_source_id == null) {
//                    uiHandler.sendEmptyMessage(REQUEST_COVER_ERROR);
//                } else {
//                    getOwnBookCover(requestItem);
//                }
//                requestItem.channel_code = 2;
//            }
//        }
//
//        if (loadingPage != null) {
//            loadingPage.setReloadAction(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    if (requestItem != null) {
//                        if (Constants.QG_SOURCE.equals(requestItem.host)) {//青果
//                            RequestManager.init(getApplicationContext());
//                            String udid = OpenUDID.getOpenUDIDInContext(
//                                    BaseBookApplication.getGlobalContext());
//                            DataService.getBookInfo(CoverPageActivity.this, requestItem.book_id,
//                                    uiHandler, RequestExecutor
//                                            .REQUEST_COVER_QG_SUCCESS,
//                                    RequestExecutor.REQUEST_COVER_QG_ERROR, udid);
//                        } else {//自有
//                            if (Constants.SG_SOURCE.equals(requestItem.host)
//                                    || requestItem.book_id == null
//                                    || requestItem.book_source_id == null) {
//                                uiHandler.sendEmptyMessage(REQUEST_COVER_ERROR);
//                            } else {
//                                getOwnBookCover(requestItem);
//                            }
//                        }
//
//                    }
//                    return null;
//                }
//            });
//        }
//    }
//
//    /**
//     * 获取推荐的书
//     */
//    public void getRecommendBook(RequestItem requestItem, String bookIds) {
//        if (requestItem != null && requestItem.book_id != null) {
//            NetService.INSTANCE.getOwnBookService().requestCoverRecommend(requestItem.book_id,
//                    bookIds)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Observer<CoverRecommendBean>() {
//                        @Override
//                        public void onSubscribe(Disposable d) {
//                        }
//
//                        @Override
//                        public void onNext(CoverRecommendBean bean) {
//                            mRecommendBooks.clear();
//
//                            if (bean != null && bean.getData() != null
//                                    && bean.getData().getMap() != null) {
//                                tv_recommend_title.setVisibility(View.VISIBLE);
//                                if (preferences == null) {
//                                    preferences = CoverPageActivity.this.getSharedPreferences(
//                                            "onlineconfig_agent_online_setting_"
//                                                    + AppUtils.getPackageName(), 0);
//                                }
//                                String[] scale = preferences.getString(
//                                        Constants.RECOMMEND_BOOKCOVER, "3,3,0").split(",");
//                                if (scale != null && scale.length >= 2) {
//                                    if (!TextUtils.isEmpty(scale[0])) {
//                                        addZNBooks(bean, Integer.parseInt(scale[0]));
//                                    }
//                                    if (!TextUtils.isEmpty(scale[1])) {
//                                        addQGBooks(bean, Integer.parseInt(scale[1]));
//                                    }
//
//                                }
//                                initRecyclerView();
//
//                            } else {
//                                if (tv_recommend_title != null) {
//                                    tv_recommend_title.setVisibility(View.GONE);
//                                }
//                            }
//                        }
//
//                        @Override
//                        public void onError(Throwable e) {
//                            if (tv_recommend_title != null) {
//                                tv_recommend_title.setVisibility(View.GONE);
//                            }
//                        }
//
//                        @Override
//                        public void onComplete() {
//
//                        }
//                    });
//        }
//
//    }
//
//    public void addBooks(CoverRecommendBean bean, int index, boolean isZnBook) {
//        if (isZnBook) {
//            Book book = new Book();
//            CoverRecommendBean.DataBean.MapBean.ZnListBean znBean =
//                    bean.getData().getMap().getZnList().get(index);
//            if (requestItem != null && !requestItem.book_id.equals(znBean.getBookId())) {
//                if (znBean.getSerialStatus().equals("FINISH")) {
//                    book.status = 2;
//                } else {
//                    book.status = 1;
//                }
//
//                book.book_id = znBean.getBookId();
//                book.book_source_id = znBean.getId();
//                book.name = znBean.getBookName();
//                book.category = znBean.getLabel();
//                book.author = znBean.getAuthorName();
//                book.img_url = znBean.getSourceImageUrl();
//                book.site = znBean.getHost();
//                book.last_chapter_name = znBean.getLastChapterName() + "";
//                book.chapter_count = Integer.valueOf(znBean.getChapterCount());
//                book.last_updatetime_native = znBean.getUpdateTime();
//                book.dex = znBean.getDex();
//                book.last_updateSucessTime = System.currentTimeMillis();
//                book.readPersonNum = znBean.getReaderCountDescp() + "";
//                mRecommendBooks.add(book);
//
//            }
//        } else {
//            Book book = new Book();
//            CoverRecommendBean.DataBean.MapBean.QgListBean qgBean =
//                    bean.getData().getMap().getQgList().get(index);
//            if (requestItem != null && !requestItem.book_id.equals(qgBean.getId())) {
//                if (qgBean.getSerialStatus().equals("FINISH")) {
//                    book.status = 2;
//                } else {
//                    book.status = 1;
//                }
//
//                book.book_id = qgBean.getId();
//                book.book_source_id = qgBean.getBookSourceId();
//                book.name = qgBean.getBookName();
//                book.category = qgBean.getLabels();
//                book.author = qgBean.getAuthor_name();
//                book.img_url = qgBean.getImage() + "";
//                book.site = qgBean.getHost() + "";
//                book.last_chapter_name = qgBean.getChapter_name() + "";
//                book.chapter_count = Integer.valueOf(qgBean.getChapter_sn());
//                book.last_updatetime_native = qgBean.getUpdate_time();
//                book.dex = 1;
//                book.last_updateSucessTime = System.currentTimeMillis();
//                book.readPersonNum = qgBean.getRead_count() + "";
//                mRecommendBooks.add(book);
//            }
//        }
//    }
//
//
//    /**
//     * 添加推荐的智能的书
//     */
//    public void addZNBooks(CoverRecommendBean bean, int znSize) {
//        int znIndex = -1;
//        markIndexs.clear();
//        if (bean.getData().getMap().getZnList() != null
//                && bean.getData().getMap().getZnList().size() > 0) {
//            if (bean.getData().getMap().getZnList().size() < znSize) {
//                for (int i = 0; i < bean.getData().getMap().getZnList().size(); i++) {
//                    znIndex = i;
//                    markIndexs.add(znIndex);
//                    addBooks(bean, znIndex, true);
//                }
//
//            } else {
//                for (int i = 0; i < znSize; i++) {//推荐位 智能只取 3本
//                    znIndex = mRandom.nextInt(bean.getData().getMap().getZnList().size());
//                    if (markIndexs.contains(znIndex)) {
//                        while (true) {
//                            znIndex = mRandom.nextInt(bean.getData().getMap().getZnList().size());
//                            if (!markIndexs.contains(znIndex)) {
//                                break;
//                            }
//                        }
//                    }
//                    markIndexs.add(znIndex);
//                    addBooks(bean, znIndex, true);
//                }
//            }
//
//        }
//    }
//
//    /**
//     * 添加推荐的青果的书
//     */
//    public void addQGBooks(CoverRecommendBean bean, int qgSize) {
//        markIndexs.clear();
//        int qgIndex = -1;
//        if (bean.getData().getMap().getQgList() != null
//                && bean.getData().getMap().getQgList().size() > 0) {
//            if (bean.getData().getMap().getQgList().size() < qgSize) {
//                for (int i = 0; i < bean.getData().getMap().getQgList().size(); i++) {
//                    qgIndex = i;
//                    markIndexs.add(qgIndex);
//                    addBooks(bean, qgIndex, false);
//                }
//            } else {
//                for (int i = 0; i < qgSize; i++) {//推荐位 青果只取 3本
//                    qgIndex = mRandom.nextInt(bean.getData().getMap().getQgList().size());
//                    if (markIndexs.contains(qgIndex)) {
//                        while (true) {
//                            qgIndex = mRandom.nextInt(bean.getData().getMap().getQgList().size());
//                            if (!markIndexs.contains(qgIndex)) {
//                                break;
//                            }
//                        }
//                    }
//                    markIndexs.add(qgIndex);
//                    addBooks(bean, qgIndex, false);
//                }
//            }
//
//        }
//
//
//    }
//
//    private void setRemoveBtn() {
//        if (book_cover_category2 != null && book_cover_bookshelf != null) {
//            mBackground = R.drawable.cover_bottom_btn_remove_bg;
//            mTextColor = R.color.cover_bottom_btn_remove_text_color;
//            book_cover_bookshelf.setText(R.string.book_cover_remove_bookshelf);
//            book_cover_bookshelf.setTextColor(getResources().getColor(mTextColor));
//            if (book_cover_category2.getVisibility() != View.VISIBLE) {
//                book_cover_bookshelf.setBackgroundResource(mBackground);
//            }
//        }
//    }
//
//    private void setAddShelfBtn() {
//        if (book_cover_category2 != null && book_cover_bookshelf != null) {
//            mBackground = R.drawable.cover_bottom_btn_add_bg;
//            mTextColor = R.color.cover_bottom_btn_add_text_color;
//            book_cover_bookshelf.setText(R.string.book_cover_add_bookshelf);
//            book_cover_bookshelf.setTextColor(getResources().getColor(mTextColor));
//            if (book_cover_category2.getVisibility() != View.VISIBLE) {
//                book_cover_bookshelf.setBackgroundResource(mBackground);
//            }
//        }
//
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (bookDaoHelper == null || requestItem == null) {
//            return;
//        }
//
//        changeDownloadButtonStatus();
//
//        if (bookDaoHelper.isBookSubed(requestItem.book_id)) {
//            if (book_cover_bookshelf != null) {
////                book_cover_bookshelf.setText(R.string.book_cover_remove_bookshelf);
//                setRemoveBtn();
//            }
//        } else {
//            if (book_cover_bookshelf != null) {
//                setAddShelfBtn();
//            }
//        }
//    }
//
//    private void changeDownloadButtonStatus() {
//        if (book_cover_download == null) {
//            return;
//        }
//        Book book = null;
//        if (bookCoverUtil != null) {
//            book = bookCoverUtil.getCoverBook(bookDaoHelper, bookVo);
//        }
//        if (book != null && book_cover_download != null) {
//            DownloadState status = CacheManager.INSTANCE.getBookStatus(book);
//            if (status == DownloadState.FINISH) {
//                book_cover_download.setText(R.string.download_status_complete);
//            } else if (status == DownloadState
//                    .WAITTING || status == DownloadState.DOWNLOADING) {
//                book_cover_download.setText(R.string.download_status_underway);
//            } else {
//                book_cover_download.setText(R.string.download_status_total);
//            }
//        }
//    }
//
//    private void handCategoryError() {
//        if (loadingPage != null) {
//            loadingPage.onSuccess();
//        }
//        Toast.makeText(CoverPageActivity.this, "请求失败", Toast.LENGTH_SHORT).show();
//    }
//
//    private void handCategoryOk() {
//        if (loadingPage != null) {
//            loadingPage.onSuccess();
//        }
//    }
//
//    private void handleOK(Object objects, boolean isQG) {
//
//        //bookCover = ((CoverPage) objects).bookCover;
//        if (isQG) {//如果是青果的数据，就先进行一次类型转换
//            bookVo = parseToBookVoBean((BookMode) objects);
//            book_cover_source_form.setText("青果阅读");
//            book_cover_source_form.setCompoundDrawables(null, null, null, null);
//        } else if (objects != null) {
//            bookVo = ((CoverPage) objects).bookVo;
//
//            if (bookVo != null) {
//
//
//                if (requestItem != null && !TextUtils.isEmpty(requestItem.book_id)) {
//                    bookVo.book_id = requestItem.book_id;
//                }
//
//                if (requestItem != null && !TextUtils.isEmpty(requestItem.book_source_id)) {
//                    bookVo.book_source_id = requestItem.book_source_id;
//                }
//
//                if (requestItem != null && !TextUtils.isEmpty(requestItem.host)) {
//                    bookVo.host = requestItem.host;
//                }
//                if (requestItem != null) {
//                    bookVo.dex = requestItem.dex;
//                }
//
//            /*if (bookVo != null && !TextUtils.isEmpty(bookVo.book_id)) {
//                requestItem.book_id = bookVo.book_id;
//            }
//
//            if (bookVo != null && !TextUtils.isEmpty(bookVo.book_source_id)) {
//                requestItem.book_source_id = bookVo.book_source_id;
//            }
//
//            if (bookVo != null && !TextUtils.isEmpty(bookVo.host)) {
//                requestItem.host = bookVo.host;
//            }
//            if (bookVo != null) {
//                requestItem.dex = bookVo.dex;
//            }*/
//                List<CoverPage.SourcesBean> sources = ((CoverPage) objects).sources;
//
//                if (sources.size() < 2) {
//                    book_cover_source_form.setCompoundDrawables(null, null, null, null);
//                }
//
//                if (bookSourceList == null) {
//                    bookSourceList = new ArrayList<>();
//                }
//                bookSourceList.clear();
//
//
//                bookSourceList.addAll(sources);
//                for (int i = 0; i < bookSourceList.size(); i++) {
//                    CoverPage.SourcesBean source = bookSourceList.get(i);
//                    if (requestItem.book_source_id.equals(source.book_source_id)) {
//                        currentSource = source;
//                    }
//                }
//
//                if (book_cover_source_form != null) {
//                    if (currentSource != null && !TextUtils.isEmpty(currentSource.host)) {
//                        book_cover_source_form.setText(currentSource.host);
//                    } else {
//                        if (bookSourceList != null && bookSourceList.size() > 0) {
//                            book_cover_source_form.setText(bookSourceList.get(0).host);
//                            currentSource = bookSourceList.get(0);
//                        }
//                    }
//                }
//
//            }
//        }
//
//        book_cover_source_form.setCompoundDrawables(null, null, null, null);
//
//        if (bookVo != null && bookCoverUtil != null) {
//            bookCoverUtil.saveHistory(bookVo);
//        }
//
//        upDateUI();
//
//        if (loadingPage != null) {
//            loadingPage.onSuccess();
//        }
//        changeDownloadButtonStatus();
////        initGuide();
//    }
//
//    @Override
//    public void onTaskStatusChange() {
//        super.onTaskStatusChange();
//        changeDownloadButtonStatus();
//    }
//
//    private CoverPage.BookVoBean parseToBookVoBean(BookMode bookMode) {
//        //防止青果后端书籍出错导致的封面页崩溃问题
//        if (bookMode.model == null) {
//            handleError();
//            return null;
//        }
//        CoverPage.BookVoBean bookVoBean = new CoverPage.BookVoBean();
//        bookVoBean.book_source_id = Constants.QG_SOURCE;
//        bookVoBean.host = Constants.QG_SOURCE;
//        bookVoBean.book_id = bookMode.model.id_book;
//        bookVoBean.name = bookMode.model.name;
//        bookVoBean.author = bookMode.model.penname;
//        if (!TextUtils.isEmpty(bookMode.model.attribute_book)) {
//            bookVoBean.status = bookMode.model.attribute_book.equals("serialize") ? 1 : 2;
//        }
//        bookVoBean.last_chapter_name = bookMode.model.id_last_chapter_name;
//        bookVoBean.serial_number = bookMode.model.id_last_chapter_serial_number;//总章数
//        bookVoBean.img_url = bookMode.model.image_book;
//        bookVoBean.labels = bookMode.model.category;
//        bookVoBean.desc = bookMode.model.description;
//        bookVoBean.update_time = bookMode.model.id_last_chapter_create_time;
//
//        return bookVoBean;
//    }
//
//    private void handleError() {
//        if (loadingPage != null) {
//            loadingPage.onError();
//        }
//        Toast.makeText(CoverPageActivity.this, "请求失败", Toast.LENGTH_SHORT).show();
//    }
//
//    private void upDateUI() {
//        book_cover_content.smoothScrollTo(0, 0);
//        if (bookVo != null) {
//
//            if (book_cover_image != null && !TextUtils.isEmpty(bookVo.img_url) && !bookVo
//                    .img_url.equals(ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL)) {
//                Glide.with(getApplicationContext()).load(bookVo.img_url).placeholder(
//                        net.lzbook.kit.R.drawable.icon_book_cover_default).error(
//                        (net.lzbook.kit.R.drawable.icon_book_cover_default)).diskCacheStrategy(
//                        DiskCacheStrategy.ALL).into(book_cover_image);
//            } else {
//                Glide.with(getApplicationContext()).load(
//                        net.lzbook.kit.R.drawable.icon_book_cover_default).into(book_cover_image);
//            }
//
//            if (book_cover_title != null && !TextUtils.isEmpty(bookVo.name)) {
//                book_cover_title.setText(bookVo.name);
//            }
//
//            if (book_cover_author != null && !TextUtils.isEmpty(bookVo.author)) {
//                book_cover_author.setText(bookVo.author);
//            }
//
//            if (book_cover_category != null && !TextUtils.isEmpty(bookVo.labels)) {
//                book_cover_category.setText(bookVo.labels);
//            }
//
//            if (book_cover_category2 != null && !TextUtils.isEmpty(bookVo.labels)) {
//                book_cover_category2.setText(bookVo.labels);
//                if (!mThemeHelper.isNight()) {
//                    book_cover_category2.setBackgroundResource(R.drawable.book_cover_label_bg);
//                    GradientDrawable background =
//                            (GradientDrawable) book_cover_category2.getBackground();
//                    background.setColor(getResources().getColor(R.color.color_white_ffffff));
//                    book_cover_category2.setTextColor(AppUtils.getRandomColor());
//                } else {
//                    book_cover_category2.setTextColor(AppUtils.getRandomColor());
//                }
//            }
//
//            if (1 == bookVo.status) {
//                if (book_cover_category2.getVisibility() != View.VISIBLE) {
//                    book_cover_status.setText("—" + getString(R.string.book_cover_state_writing));
//                } else {
//                    book_cover_status.setText(getString(R.string.book_cover_state_writing));
//                    if (!mThemeHelper.isNight()) {
//                        book_cover_status.setBackgroundResource(R.drawable.book_cover_label_bg);
//                        GradientDrawable background =
//                                (GradientDrawable) book_cover_status.getBackground();
//                        background.setColor(getResources().getColor(R.color.color_white_ffffff));
//                        book_cover_status.setTextColor(
//                                getResources().getColor(R.color.color_red_ff2d2d));
//                    } else {
//                        book_cover_status.setTextColor(
//                                getResources().getColor(R.color.color_red_ff5656));
//                    }
//                }
//            } else {
//                if (book_cover_category2.getVisibility() != View.VISIBLE) {
//                    book_cover_status.setText("—" + getString(R.string.book_cover_state_written));
//                } else {
//                    book_cover_status.setText(getString(R.string.book_cover_state_written));
//                    if (!mThemeHelper.isNight()) {
//                        book_cover_status.setBackgroundResource(R.drawable.book_cover_label_bg);
//                        GradientDrawable background =
//                                (GradientDrawable) book_cover_status.getBackground();
//                        background.setColor(getResources().getColor(R.color.color_white_ffffff));
//                        book_cover_status.setTextColor(
//                                getResources().getColor(R.color.color_brown_e9cfae));
//                    } else {
//                        book_cover_status.setTextColor(
//                                getResources().getColor(R.color.color_brown_e2bd8d));
//                    }
//
//                }
//            }
//
//            if (book_cover_update_time != null) {
//                book_cover_update_time.setText(Tools.compareTime(AppUtils.formatter, bookVo
//                        .update_time));
//            }
//
//            if (book_cover_last_chapter != null && bookVo != null && !TextUtils.isEmpty
//                    (bookVo.last_chapter_name)) {
//                book_cover_last_chapter.setText(bookVo.last_chapter_name);
//            }
//
//            if (bookVo.desc != null && !TextUtils.isEmpty(bookVo.desc)) {
//                book_cover_description.setText(bookVo.desc);
//            } else {
//                book_cover_description.setText(getResources().getString(R.string
//                        .book_cover_no_description));
//            }
//        } else {
//            showToastShort(R.string.book_cover_no_resource);
//            if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
//                finish();
//            }
//        }
//    }
//
//    @Override
//    public void onClick(View view) {
//        Intent intent = new Intent();
//
//        if (view instanceof RecommendItemView) {
//            RecommendItemView item = (RecommendItemView) view;
//            intent.putExtra("word", item.getTitle());
//            intent.putExtra("search_type", "0");
//            intent.putExtra("filter_type", "0");
//            intent.putExtra("filter_word", "ALL");
//            intent.putExtra("sort_type", "0");
//            intent.setClass(this, SearchBookActivity.class);
//            startActivity(intent);
//            return;
//        }
//
//        switch (view.getId()) {
//            case R.id.book_cover_back:
//                Map<String, String> data = new HashMap<>();
//                data.put("type", "1");
//                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE,
//                        StartLogClickUtil.BACK, data);
//                finish();
//                SearchBookActivity.isSatyHistory = true;
//                break;
//
////            case R.id.book_cover_source_view:
////                //书籍详情页换源点击
////                StatServiceUtils.statAppBtnClick(this, StatServiceUtils
//// .b_details_click_ch_source);
////                showCoverSourceDialog();
////
////                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE,
//// StartLogClickUtil.SOURCECHANGE);
////                break;
//
//            case R.id.book_cover_bookshelf:
//                if (bookDaoHelper == null || bookCoverUtil == null) {
//                    return;
//                }
//                if (!bookDaoHelper.isBookSubed(requestItem.book_id)) {
//                    Book insertBook = bookCoverUtil.getCoverBook(bookDaoHelper, bookVo);
//                    if (currentSource != null) {
//                        insertBook.last_updatetime_native = currentSource.update_time;
//                    }
//                    if (bookVo.host.equals(Constants.QG_SOURCE)) {
//                        insertBook.last_updatetime_native = bookVo.update_time;
//                    }
//                    insertBook.last_updateSucessTime = System.currentTimeMillis();
//                    boolean succeed = bookDaoHelper.insertBook(insertBook);
//                    if (succeed && book_cover_bookshelf != null) {
//                        //添加书架打点
//                        StatServiceUtils.statAppBtnClick(this,
//                                StatServiceUtils.b_details_click_book_add);
////                        book_cover_bookshelf.setText(R.string.book_cover_remove_bookshelf);
//                        setRemoveBtn();
//                        showToastShort(R.string.succeed_add);
//                        Map<String, String> data1 = new HashMap<>();
//                        data1.put("type", "1");
//                        data1.put("bookid", insertBook.book_id);
//                        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE,
//                                StartLogClickUtil.SHELFEDIT, data1);
//                    }
//
//                } else {
//
//                    if (book_cover_bookshelf != null) {
////                        book_cover_bookshelf.setText(R.string.book_cover_add_bookshelf);
//                        setAddShelfBtn();
//                    }
//                    //移除书架的打点
//                    StatServiceUtils.statAppBtnClick(this,
//                            StatServiceUtils.b_details_click_book_remove);
//                    showToastShort(getString(R.string.succeed_remove));
//                    Map<String, String> data2 = new HashMap<>();
//                    data2.put("type", "2");
//                    data2.put("bookid", requestItem.book_id);
//                    StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE,
//                            StartLogClickUtil.SHELFEDIT, data2);
//
//                    changeDownloadButtonStatus();
//
//                    book_cover_bookshelf.setClickable(false);
//                    final MyDialog cleanDialog = new MyDialog(this, R.layout.dialog_download_clean);
//                    cleanDialog.setCanceledOnTouchOutside(false);
//                    cleanDialog.setCancelable(false);
//                    ((TextView) cleanDialog.findViewById(R.id.dialog_msg)).setText(
//                            R.string.tip_cleaning_cache);
//                    cleanDialog.show();
//
//                    Observable.create(new ObservableOnSubscribe<Boolean>() {
//                        @Override
//                        public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
//                            CacheManager.INSTANCE.remove(requestItem.book_id);
//
//                            if (bookDaoHelper != null) {
//                                bookDaoHelper.deleteBook(requestItem.toBook(), false);
//                            }
//
//                            e.onNext(true);
//                            e.onComplete();
//                        }
//                    }).subscribeOn(Schedulers.io())
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .subscribe(new Consumer<Boolean>() {
//                                @Override
//                                public void accept(Boolean aBoolean) throws Exception {
//                                    cleanDialog.dismiss();
//                                    book_cover_bookshelf.setClickable(true);
//
//                                    changeDownloadButtonStatus();
//                                }
//                            });
//                }
//                break;
//
//            case R.id.book_cover_reading:
//                //转码阅读点击的统计
//                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_trans_read);
//                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE,
//                        StartLogClickUtil.TRANSCODEREAD);
//                if (bookSourceList != null && bookSourceList.size() == 0) {
//                    if (requestItem != null && Constants.QG_SOURCE.equals(requestItem.host)) {
//                        showReadingSourceDialog();
//                        requestItem.fromType =
//                                3;// 打点统计 当前页面来源，所有可能来源的映射唯一字符串。书架(0)/目录页(1)/上一页翻页(2)/书籍封面(3)
//                    } else {
//                        Toast.makeText(getApplicationContext(), "当前书籍不能阅读，先去看看其他书吧",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    showReadingSourceDialog();
//                }
//                break;
//
//            case R.id.book_cover_download:
//
//                if (bookVo == null) {
//                    return;
//                }
//
//                Book book = bookCoverUtil.getCoverBook(bookDaoHelper, bookVo);
//                DownloadState downloadState = CacheManager.INSTANCE.getBookStatus(book);
//                if (downloadState != DownloadState.FINISH && downloadState != DownloadState.WAITTING
//                        && downloadState != DownloadState.DOWNLOADING) {
//                    Toast.makeText(this, "马上开始为你缓存...", Toast.LENGTH_SHORT).show();
//                }
//
//                //全本缓存的点击统计
//                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_all_load);
//                Map<String, String> data3 = new HashMap<>();
//                data3.put("bookId", requestItem.book_id);
//                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE,
//                        StartLogClickUtil.CASHEALL, data3);
//                if (Constants.QG_SOURCE.equals(requestItem.host)) {
//                    if (bookDaoHelper == null) {
//                        bookDaoHelper = BookDaoHelper.getInstance();
//                    }
//                    if (bookDaoHelper != null && bookCoverUtil != null) {
//                        if (!bookDaoHelper.isBookSubed(requestItem.book_id)) {
//                            Book insertBook = bookCoverUtil.getCoverBook(bookDaoHelper, bookVo);
//
//                            insertBook.last_updatetime_native = bookVo.update_time;
//
//                            boolean succeed = bookDaoHelper.insertBook(insertBook);
//                            if (succeed && book_cover_bookshelf != null) {
////                                book_cover_bookshelf.setText(R.string
//// .book_cover_remove_bookshelf);
//                                setRemoveBtn();
//                                showToastShort(getString(R.string.succeed_add));
//                                BaseBookHelper.startDownBookTask(CoverPageActivity.this,
//                                        requestItem.toBook(), 0);
//                            }
//                        } else {
//                            BaseBookHelper.startDownBookTask(CoverPageActivity.this,
//                                    requestItem.toBook(), 0);
//                        }
//                    }
//                } else {
//                    if (bookSourceList != null && bookSourceList.size() == 0) {
//                        Toast.makeText(getApplicationContext(), "当前书籍不能缓存，先去看看其他书吧",
//                                Toast.LENGTH_SHORT).show();
//                    } else {
//                        if (bookDaoHelper == null) {
//                            bookDaoHelper = BookDaoHelper.getInstance();
//                        }
//                        if (bookDaoHelper != null && bookCoverUtil != null) {
//                            if (!bookDaoHelper.isBookSubed(requestItem.book_id)) {
//                                Book insertBook = bookCoverUtil.getCoverBook(bookDaoHelper, bookVo);
//
//                                if (currentSource != null) {
//                                    insertBook.last_updatetime_native = currentSource.update_time;
//                                }
//
//                                boolean succeed = bookDaoHelper.insertBook(insertBook);
//                                if (succeed && book_cover_bookshelf != null) {
////                                    book_cover_bookshelf.setText(R.string
//// .book_cover_remove_bookshelf);
//                                    setRemoveBtn();
//                                    showToastShort(getString(R.string.succeed_add));
//                                    BaseBookHelper.startDownBookTask(CoverPageActivity.this,
//                                            requestItem.toBook(), 0);
//                                }
//                            } else {
//                                BaseBookHelper.startDownBookTask(CoverPageActivity.this,
//                                        requestItem.toBook(), 0);
//                            }
//                        }
//                    }
//                }
//
//                changeDownloadButtonStatus();
//                break;
//            case R.id.book_cover_catalog_view_nobg:
//            case R.id.book_cover_catalog_view:
//                //书籍详情页查看目录点击
//                StatServiceUtils.statAppBtnClick(this,
//                        StatServiceUtils.b_details_click_to_catalogue);
//                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE,
//                        StartLogClickUtil.CATALOG);
//                if (Constants.QG_SOURCE.equals(requestItem.host)) {
//                    goToCataloguesAct(intent, 0, false);
//                } else {
//                    if (bookSourceList != null && bookSourceList.size() == 0) {
//                        Toast.makeText(getApplicationContext(), "当前书籍不能阅读，先去看看其他书吧",
//                                Toast.LENGTH_SHORT).show();
//                    } else {
//                        goToCataloguesAct(intent, 0, false);
//                    }
//                }
//                break;
//
//            case R.id.book_cover_last_chapter:
//                if (bookVo == null) {
//                    return;
//                }
//                if (Constants.QG_SOURCE.equals(requestItem.host)) {
//                    goToCataloguesAct(intent, bookVo.serial_number - 1, true);
//                } else {
//                    if (bookSourceList != null && bookSourceList.size() == 0) {
//                        Toast.makeText(getApplicationContext(), "当前书籍不能阅读，先去看看其他书吧",
//                                Toast.LENGTH_SHORT).show();
//                    } else {
//                        goToCataloguesAct(intent, bookVo.serial_number - 1, true);
//                    }
//                }
//                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE,
//                        StartLogClickUtil.LATESTCHAPTER);
//                break;
//            default:
//                break;
//        }
//    }
//
//    /**
//     * 点击查看目录或者最新章节后的跳转操作
//     *
//     * @param locationSequence 将要定位到的章节序号
//     */
//    private void goToCataloguesAct(Intent intent, int locationSequence, boolean isLastChapter) {
//        if (bookVo != null && bookCoverUtil != null) {
//            Bundle bundle = new Bundle();
//            bundle.putSerializable("cover", bookCoverUtil.getCoverBook(bookDaoHelper,
//                    bookVo));
//            bundle.putInt("sequence", locationSequence);
//            bundle.putBoolean("fromCover", true);
//            bundle.putBoolean("is_last_chapter", isLastChapter);
//            bundle.putSerializable(Constants.REQUEST_ITEM, requestItem);
//            intent.setClass(CoverPageActivity.this, CataloguesActivity.class);
//            intent.putExtras(bundle);
//            startActivity(intent);
//        }
//    }
//
//    private void showCoverSourceDialog() {
//        if (Constants.QG_SOURCE.equals(requestItem.host) || bookSourceList.size() == 1) {//青果
//            Toast.makeText(this, "该小说暂无其他来源！", Toast.LENGTH_LONG).show();
//            return;
//        }
//        if (coverSourceDialog == null) {
//            coverSourceDialog = new MyDialog(CoverPageActivity.this, R.layout
//                    .dialog_read_source, Gravity.CENTER);
//            coverSourceDialog.setCanceledOnTouchOutside(true);
//            ListView sourceView = (ListView) coverSourceDialog.findViewById(R.id
//                    .change_source_list);
//            TextView dialog_top_title = (TextView) coverSourceDialog.findViewById(
//                    R.id.dialog_top_title);
//            dialog_top_title.setText(R.string.change_source);
//            RelativeLayout change_source_statement = (RelativeLayout) coverSourceDialog
//                    .findViewById(R.id.change_source_statement);
//            change_source_statement.setVisibility(View.GONE);
//
//
//            final CoverSourceAdapter bookSourceAdapter = new CoverSourceAdapter(this,
//                    bookSourceList);
//
//            sourceView.setAdapter(bookSourceAdapter);
//
//            if (bookSourceList.size() > 4) {
//                sourceView.getLayoutParams().height = getResources().getDimensionPixelOffset(R
//                        .dimen.dimen_view_height_240);
//            }
//
//
//            sourceView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    CoverPage.SourcesBean source =
//                            (CoverPage.SourcesBean) bookSourceAdapter.getItem(position);
//                    AppLog.e(TAG, "监听开始执行");
//                    if (source != null) {
//                        if (requestItem != null && !TextUtils.isEmpty(source.book_source_id)) {
//                            requestItem.book_id = source.book_id;
//                            requestItem.book_source_id = source.book_source_id;
//                            requestItem.host = source.host;
//                            requestItem.dex = source.dex;
//                        }
//
//                        BookDaoHelper bookDaoHelper = BookDaoHelper.getInstance();
//                        if (bookDaoHelper.isBookSubed(source.book_id)) {
//                            Book iBook = bookDaoHelper.getBook(source.book_id, 0);
//                            iBook.book_source_id = requestItem.book_source_id;
//                            iBook.site = requestItem.host;
//                            iBook.parameter = requestItem.parameter;
//                            iBook.extra_parameter = requestItem.extra_parameter;
//                            iBook.last_updatetime_native = source.update_time;
//                            iBook.dex = source.dex;
//                            bookDaoHelper.updateBook(iBook);
//
//                            BookChapterDao bookChapterDao = new BookChapterDao(
//                                    CoverPageActivity.this, source.book_id);
//
//                            bookChapterDao.deleteBookChapters(0);
//                            CacheManager.INSTANCE.stop(iBook.book_id);
////                            CacheManager.INSTANCE.freshBook(iBook.book_id, true);
//                        }
//
//                        currentSource = source;
//
//                        loadCoverInfo();
//
//                        if (book_cover_source_form != null) {
//                            book_cover_source_form.setText(source.host);
//                        }
//
//                        //                        changeBookInformation(source);
//
//                        coverSourceDialog.dismiss();
//                    }
//                }
//            });
//        }
//
//        TextView change_source_original_web = (TextView) coverSourceDialog.findViewById(R.id
//                .change_source_original_web);
//        change_source_original_web.setText(R.string.cancel);
//        TextView change_source_continue = (TextView) coverSourceDialog.findViewById(R.id
//                .change_source_continue);
//
//        change_source_original_web.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Map<String, String> data = new HashMap<>();
//                data.put("type", "2");
//                StartLogClickUtil.upLoadEventLog(CoverPageActivity.this,
//                        StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.SOURCECHANGEPOPUP,
//                        data);
//                coverSourceDialog.dismiss();
//            }
//        });
//        change_source_continue.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                if (bookSourceList != null && bookSourceList.size() == 0) {
//                    Toast.makeText(getApplicationContext(), "当前书籍不能阅读，先去看看其他书吧",
//                            Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                Intent intent = new Intent();
//                Bundle bundle = new Bundle();
//                if (bookDaoHelper == null || bookCoverUtil == null || bookVo == null) {
//                    return;
//                }
//                Book book = bookCoverUtil.getCoverBook(bookDaoHelper, bookVo);
//
//                if (bookDaoHelper.isBookSubed(requestItem.book_id) && book != null && book
//                        .sequence != -2) {
//                    bundle.putInt("sequence", book.sequence);
//                    bundle.putInt("offset", book.offset);
//                } else {
//                    bundle.putInt("sequence", -1);
//                }
//                if (book != null) {
//                    bundle.putSerializable("book", book);
//                }
//                if (requestItem != null) {
//                    bundle.putSerializable(Constants.REQUEST_ITEM, requestItem);
//                }
//                if (book != null && requestItem != null) {
//                    AppLog.e(TAG, "GotoReading: " + book.site + " : " + requestItem.host);
//                }
//                Map<String, String> data = new HashMap<>();
//                data.put("type", "1");
//                StartLogClickUtil.upLoadEventLog(CoverPageActivity.this,
//                        StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.SOURCECHANGEPOPUP,
//                        data);
//
//                intent.setClass(CoverPageActivity.this, ReadingActivity.class);
//                intent.putExtras(bundle);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                startActivity(intent);
//                coverSourceDialog.dismiss();
//            }
//        });
//
//        if (!coverSourceDialog.isShowing()) {
//            try {
//                coverSourceDialog.show();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private void showReadingSourceDialog() {
//        if (readingSourceDialog == null) {
//            readingSourceDialog = new MyDialog(CoverPageActivity.this, R.layout
//                    .dialog_read_source, Gravity.CENTER);
//            readingSourceDialog.setCanceledOnTouchOutside(true);
//            TextView change_source_head = (TextView) readingSourceDialog.findViewById(
//                    R.id.dialog_top_title);
//            change_source_head.setText("转码");
//            TextView change_source_disclaimer_jump = (TextView) readingSourceDialog.findViewById
//                    (R.id.change_source_disclaimer_jump);
//            TextView change_source_original_web = (TextView) readingSourceDialog.findViewById(R
//                    .id.change_source_original_web);
//            change_source_original_web.setText(R.string.cancel);
//            TextView change_source_continue = (TextView) readingSourceDialog.findViewById(R.id
//                    .change_source_continue);
//
//            if (change_source_disclaimer_jump != null) {
//                change_source_disclaimer_jump.setOnClickListener(new OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Intent intent = new Intent(CoverPageActivity.this, DisclaimerActivity
//                                .class);
//                        try {
//                            intent.putExtra("isFromReadingPage", true);
//                            startActivity(intent);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        readingSourceDialog.dismiss();
//                    }
//                });
//            }
//            change_source_original_web.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Map<String, String> data = new HashMap<>();
//                    data.put("type", "2");
//                    StartLogClickUtil.upLoadEventLog(CoverPageActivity.this,
//                            StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.TRANSCODEPOPUP,
//                            data);
//
//                    readingSourceDialog.dismiss();
//                }
//            });
//            change_source_continue.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Map<String, String> data = new HashMap<>();
//                    data.put("type", "1");
//                    StartLogClickUtil.upLoadEventLog(CoverPageActivity.this,
//                            StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.TRANSCODEPOPUP,
//                            data);
//
//                    requestItem.fromType =
//                            3;// 打点统计 当前页面来源，所有可能来源的映射唯一字符串。书架(0)/目录页(1)/上一页翻页(2)/书籍封面(3)
//                    if (bookDaoHelper.isBookSubed(bookVo.book_id)) {
//                        Book book = bookDaoHelper.getBook(bookVo.book_id, 0);
//                        if (Constants.QG_SOURCE.equals(requestItem.host)) {
//                            readingCustomaryBook(null, false);
//                        } else {
//                            if (currentSource.book_source_id.equals(book.book_source_id)) {
//
//                                //直接进入阅读
//                                readingCustomaryBook(currentSource, true);
//                                readingSourceDialog.dismiss();
//                            } else {
//                                //弹出切源提示
//                                readingSourceDialog.dismiss();
////                                showChangeSourceNoticeDialog(currentSource);
//
//                                intoReadingActivity(currentSource);
//                            }
//                        }
//                    } else {
//                        continueReading();
//                    }
//                    if (readingSourceDialog.isShowing()) {
//                        readingSourceDialog.dismiss();
//                    }
//                }
//            });
//        }
//
//        if (!readingSourceDialog.isShowing()) {
//            try {
//                readingSourceDialog.show();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    //阅读书架上的书籍
//    private void readingCustomaryBook(CoverPage.SourcesBean source, boolean isCurrentSource) {
//        Intent intent = new Intent();
//        Bundle bundle = new Bundle();
//        if (bookDaoHelper == null || bookCoverUtil == null || bookVo == null) {
//            return;
//        }
//        Book book;
//
//        if (source == null) {//说明是青果源的书
//            book = bookDaoHelper.getBook(requestItem.book_id, 0);
//
//        } else {
//            book = bookDaoHelper.getBook(source.book_id, 0);
//        }
//
//        if (book != null && book.sequence != -2) {
//            bundle.putInt("sequence", book.sequence);
//            bundle.putInt("offset", book.offset);
//        } else {
//            bundle.putInt("sequence", -1);
//        }
//        if (book != null) {
//            if (isCurrentSource) {
//                book.last_updatetime_native = source.update_time;
//            }
//            if (Constants.QG_SOURCE.equals(bookVo.host)) {
//                book.last_updatetime_native = bookVo.update_time;
//            }
//            bundle.putSerializable("book", book);
//        }
//        if (requestItem != null) {
//            bundle.putSerializable(Constants.REQUEST_ITEM, requestItem);
//        }
//        intent.setClass(CoverPageActivity.this, ReadingActivity.class);
//        intent.putExtras(bundle);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        startActivity(intent);
//    }
//
//    private void continueReading() {
//        Intent intent = new Intent();
//        Bundle bundle = new Bundle();
//        if (bookDaoHelper == null || bookCoverUtil == null || bookVo == null) {
//            return;
//        }
//        Book book = bookCoverUtil.getCoverBook(bookDaoHelper, bookVo);
//        if (bookDaoHelper.isBookSubed(requestItem.book_id) && book != null && book.sequence != -2) {
//            bundle.putInt("sequence", book.sequence);
//            bundle.putInt("offset", book.offset);
//        } else {
//            bundle.putInt("sequence", -1);
//        }
//        if (book != null) {
//            if (Constants.QG_SOURCE.equals(bookVo.host)) {
//                book.last_updatetime_native = bookVo.update_time;
//            } else {
//                if (currentSource != null) {
//                    book.last_updatetime_native = currentSource.update_time;
//                }
//            }
//            bundle.putSerializable("book", book);
//        }
//        if (requestItem != null) {
//            bundle.putSerializable(Constants.REQUEST_ITEM, requestItem);
//        }
//        AppLog.e(TAG, "GotoReading: " + book.site + " : " + requestItem.host);
//        intent.setClass(CoverPageActivity.this, ReadingActivity.class);
//        intent.putExtras(bundle);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        startActivity(intent);
//    }
//
//    private void intoReadingActivity(CoverPage.SourcesBean source) {
//        //进入阅读页逻辑
//        Intent intent = new Intent();
//        Bundle bundle = new Bundle();
//        if (bookDaoHelper == null || bookCoverUtil == null || bookVo == null) {
//            return;
//        }
//        Book book = bookCoverUtil.getCoverBook(bookDaoHelper, bookVo);
//        if (bookDaoHelper.isBookSubed(requestItem.book_id) && book != null && book.sequence != -2) {
//            bundle.putInt("sequence", book.sequence);
//            AppLog.e(TAG, "offset : " + book.offset);
//        } else {
//            bundle.putInt("sequence", -1);
//        }
//
//
//        if (requestItem != null) {
//            requestItem.book_id = source.book_id;
//            requestItem.book_source_id = source.book_source_id;
//            requestItem.host = source.host;
//            requestItem.dex = source.dex;
//
//            bundle.putSerializable(Constants.REQUEST_ITEM, requestItem);
//        }
//
//        if (book != null) {
//            book = changeBookInformation(source, book);
//            bundle.putSerializable("book", book);
//        }
//        AppLog.e(TAG, "GotoReading: " + book.site + " : " + requestItem.host);
//        intent.setClass(CoverPageActivity.this, ReadingActivity.class);
//        intent.putExtras(bundle);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        startActivity(intent);
//    }
//
//    private void showChangeSourceNoticeDialog(final CoverPage.SourcesBean source) {
//        if (!isFinishing()) {
//            confirm_change_source_dialog = new MyDialog(this, R.layout.publish_hint_dialog);
//            confirm_change_source_dialog.setCanceledOnTouchOutside(true);
//            Button dialog_cancel = (Button) confirm_change_source_dialog.findViewById(R.id
//                    .publish_stay);
//            dialog_cancel.setText(R.string.book_cover_continue_read_cache);
//            Button dialog_confirm = (Button) confirm_change_source_dialog.findViewById(R.id
//                    .publish_leave);
//            dialog_confirm.setText(R.string.book_cover_confirm_change_source);
//            TextView dialog_information = (TextView) confirm_change_source_dialog.findViewById(R
//                    .id.publish_content);
//            dialog_information.setText(R.string.book_cover_change_source_prompt);
//            dialog_cancel.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    dismissDialog();
//                    readingCustomaryBook(source, false);
//                }
//            });
//            dialog_confirm.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    dismissDialog();
//                    intoReadingActivity(source);
//                }
//            });
//
//            confirm_change_source_dialog.setOnCancelListener(new DialogInterface.OnCancelListener
//                    () {
//                @Override
//                public void onCancel(DialogInterface dialog) {
//                    confirm_change_source_dialog.dismiss();
//                }
//            });
//            if (!confirm_change_source_dialog.isShowing()) {
//                try {
//                    confirm_change_source_dialog.show();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//    private void dismissDialog() {
//        if (confirm_change_source_dialog != null && confirm_change_source_dialog.isShowing()) {
//            confirm_change_source_dialog.dismiss();
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//
//        try {
//            setContentView(R.layout.empty);
//        } catch (Resources.NotFoundException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            if (requestItem != null && !bookDaoHelper.isBookSubed(requestItem.book_id)) {
//                deleteDatabase("book_chapter_" + requestItem.book_id);
//            }
//        } catch (IndexOutOfBoundsException e) {
//            e.printStackTrace();
//        }
//
//        if (bookCoverUtil != null) {
//            bookCoverUtil.unRegistReceiver();
//            bookCoverUtil = null;
//        }
//
//        if (readingSourceDialog != null) {
//            readingSourceDialog.dismiss();
//        }
//
//        if (loadingPage != null) {
//            loadingPage = null;
//        }
//
//        if (coverRecommendAdapter != null) {
//            coverRecommendAdapter = null;
//        }
//
//        if (uiHandler != null) {
//            uiHandler.removeCallbacksAndMessages(null);
//            uiHandler = null;
//        }
//        super.onDestroy();
//    }
//
//    @Override
//    public void onBackPressed() {
//        Map<String, String> data = new HashMap<>();
//        data.put("type", "2");
//        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SYSTEM_PAGE,
//                StartLogClickUtil.BACK, data);
//        finish();
//        SearchBookActivity.isSatyHistory = true;
//    }
//
//    @Override
//    public void changeState() {
//        changeDownloadButtonStatus();
//    }
//
//    @Override
//    public void downLoadService() {
//        changeDownloadButtonStatus();
//    }
//
//    private Book changeBookInformation(CoverPage.SourcesBean source, Book book) {
//        BookDaoHelper bookDaoHelper = BookDaoHelper.getInstance();
//        book.book_source_id = source.book_source_id;
//        book.site = source.host;
//        book.last_updatetime_native = source.update_time;
//        book.dex = source.dex;
//
//        if (bookDaoHelper.isBookSubed(source.book_id)) {
//            bookDaoHelper.updateBook(book);
//        }
//
//        BookChapterDao bookChapterDao = new BookChapterDao(CoverPageActivity.this, source.book_id);
//
//        bookChapterDao.deleteBookChapters(0);
//
//        return book;
//    }
//
//    @Override
//    public void onScrollChanged(int top, int oldTop) {
//        if (AppUtils.px2dip(this, top) > 32 && bookVo != null) {
//            if (tv_title != null && !TextUtils.isEmpty(bookVo.name)) {
//                tv_title.setText(bookVo.name);
//            }
//        } else {
//            tv_title.setText("书籍详情");
//        }
//
//    }
//
//    @Override
//    public void onItemClick(View view, int position) {
//        if (shake.check()) {
//            return;
//        }
//        if (view == null || position < 0 || position > mRecommendBooks.size()) {
//            return;
//        }
//        Book book = mRecommendBooks.get(position);
//        if (book == null) {
//            return;
//        }
//        Map<String, String> data = new HashMap<>();
//        if (requestItem != null && requestItem.book_id != null) {
//            data.put("bookid", requestItem.book_id);
//            data.put("TbookID", book.book_id);
//        }
//        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE,
//                StartLogClickUtil.RECOMMENDEDBOOK, data);
//        BookHelper.goToCoverOrRead(this, this, book, 2);
//    }
//
//    private static class UIHandler extends Handler {
//
//        private WeakReference<CoverPageActivity> weakReference;
//
//        UIHandler(CoverPageActivity coverPageActivity) {
//            weakReference = new WeakReference<>(coverPageActivity);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            CoverPageActivity coverPageActivity = weakReference.get();
//            if (coverPageActivity == null) {
//                return;
//            }
//            switch (msg.what) {
//                case REQUEST_COVER_SUCCESS:
//                    coverPageActivity.handleOK(msg.obj, false);
//                    break;
//                case REQUEST_COVER_ERROR:
//                    coverPageActivity.handleError();
//                    break;
//                case RequestExecutor.REQUEST_COVER_QG_SUCCESS:
//                    coverPageActivity.handleOK(msg.obj, true);
//                    break;
//                case RequestExecutor.REQUEST_COVER_QG_ERROR:
//                    coverPageActivity.handleError();
//                    break;
//                case GET_CATEGORY_OK:
//                    coverPageActivity.handCategoryOk();
//                    break;
//                case GET_CATEGORY_ERROR:
//                    coverPageActivity.handCategoryError();
//                    break;
//                default:
//                    break;
//            }
//        }
//    }
//}
