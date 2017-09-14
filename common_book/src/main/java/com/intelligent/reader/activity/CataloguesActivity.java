/**
 * @Title: CataloguesActivity.java
 * @Description: 小说目录页
 */
package com.intelligent.reader.activity;

import com.baidu.mobstat.StatService;
import com.intelligent.reader.R;
import com.intelligent.reader.adapter.BookmarkAdapter;
import com.intelligent.reader.adapter.CatalogAdapter;
import com.intelligent.reader.read.help.BookHelper;
import com.intelligent.reader.receiver.OffLineDownLoadReceiver;
import com.quduquxie.network.DataCache;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
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
import net.lzbook.kit.request.RequestExecutor;
import net.lzbook.kit.request.RequestFactory;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.StatServiceUtils;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
public class CataloguesActivity extends BaseCacheableActivity implements OnClickListener, OnScrollListener, OnItemClickListener {

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
    private RadioButton tab_catalog;
    private RadioButton tab_bookmark;
    private ListView catalog_main;
    private ListView bookmark_main;
    private LinearLayout bookmark_empty;
    private TextView bookmark_empty_message;
    private TextView catalog_empty_refresh;
    private TextView catalog_chapter_hint;
    private TextView catalog_chapter_count;
    private TextView tv_catalog_novel_sort;
    private ImageView iv_catalog_novel_sort;
    private ImageView iv_back_reading;
    //当前页标识
    private View currentView;
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
    private BookmarkAdapter mBookmarkAdapter;
    private ArrayList<Chapter> chapterList = new ArrayList<>();
    private ArrayList<Bookmark> bookmarkList = new ArrayList<>();
    private boolean isPositive = true;
    /**
     * 标识List的滚动状态。
     */
    private int scrollState;
    private OffLineDownLoadReceiver downLoadReceiver;
    private RequestFactory requestFactory;
    private RequestItem requestItem;

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

        tv_catalog_novel_sort = (TextView) findViewById(R.id.tv_catalog_novel_sort);
        tv_catalog_novel_sort.setOnClickListener(this);

        iv_catalog_novel_sort = (ImageView) findViewById(R.id.iv_catalog_novel_sort);
        iv_catalog_novel_sort.setOnClickListener(this);

        catalog_chapter_count = (TextView) findViewById(R.id.catalog_chapter_count);

        tab_bookmark = (RadioButton) findViewById(R.id.tab_bookmark);
        tab_bookmark.setOnClickListener(this);
        tab_catalog = (RadioButton) findViewById(R.id.tab_catalog);
        tab_catalog.setOnClickListener(this);

        catalog_main = (ListView) findViewById(R.id.catalog_main);
        bookmark_main = (ListView) findViewById(R.id.bookmark_main);

        bookmark_empty = (LinearLayout) findViewById(R.id.rl_layout_empty_online);
        bookmark_empty.setVisibility(View.GONE);

//        bookmark_empty_image = (ImageView) findViewById(R.id.bookmark_empty_image);
//        if ("night".equals(ResourceUtil.mode)) {
//            bookmark_empty_image.setImageDrawable(getResources().getDrawable(R.drawable.icon_bookmark_empty_night));
//        } else {
//            bookmark_empty_image.setImageDrawable(getResources().getDrawable(R.drawable.icon_bookmark_empty_light));
//        }

        bookmark_empty_message = (TextView) findViewById(R.id.mask_no_text);
        catalog_empty_refresh = (TextView) findViewById(R.id.catalog_empty_refresh);

        catalog_chapter_hint = (TextView) findViewById(R.id.char_hint);
        catalog_chapter_hint.setVisibility(View.INVISIBLE);

        iv_back_reading = (ImageView) findViewById(R.id.iv_back_reading);
        iv_back_reading.setOnClickListener(this);
        currentView = tab_catalog;

        changeSortState(isPositive);
    }

    private void initListener() {
        if (catalog_main != null) {
            catalog_main.setOnItemClickListener(this);
            catalog_main.setOnScrollListener(this);
        }

        if (bookmark_main != null) {
            bookmark_main.setOnItemClickListener(this);
        }

        if (catalog_empty_refresh != null) {
            catalog_empty_refresh.setOnClickListener(this);
        }
    }

    private void initData(Bundle bundle) {

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
        }

        if (mBookDaoHelper == null)
            mBookDaoHelper = BookDaoHelper.getInstance(this);

        getChapterData();

        loadBookMark();
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

    private void loadBookMark() {
        if (myHandler != null) {
            myHandler.obtainMessage(MESSAGE_FETCH_BOOKMARK).sendToTarget();
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

    public void onEvent(EventBookmark eventBookmark) {
        if (eventBookmark.getType() == EventBookmark.type_delete) {
            AppLog.e(TAG, "eventBookmark:" + eventBookmark.getBookmark().id + " name:" + eventBookmark.getBookmark().chapter_name);
            Bookmark bookmark = eventBookmark.getBookmark();
            if (bookmark != null) {
                ArrayList<Integer> deleteList = new ArrayList<>();
                deleteList.add(bookmark.id);

                startDeleteBookmarks(tab_bookmark, deleteList);
            }
        }
    }

    private void showNullBookMarkNoteLayout() {
        if (currentView == tab_bookmark) {
            if (bookmarkList != null && bookmarkList.size() == 0) {
                if (bookmark_empty != null)
                    bookmark_empty.setVisibility(View.VISIBLE);
            } else {
                if (bookmark_empty != null)
                    bookmark_empty.setVisibility(View.GONE);
            }
        } else {
            if (bookmark_empty != null)
                bookmark_empty.setVisibility(View.GONE);
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

        if (mBookmarkAdapter == null)
            mBookmarkAdapter = new BookmarkAdapter(this, bookmarkList);
        if (bookmark_main != null)
            bookmark_main.setAdapter(mBookmarkAdapter);
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
    }

    private void setBookMark() {
        if (bookmarkList == null) {
            bookmarkList = new ArrayList<>();
        } else {
            bookmarkList.clear();
        }

        if (mBookDaoHelper != null && book != null) {
            ArrayList<Bookmark> list = mBookDaoHelper.getBookMarks(book.book_id);
            if (list != null && list.size() > 0 && bookmarkList != null) {
                for (Bookmark mark : list) {
                    bookmarkList.add(mark);
                }
            }
            if (mBookmarkAdapter != null)
                mBookmarkAdapter.notifyDataSetChanged();
        }
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
            case R.id.tab_catalog:
                if (catalog_main != null) {
                    catalog_main.setVisibility(View.VISIBLE);
                }
                if (rl_catalog_novel != null) {
                    rl_catalog_novel.setVisibility(View.VISIBLE);
                }
                if (bookmark_main != null) {
                    bookmark_main.setVisibility(View.GONE);
                }
                if (bookmark_empty != null) {
                    bookmark_empty.setVisibility(View.GONE);
                }
                if (tab_catalog != null) {
//                    tab_catalog.setTextColor(colorSelected);
//                    tab_catalog.setBackgroundResource(R.drawable.icon_selected_not_finish);
                }
                if (tab_bookmark != null) {
//                    tab_bookmark.setTextColor(colorNormal);
//                    tab_bookmark.setBackgroundResource(R.drawable.icon_select_finish);
                }
                currentView = tab_catalog;
                break;
            case R.id.tab_bookmark:
                if (catalog_main != null) {
                    catalog_main.setVisibility(View.GONE);
                }
                if (rl_catalog_novel != null) {
                    rl_catalog_novel.setVisibility(View.GONE);
                }
                if (bookmark_main != null) {
                    bookmark_main.setVisibility(View.VISIBLE);
                }
                if (tab_catalog != null) {
//                    tab_catalog.setTextColor(colorNormal);
//                    tab_catalog.setBackgroundResource(R.drawable.icon_select_finish);
                }
                if (tab_bookmark != null) {
//                    tab_bookmark.setTextColor(colorSelected);
//                    tab_bookmark.setBackgroundResource(R.drawable.icon_selected_not_finish);
                }
                currentView = tab_bookmark;
                showNullBookMarkNoteLayout();
                break;
            case R.id.iv_catalog_novel_sort://正序、逆序
            case R.id.tv_catalog_novel_sort:
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
            default:
                break;
        }
    }

    private void changeSortState(boolean b) {
        if (tv_catalog_novel_sort != null && iv_catalog_novel_sort != null) {
            if (b) {
                tv_catalog_novel_sort.setText(R.string.catalog_negative);
                sortIcon = R.mipmap.dir_sort_negative;
                //正序的统计
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.rb_catalog_click_zx_btn);

                iv_catalog_novel_sort.setImageResource(sortIcon);

//                if ("night".equals(ResourceUtil.mode)) {
//                    iv_catalog_novel_sort.setImageResource(R.drawable.icon_sort_negative_night);
//                } else {
//                    iv_catalog_novel_sort.setImageResource(R.drawable.icon_sort_negative);
//                }
            } else {
                tv_catalog_novel_sort.setText(R.string.catalog_positive);
                sortIcon = R.mipmap.dir_sort_positive;
                iv_catalog_novel_sort.setImageResource(sortIcon);
                //倒序的统计
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.rb_catalog_click_dx_btn);
//                if ("night".equals(ResourceUtil.mode)) {
//                    iv_catalog_novel_sort.setImageResource(R.drawable.icon_sort_positive_night);
//                } else {
//                    iv_catalog_novel_sort.setImageResource(R.drawable.icon_sort_positive);
//                }
            }
        }
    }

    private void startDeleteBookmarks(final View currentView, final ArrayList<?> list) {
        if (list.size() > 0) {
            final MyDialog mDialog = new MyDialog(CataloguesActivity.this, R.layout.publish_hint_dialog, Gravity.CENTER, true);
            TextView dialog_prompt = (TextView) mDialog.findViewById(R.id.dialog_title);
            dialog_prompt.setText(R.string.prompt);
            TextView dialog_information = (TextView) mDialog.findViewById(R.id.publish_content);
            dialog_information.setText(R.string.determine_remove_bookmark);
            dialog_information.setGravity(Gravity.CENTER);
            Button dialog_cancel = (Button) mDialog.findViewById(R.id.publish_stay);
            dialog_cancel.setText(R.string.cancel);
            Button dialog_confirm = (Button) mDialog.findViewById(R.id.publish_leave);
            dialog_confirm.setText(R.string.delete);
            dialog_confirm.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentView == tab_bookmark) {
                        doDeleteBookmarks(list);
                    }
                    if (mDialog != null)
                        mDialog.dismiss();
                }
            });
            dialog_cancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDialog != null)
                        mDialog.dismiss();
                }
            });
            try {
                mDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void doDeleteBookmarks(ArrayList<?> deleteList) {

        if (mBookDaoHelper == null) {
            mBookDaoHelper = BookDaoHelper.getInstance(this);
        }

        mBookDaoHelper.deleteBookMark((ArrayList<Integer>) deleteList, 0);

        ArrayList<Bookmark> marks = mBookDaoHelper.getBookMarks(book.book_id);
        if (bookmarkList != null)
            bookmarkList.clear();
        if (marks != null && bookmarkList != null) {
            for (Bookmark bookmark : marks) {
                bookmarkList.add(bookmark);
            }
        }
        if (mBookmarkAdapter != null)
            mBookmarkAdapter.notifyDataSetChanged();
        setMarkCount();
    }

    public void setMarkCount() {
        showNullBookMarkNoteLayout();
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
            }
        } else if (parent == bookmark_main) {
            if (bookmarkList != null) {
                Bookmark bookmark = bookmarkList.get(position);
                if (bookmark != null) {
                    bundle.putInt("sequence", bookmark.sequence);
                    bundle.putInt("offset", bookmark.offset);
                    bundle.putSerializable(Constants.REQUEST_ITEM, requestItem);
                }
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
                case MESSAGE_FETCH_BOOKMARK:
                    cataloguesActivity.setBookMark();
                    break;
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