/**
 * @Title: CataloguesActivity.java
 * @Description: 小说目录页
 */
package com.intelligent.reader.activity;

import com.baidu.mobstat.StatService;
import com.intelligent.reader.R;
import com.intelligent.reader.adapter.CatalogAdapter;
import com.intelligent.reader.read.help.BookHelper;
import com.intelligent.reader.receiver.DownBookClickReceiver;
import com.intelligent.reader.receiver.OffLineDownLoadReceiver;
import com.quduquxie.network.DataCache;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.component.service.DownloadService;
import net.lzbook.kit.book.download.DownloadState;
import net.lzbook.kit.book.view.LoadingPage;
import net.lzbook.kit.book.view.MyDialog;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.Bookmark;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.EventBookmark;
import net.lzbook.kit.data.bean.RequestItem;
import net.lzbook.kit.data.db.BookChapterDao;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.repair_books.RepairHelp;
import net.lzbook.kit.request.RequestExecutor;
import net.lzbook.kit.request.RequestFactory;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.BookCoverUtil;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.StatServiceUtils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import de.greenrobot.event.EventBus;

/**
 * CataloguesActivity
 * 小说目录
 */
public class CataloguesActivity extends BaseCacheableActivity implements OnClickListener, OnScrollListener, OnItemClickListener, BookCoverUtil
        .OnDownloadState, BookCoverUtil.OnDownLoadService, DownloadService.OnDownloadListener {

    protected static final int MESSAGE_FETCH_CATALOG = 0;
    protected static final int MESSAGE_FETCH_BOOKMARK = MESSAGE_FETCH_CATALOG + 1;
    protected static final int MESSAGE_FETCH_ERROR = MESSAGE_FETCH_BOOKMARK + 1;
    private static final int DELAY_OVERLAY = MESSAGE_FETCH_ERROR + 1;
    private final MyHandler myHandler = new MyHandler(this);
    public int type = 2;
    int colorSelected;
    int colorNormal;
    int sortIcon = 0;//背景色
    private FrameLayout catalog_root;
    private RelativeLayout rl_catalog_novel;
    private TextView catalog_novel_name;
    private ImageView catalog_novel_close;
    private ListView catalog_main;
    private TextView catalog_empty_refresh;
    private TextView catalog_chapter_hint;
    private TextView catalog_chapter_count;
    private ImageView iv_catalog_novel_sort;
    private ImageView iv_back_reading;
    //是否是最后一页
    private boolean is_last_chapter;
    //是否来源于封面页
    private boolean fromCover;
    //是否来源于完结页
    private boolean fromEnd;
    //加载页
    private LoadingPage loadingPage;
    private int sequence;
    //小说ID
    private int nid;
    //小说
    private Book book;
    //小说帮助类
    private BookDaoHelper mBookDaoHelper;
    private CatalogAdapter mCatalogAdapter;
    private ArrayList<Chapter> chapterList = new ArrayList<>();
    private boolean isPositive = true;
    /**
     * 标识List的滚动状态。
     */
    private int scrollState;
    private OffLineDownLoadReceiver downLoadReceiver;
    private RequestFactory requestFactory;
    private RequestItem requestItem;
    private TextView book_catalog_download, book_catalog_reading, book_catalog_bookshelf;
    private MyDialog readingSourceDialog;
    private int mTextColor = 0;
    private BookCoverUtil bookCoverUtil;
    private DownloadService downloadService;
    private ImageView iv_fixbook;
    private ServiceConnection sc = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadService = ((DownloadService.MyBinder) service).getService();
            BaseBookApplication.setDownloadService(downloadService);
            downloadService.setUiContext(getApplicationContext());
            downloadService.setOnDownloadListener(CataloguesActivity.this);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.act_catalog);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        requestFactory = new RequestFactory();
        colorSelected = getResources().getColor(R.color.theme_primary_ffffff);
        colorNormal = getResources().getColor(R.color.theme_primary);
        initUI();
        initListener();

        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            return;
        }

        initData(bundle);
        initCatalogAndBookmark();
        if (fromEnd) {
            isPositive = false;
            changeSortState(isPositive);
        }
        EventBus.getDefault().register(this);

    }

    private void initUI() {

        catalog_root = (FrameLayout) findViewById(R.id.catalog_layout);

        rl_catalog_novel = (RelativeLayout) findViewById(R.id.rl_catalog_novel);

        catalog_novel_name = (TextView) findViewById(R.id.catalog_novel_name);

        catalog_novel_close = (ImageView) findViewById(R.id.catalog_novel_close);
        catalog_novel_close.setOnClickListener(this);

        book_catalog_download = (TextView) findViewById(R.id.book_catalog_download);
        book_catalog_reading = (TextView) findViewById(R.id.book_catalog_reading);
        book_catalog_bookshelf = (TextView) findViewById(R.id.book_catalog_bookshelf);
        book_catalog_download.setOnClickListener(this);
        book_catalog_reading.setOnClickListener(this);
        book_catalog_bookshelf.setOnClickListener(this);


        iv_catalog_novel_sort = (ImageView) findViewById(R.id.iv_catalog_novel_sort);
        iv_catalog_novel_sort.setOnClickListener(this);

        catalog_chapter_count = (TextView) findViewById(R.id.catalog_chapter_count);

        catalog_main = (ListView) findViewById(R.id.catalog_main);


        catalog_empty_refresh = (TextView) findViewById(R.id.catalog_empty_refresh);

        catalog_chapter_hint = (TextView) findViewById(R.id.char_hint);
        catalog_chapter_hint.setVisibility(View.INVISIBLE);

        iv_fixbook = (ImageView) findViewById(R.id.iv_fixbook);

        iv_back_reading = (ImageView) findViewById(R.id.iv_back_reading);
        iv_back_reading.setOnClickListener(this);
        downloadService = BaseBookApplication.getDownloadService();
        changeSortState(isPositive);
    }

    private void initListener() {
        if (catalog_main != null) {
            catalog_main.setOnItemClickListener(this);
            catalog_main.setOnScrollListener(this);
        }


        if (catalog_empty_refresh != null) {
            catalog_empty_refresh.setOnClickListener(this);
        }
        if (downloadService == null) {
            reStartDownloadService(BaseBookApplication.getGlobalContext().getApplicationContext());
            downloadService = BaseBookApplication.getDownloadService();
        } else {
            downloadService.setUiContext(getApplicationContext());
            downloadService.setOnDownloadListener(this);
        }

        if (iv_fixbook != null) {
            iv_fixbook.setOnClickListener(this);
        }
    }

    private void reStartDownloadService(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, DownloadService.class);
        context.startService(intent);
        context.bindService(intent, sc, BIND_AUTO_CREATE);
    }

    private void initData(Bundle bundle) {
        if (BaseBookApplication.getDownloadService() == null) {
            BookHelper.reStartDownloadService();
        }
        if (bookCoverUtil == null) {
            bookCoverUtil = new BookCoverUtil(CataloguesActivity.this, this);
        }
        bookCoverUtil.registReceiver();
        bookCoverUtil.setOnDownloadState(this);
        bookCoverUtil.setOnDownLoadService(this);

        requestItem = (RequestItem) bundle.getSerializable(Constants.REQUEST_ITEM);

        if (requestItem == null || requestItem.book_id == null || requestItem.host == null) {
            exitAndUpdate();
            return;
        }

        sequence = Math.max(bundle.getInt("sequence"), 0);
        AppLog.e(TAG, "CataloguesActivity: " + sequence);
        is_last_chapter = bundle.getBoolean("is_last_chapter", false);
        fromCover = bundle.getBoolean("fromCover", true);
        fromEnd = bundle.getBoolean("fromEnd", false);
        book = (Book) bundle.getSerializable("cover");
        if (book != null) {
            catalog_novel_name.setText(book.name);
            if (RepairHelp.isShowFixBtn(this, book.book_id)) {
                iv_fixbook.setVisibility(View.VISIBLE);
            } else {
                iv_fixbook.setVisibility(View.GONE);
            }

        }

        if (mBookDaoHelper == null)
            mBookDaoHelper = BookDaoHelper.getInstance();

        getChapterData();

    }

    public void onEvent(EventBookmark eventBookmark) {
        if (eventBookmark.getType() == EventBookmark.type_delete) {
            AppLog.e(TAG, "eventBookmark:" + eventBookmark.getBookmark().id + " name:" + eventBookmark.getBookmark().chapter_name);
            Bookmark bookmark = eventBookmark.getBookmark();
            if (bookmark != null) {
                ArrayList<Integer> deleteList = new ArrayList<>();
                deleteList.add(bookmark.id);
            }
        }
    }

    private void getChapterData() {
        if (book != null) {
            if (loadingPage != null) {
                loadingPage.onSuccess();
            }
            loadingPage = new LoadingPage(this, LoadingPage.setting_result);
            loadingPage.setCustomBackgroud();

            final BookChapterDao chapterDao = new BookChapterDao(this, book.book_id);
            chapterList = chapterDao.queryBookChapter();
            if (chapterList != null) {
                AppLog.e(TAG, "ChapterList: " + chapterList.size());
            }
            if (chapterList != null && chapterList.size() != 0) {
                if (myHandler != null)
                    myHandler.obtainMessage(RequestExecutor.REQUEST_CATALOG_SUCCESS).sendToTarget();
            } else {
                if (requestItem != null) {
                    if (Constants.SG_SOURCE.equals(requestItem.host)) {
                        myHandler.sendEmptyMessage(RequestExecutor.REQUEST_CATALOG_ERROR);
                    } else {
                        requestFactory.requestExecutor(requestItem).requestCatalogList(getApplicationContext(), myHandler, requestItem);
                    }
                }
            }

            if (loadingPage != null) {
                loadingPage.isCategory = true;
                loadingPage.setReloadAction(new Callable<Void>() {

                    @Override
                    public Void call() throws Exception {
                        if (requestItem != null) {
                            if (Constants.SG_SOURCE.equals(requestItem.host)) {
                                myHandler.sendEmptyMessage(RequestExecutor.REQUEST_CATALOG_ERROR);
                            } else {
                                requestFactory.requestExecutor(requestItem).requestCatalogList(getApplicationContext(), myHandler, requestItem);
                            }
                        }
                        return null;
                    }
                });
            }
        }
    }


    private void delayOverLay() {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
                || scrollState == OnScrollListener.SCROLL_STATE_FLING &&
                catalog_chapter_hint != null) {
            catalog_chapter_hint.setVisibility(View.INVISIBLE);
        }
    }

    private void dataError() {
        if (loadingPage != null) {
            loadingPage.onError();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isModeChange()) {
            setMode();
        }
        if (mBookDaoHelper == null || requestItem == null)
            return;

        if (mBookDaoHelper.isBookSubed(requestItem.book_id)) {
            if (book_catalog_bookshelf != null) {
                book_catalog_bookshelf.setText(R.string.book_cover_havein_bookshelf);
                setRemoveBtn();
            }
        }

        StatService.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        StatService.onPause(this);
    }

    private void initCatalogAndBookmark() {
        mCatalogAdapter = new CatalogAdapter(this, chapterList, requestItem.host);
        catalog_main.setAdapter(mCatalogAdapter);
        if (is_last_chapter) {
            mCatalogAdapter.setSelectedItem(chapterList.size());
            catalog_main.setSelection(chapterList.size());
        } else {
            mCatalogAdapter.setSelectedItem(sequence + 1);
            catalog_main.setSelection(sequence + 1);
        }


    }

    private void setCatalogData(Message msg) {
        if (loadingPage != null) {
            loadingPage.onSuccess();
        }
        if (msg != null && msg.obj != null) {
            chapterList = (ArrayList<Chapter>) msg.obj;
        }

        if (chapterList == null) {
            showToastShort(getString(R.string.failed_get_data));
        } else {
            catalog_chapter_count.setText("共" + chapterList.size() + "章");
            if (mCatalogAdapter != null) {
                if (fromEnd) {
                    isPositive = false;
                    Collections.reverse(chapterList);
                }
                mCatalogAdapter.setList(chapterList);
                mCatalogAdapter.notifyDataSetChanged();
            }

            //设置选中的条目
            int position = 0;
            if (is_last_chapter) {
                position = chapterList.size();
            } else {
                position = sequence;
            }

            if (catalog_main != null) {
                catalog_main.setSelection(position);
            }

            if (mCatalogAdapter != null)
                mCatalogAdapter.setSelectedItem(position);
        }
        changeDownloadButtonStatus();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (downLoadReceiver == null) {
            downLoadReceiver = new OffLineDownLoadReceiver(this);
        }
        downLoadReceiver.registerAction();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (downLoadReceiver != null) {
            try {
                unregisterReceiver(downLoadReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        try {
            EventBus.getDefault().unregister(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (chapterList != null) {
            chapterList.clear();
        }
        if (myHandler != null)
            myHandler.removeCallbacksAndMessages(null);

        if (bookCoverUtil != null) {
            bookCoverUtil.unRegistReceiver();
            bookCoverUtil = null;
        }
        super.onDestroy();
    }

    public void notifyChangeDownLoad() {
        if (mCatalogAdapter != null) {
            mCatalogAdapter.notifyDataSetChanged();
        }
    }

    // 目录
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (chapterList != null && !chapterList.isEmpty()) {
            catalog_chapter_hint.setText(String.format(getString(R.string.chapter_sort), chapterList.get(firstVisibleItem).sequence + 1));
        }
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        this.scrollState = scrollState;
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE || scrollState == OnScrollListener.SCROLL_STATE_FLING) {
            if (myHandler != null) {
                myHandler.removeMessages(DELAY_OVERLAY);
                myHandler.sendEmptyMessageDelayed(DELAY_OVERLAY, 1500);
            }
        } else {
            if (catalog_chapter_hint != null) {
                catalog_chapter_hint.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.catalog_novel_close:
                Map<String, String> data = new HashMap<>();
                data.put("type", "1");
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.BACK, data);
                if (!fromCover) {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constants.REQUEST_ITEM, requestItem);
                    bundle.putInt("sequence", sequence);
                    bundle.putSerializable("book", book);
                    intent.putExtras(bundle);
                    setResult(RESULT_OK, intent);
                }
                finish();
                break;
            case R.id.iv_back_reading:
                finish();
                break;
            case R.id.catalog_empty_refresh:
                getChapterData();
                break;
            case R.id.book_catalog_download:
                Map<String, String> data1 = new HashMap<>();
                if (requestItem != null && requestItem.book_id != null) {
                    data1.put("bookid", requestItem.book_id);
                }
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOKCATALOG, StartLogClickUtil.CATALOG_CASHEALL, data1);
                startDownLoader();
                break;
            case R.id.book_catalog_reading:

                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOKCATALOG, StartLogClickUtil.TRANSCODEREAD);
                showReadingSourceDialog();
                break;
            case R.id.book_catalog_bookshelf:
                if (mBookDaoHelper == null) {
                    return;
                }
                Map<String, String> data2 = new HashMap<>();

                if (requestItem != null && !mBookDaoHelper.isBookSubed(requestItem.book_id)) {


                    if (book != null) {
                        boolean succeed = mBookDaoHelper.insertBook(book);
                        if (succeed && book_catalog_bookshelf != null) {
                            data2.put("type", "1");
                            //添加书架打点
                            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_book_add);
                            book_catalog_bookshelf.setText(R.string.book_cover_havein_bookshelf);
                            setRemoveBtn();
                            showToastShort(R.string.succeed_add);
                        }
                    }
                } else {

                    showToastShort(R.string.have_add);
                }

                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOKCATALOG, StartLogClickUtil.CATALOG_CASHEALL, data2);
                break;

            case R.id.iv_catalog_novel_sort://正序、逆序
                if (chapterList != null && !chapterList.isEmpty()) {
                    //书签点击的统计
                    StatServiceUtils.statAppBtnClick(this, StatServiceUtils.rb_catalog_click_book_mark);
                    isPositive = !isPositive;
                    Collections.reverse(chapterList);
                    mCatalogAdapter.setList(chapterList);
                    mCatalogAdapter.notifyDataSetChanged();
                    changeSortState(isPositive);
                }
                break;
            case R.id.iv_fixbook:
                RepairHelp.fixBook(this, book, new RepairHelp.FixCallBack() {
                    @Override
                    public void toDownLoadActivity() {
                        Intent intent_download = new Intent(CataloguesActivity.this, DownloadManagerActivity.class);
                        try {
                            CataloguesActivity.this.startActivity(intent_download);
                            CataloguesActivity.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
            default:
                break;
        }
    }

    //缓存
    public void startDownLoader() {

        if (book == null)
            return;

        DownloadState downloadState = BookHelper.getDownloadState(CataloguesActivity.this, book);
        if (downloadState != DownloadState.FINISH && downloadState != DownloadState.WAITTING && downloadState != DownloadState.DOWNLOADING) {
            Toast.makeText(this, "马上开始为你缓存。。。", Toast.LENGTH_SHORT).show();
        }

        //全本缓存的点击统计
        StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_all_load);

//        if (Constants.QG_SOURCE.equals(requestItem.host)) {
        if (mBookDaoHelper == null) {
            mBookDaoHelper = BookDaoHelper.getInstance();
        }
        if (mBookDaoHelper != null && bookCoverUtil != null) {
            if (!mBookDaoHelper.isBookSubed(requestItem.book_id)) {

                boolean succeed = mBookDaoHelper.insertBook(book);
                if (succeed && book_catalog_bookshelf != null) {
                    book_catalog_bookshelf.setText(R.string.book_cover_havein_bookshelf);
                    setRemoveBtn();
                    showToastShort(getString(R.string.succeed_add));


                    bookCoverUtil.catalogStartDownLoad(book);
                }
            } else {
                bookCoverUtil.catalogStartDownLoad(book);
            }
        }
//        }
//        else {
//
//            if (mBookDaoHelper == null) {
//                mBookDaoHelper = BookDaoHelper.getInstance();
//            }
//            if (mBookDaoHelper != null && bookCoverUtil != null) {
//                if (!mBookDaoHelper.isBookSubed(requestItem.book_id)) {
//
//                    boolean succeed = mBookDaoHelper.insertBook(book);
//                    if (succeed && book_catalog_bookshelf != null) {
//                        book_catalog_bookshelf.setText(R.string.book_cover_havein_bookshelf);
//                        setRemoveBtn();
//                        showToastShort(getString(R.string.succeed_add));
//                        bookCoverUtil.catalogStartDownLoad(book);
//                    }
//                } else {
//                    bookCoverUtil.catalogStartDownLoad(book);
//                }
//            }
//        }
    }

    private void setRemoveBtn() {
        mTextColor = R.color.home_title_search_text;
        book_catalog_bookshelf.setTextColor(getResources().getColor(mTextColor));
    }

    private void showReadingSourceDialog() {
        if (readingSourceDialog == null) {
            readingSourceDialog = new MyDialog(CataloguesActivity.this, R.layout
                    .dialog_read_source, Gravity.CENTER);
            readingSourceDialog.setCanceledOnTouchOutside(true);
            TextView title = (TextView) readingSourceDialog.findViewById(R.id.dialog_top_title);
            title.setText("转码");

            TextView cancle = (TextView) readingSourceDialog.findViewById(R.id.change_source_original_web);
            cancle.setText(R.string.cancel);
            TextView continueRead = (TextView) readingSourceDialog.findViewById(R.id.change_source_continue);

            cancle.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map<String, String> data1 = new HashMap<>();
                    data1.put("type", "2");
                    StartLogClickUtil.upLoadEventLog(CataloguesActivity.this, StartLogClickUtil.BOOKCATALOG, StartLogClickUtil.CATALOG_TRANSCODEPOPUP, data1);
                    readingSourceDialog.dismiss();
                }
            });
            continueRead.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Map<String, String> data1 = new HashMap<>();
                    data1.put("type", "1");
                    StartLogClickUtil.upLoadEventLog(CataloguesActivity.this, StartLogClickUtil.BOOKCATALOG, StartLogClickUtil.CATALOG_TRANSCODEPOPUP, data1);
                    readingBook();
                    if (readingSourceDialog.isShowing()) {
                        readingSourceDialog.dismiss();
                    }
                }
            });
        }
        if (!readingSourceDialog.isShowing()) {
            try {
                readingSourceDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //进入阅读页
    private void readingBook() {
        if (requestItem == null || book == null) {
            return;
        }
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        if (mBookDaoHelper != null && mBookDaoHelper.isBookSubed(requestItem.book_id) && book != null && book.sequence != -2) {
            bundle.putInt("sequence", book.sequence);
            bundle.putInt("offset", book.offset);
        } else {
            bundle.putInt("sequence", -1);
        }

        if (book != null) {
            bundle.putSerializable("book", book);
        }
        if (requestItem != null) {
            requestItem.fromType = 1;// 打点统计 当前页面来源，所有可能来源的映射唯一字符串。书架(0)/目录页(1)/上一页翻页(2)/书籍封面(3)
            bundle.putSerializable(Constants.REQUEST_ITEM, requestItem);
        }

        if (book != null && requestItem != null && Constants.QG_SOURCE.equals(book.site)) {
            requestItem.channel_code = 1;
        } else {
            requestItem.channel_code = 2;
        }

        intent.setClass(CataloguesActivity.this, ReadingActivity.class);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        exit();
    }

    //排序
    private void changeSortState(boolean b) {
        if (iv_catalog_novel_sort != null) {
            if (b) {
                sortIcon = R.drawable.icon_catalog_daoxu;
                //正序的统计
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.rb_catalog_click_zx_btn);

                iv_catalog_novel_sort.setImageResource(sortIcon);
            } else {
                sortIcon = R.drawable.icon_catalog_zhengxu;
                iv_catalog_novel_sort.setImageResource(sortIcon);
                //倒序的统计
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.rb_catalog_click_dx_btn);
            }
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitAndUpdate();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exitAndUpdate() {
        //如果是从通知栏过来, 且已经退出到home了, 要回到应用中
        if (isTaskRoot()) {
            Intent intent = new Intent(this, SplashActivity.class);
            startActivity(intent);
        }
        exit();
    }

    private void exit() {
        finish();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        requestItem.fromType = 1; // 打点统计 当前页面来源，所有可能来源的映射唯一字符串。书架(0)/目录页(1)/上一页翻页(2)/书籍封面(3)
        if (parent == catalog_main) {
            if (chapterList != null && !chapterList.isEmpty()) {
                boolean isChapterExist;
                Chapter tempChapter = chapterList.get(position);
                if (requestItem.host.equals(Constants.QG_SOURCE)) {
                    requestItem.channel_code = 1;
                    isChapterExist = DataCache.isChapterExists(tempChapter.chapter_id, tempChapter.book_id);
                } else {
                    requestItem.channel_code = 2;
                    isChapterExist = BookHelper.isChapterExist(tempChapter.sequence, book.book_id);
                }
                if (!isChapterExist && NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
                    showToastShort(getString(R.string.no_net));
                    return;
                }

                bundle.putSerializable(Constants.REQUEST_ITEM, requestItem);
                AppLog.e(TAG, "CataloguesActivity: " + requestItem.toString());
                AppLog.e(TAG, "CataloguesActivity: " + chapterList.get(position).toString());
                bundle.putInt("sequence", chapterList.get(position).sequence);

                Map<String, String> data1 = new HashMap<>();
                data1.put("bookid", requestItem.book_id);
                data1.put("chapterid", tempChapter.chapter_id);
                StartLogClickUtil.upLoadEventLog(CataloguesActivity.this, StartLogClickUtil.BOOKCATALOG, StartLogClickUtil.CATALOG_CATALOGCHAPTER, data1);
            }
        }

        bundle.putSerializable("book", book);
        bundle.putString("thememode", mThemeHelper.getMode());
        intent.putExtras(bundle);


        if (fromCover) {
            intent.setClass(CataloguesActivity.this, ReadingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            setResult(RESULT_OK, intent);
        }
        exit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("menu");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void changeState() {
        changeDownloadButtonStatus();
    }

    @Override
    public void downLoadService() {
        changeDownloadButtonStatus();
    }

    private void changeDownloadButtonStatus() {
        if (book_catalog_download == null) {
            return;
        }
        if (book != null && book_catalog_download != null) {
            if (BookHelper.getDownloadState(CataloguesActivity.this, book) == DownloadState.FINISH) {
                book_catalog_download.setText(R.string.download_status_complete);
                book_catalog_download.setTextColor(getResources().getColor(R.color.home_title_search_text));
            } else if (BookHelper.getDownloadState(CataloguesActivity.this, book) == DownloadState
                    .LOCKED) {
                book_catalog_download.setText(R.string.download_status_complete);
                book_catalog_download.setTextColor(getResources().getColor(R.color.home_title_search_text));
            } else if (BookHelper.getDownloadState(CataloguesActivity.this, book) == DownloadState
                    .NOSTART) {
                book_catalog_download.setText(R.string.download_status_total);
            } else {
                book_catalog_download.setText(R.string.download_status_underway);
            }
        }
    }


    @Override
    public void notificationCallBack(Notification preNTF, String book_id) {
        PendingIntent pending = null;
        Intent intent = null;
        if (!book_id.equals(-1 + "")) {
            intent = new Intent(this, DownBookClickReceiver.class);
            intent.setAction(DownBookClickReceiver.action);
            intent.putExtra("book_id", book_id);
            pending = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            intent = new Intent(this, DownloadManagerActivity.class);
            pending = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        preNTF.contentIntent = pending;
    }

    private static class MyHandler extends Handler {
        private WeakReference<CataloguesActivity> activityWeakReference;

        MyHandler(CataloguesActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            CataloguesActivity cataloguesActivity = activityWeakReference.get();
            if (cataloguesActivity == null) {
                return;
            }
            switch (msg.what) {

                case RequestExecutor.REQUEST_CATALOG_ERROR:
                    cataloguesActivity.dataError();
                    break;
                case RequestExecutor.REQUEST_CATALOG_SUCCESS:
                    cataloguesActivity.setCatalogData(msg);
                    break;
                case DELAY_OVERLAY:
                    cataloguesActivity.delayOverLay();
                    break;
                case RequestExecutor.REQUEST_QG_CATALOG_SUCCESS:
                    cataloguesActivity.setCatalogData(msg);
                    break;
                case RequestExecutor.REQUEST_QG_CATALOG_ERROR:
                    cataloguesActivity.dataError();
                    break;
            }
        }
    }

}