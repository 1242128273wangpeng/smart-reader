package com.dingyue.bookshelf;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.TextUtils;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dingyue.bookshelf.contract.BookHelperContract;
import com.dingyue.contract.CommonContract;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.component.service.CheckNovelUpdateService;
import net.lzbook.kit.book.download.CacheManager;
import net.lzbook.kit.book.view.ConsumeEvent;
import net.lzbook.kit.book.view.MyDialog;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.UpdateCallBack;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.BookUpdate;
import net.lzbook.kit.data.bean.BookUpdateResult;
import net.lzbook.kit.data.bean.SensitiveWords;
import net.lzbook.kit.data.bean.Source;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.pulllist.SuperSwipeRefreshLayout;
import net.lzbook.kit.router.BookRouter;
import net.lzbook.kit.router.RouterConfig;
import net.lzbook.kit.router.RouterUtil;
import net.lzbook.kit.utils.AnimationHelper;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.BaseBookHelper;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.StatServiceUtils;
import net.lzbook.kit.utils.ToastUtils;
import net.lzbook.kit.utils.oneclick.AntiShake;
import net.lzbook.kit.utils.pulllist.DividerItemDecoration;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * 书架页Fragment
 */
public class BookShelfFragment extends Fragment implements UpdateCallBack,
        FrameBookHelper.BookUpdateService, FrameBookHelper.DownLoadStateCallback,
        FrameBookHelper.DownLoadNotify, FrameBookHelper
                .NotificationCallback, BookShelfRemoveHelper.OnMenuDeleteClickListener,
        BookShelfRemoveHelper.OnMenuStateListener, FrameBookHelper
                .BookChanged, BookShelfReAdapter.ShelfItemClickListener,
        BookShelfReAdapter.ShelfItemLongClickListener {

    public static final String ACTION_CHKHIDE = AppUtils.getPackageName();
    private static final int NO_BOOK_DATA_VIEW_SHOW = 0x14;
    private final static int NO_BOOK_DATA_VIEW_GONE = NO_BOOK_DATA_VIEW_SHOW + 1;
    private static final int LONG_PRESS_EDIT = NO_BOOK_DATA_VIEW_GONE + 1;
    private final static int REFRESH_DATA_AFTER_DELETE = LONG_PRESS_EDIT + 1;
    private static final int PULL_REFRESH_DELAY = 30 * 1000;
    private static String TAG = BookShelfFragment.class.getSimpleName();
    private final UiHandler handler = new UiHandler(this);
    public View bookshelf_content;
    public RelativeLayout bookshelf_main;
    public BookShelfRemoveHelper bookShelfRemoveHelper;
    public BookShelfReAdapter bookShelfReAdapter;
    public ArrayList<Book> iBookList = new ArrayList<>();
    public ProgressBar loading_progress_bar;
    public TextView loading_message;
    public SuperSwipeRefreshLayout swipeRefreshLayout;
    //书籍屏蔽相关字段
    protected SensitiveWords bookSensitiveWord;
    ImageView download_bookshelf;
    boolean isUpdateFinish = false;
    ArrayList<Book> bookCollect_checked;
    private WeakReference<Activity> weakReference;
    private Context mContext;
    private BaseFragment.FragmentCallback fragmentCallback;
    private int versionCode;
    private List<String> bookSensitiveWords;
    private boolean noBookSensitive = false;
    ImageView bookshelf_float_ad;
    private LinearLayout bookshelf_empty;
    private ArrayList<Book> bookOnLines;
    private ArrayList<String> update_table;
    private FrameBookHelper frameBookHelper;
    private BookDaoHelper bookDaoHelper;
    private boolean isShowAD = false;
    private boolean isGetAdEvent;
    private long bookrack_update_time;
    private long load_data_finish_time;
    private MyDialog deleteDialog;
    private MyDialog mDialog;
    private SharedPreferences sharedPreferences;
    private ArrayList<Book> esBookOnlineList = new ArrayList<>();
    private ImageView bookshelf_empty_btn;
    private ProgressBar head_pb_view;
    private TextView head_text_view;
    private ImageView head_image_view;
    private RecyclerView recyclerView;
    private ShelfGridLayoutManager layoutManager;
    private boolean isList = true;
    private boolean isShowDownloadBtn = false;
    private RelativeLayout headerReleative;//头部广告view
    private ShowGuideLable mShowGuideLable;//显示引导页
    private Toast toast;

    private ImageView content_head_setting;
    private TextView content_title;
    private ImageView content_head_search, content_download_manage;
    private AntiShake shake = new AntiShake();
    private RelativeLayout content_head_editor;
    private ImageView home_edit_back;
    private TextView home_edit_cancel;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.weakReference = new WeakReference<>(activity);
        fragmentCallback = (BaseFragment.FragmentCallback) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isList = false;
        mContext = getActivity();
        versionCode = AppUtils.getVersionCode();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                getActivity().getApplicationContext());
        initData();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return initView(inflater);
    }

    private View initView(LayoutInflater inflater) {

        if (inflater == null) {
            inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        try {
            bookshelf_content = inflater.inflate(R.layout.fragment_bookshelf, null);
        } catch (InflateException e) {
            e.printStackTrace();
        }

        if (bookshelf_content != null) {
            bookshelf_main = (RelativeLayout) bookshelf_content.findViewById(R.id.bookshelf_main);

            content_head_setting = (ImageView) bookshelf_content.findViewById(
                    R.id.content_head_setting);
            content_head_search = (ImageView) bookshelf_content.findViewById(
                    R.id.content_head_search);
            content_download_manage = (ImageView) bookshelf_content.findViewById(
                    R.id.content_download_manage);

            //长按编辑栏布局
            content_head_editor = (RelativeLayout) bookshelf_content.findViewById(
                    R.id.content_head_editor);
            home_edit_back = (ImageView) bookshelf_content.findViewById(R.id.home_edit_back);
            home_edit_cancel = (TextView) bookshelf_content.findViewById(R.id.home_edit_cancel);

            initClick();

            bookshelf_empty = (LinearLayout) bookshelf_content.findViewById(R.id.bookshelf_empty);
            bookshelf_empty_btn = (ImageView) bookshelf_content.findViewById(
                    R.id.bookshelf_empty_btn);
            bookshelf_empty.setVisibility(View.GONE);

            bookrack_update_time = AppUtils.getLongPreferences(mContext, "bookrack_update_time",
                    System.currentTimeMillis());

//            book_shelf_loading = (RelativeLayout) bookshelf_content.findViewById(R.id
// .book_shelf_loading);
//            book_shelf_loading.setVisibility(View.GONE);
            loading_progress_bar = (ProgressBar) bookshelf_content.findViewById(
                    R.id.loading_progressbar);
//            download_bookshelf = (ImageView) bookshelf_content.findViewById(R.id
// .fab_goto_down_act);
//           if(download_bookshelf.getVisibility()==View.VISIBLE){
//               isShowDownloadBtn = true;
//                download_bookshelf.setOnClickListener(new OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent intent = new Intent(mContext, DownloadManagerActivity.class);
//                        startActivity(intent);
//                    }
//                });
//            }
//            loading_progress = (ProgressBar) bookshelf_content.findViewById(R.id
// .loading_progress);
//            loading_message = (TextView) bookshelf_content.findViewById(R.id.loading_message);

            //初始化RecyclerView
            initRecyclerView();

            Activity activity = weakReference.get();
            if (activity == null) {
                return null;
            }

            initRemoveHelper();
        }

        return bookshelf_content;
    }


    @Override
    public void onItemClick(View view, int position) {
        AppLog.e(TAG, "BookShelfItemClick");
        if (iBookList == null || position < 0 || position > iBookList.size()) {
            return;
        }
        if (bookShelfRemoveHelper.isRemoveMode()) {
            bookShelfRemoveHelper.setCheckPosition(position);
        }
        if (!bookShelfRemoveHelper.isRemoveMode()) {
            intoNovelContent(position);
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {
        if (!bookShelfRemoveHelper.isRemoveMode()) {
            Book book = (Book) this.iBookList.get(position);
            int index = -1;
            if (book != null && this.bookOnLines.contains(book)) {
                for (int i = 0; i < this.bookOnLines.size(); i++) {
                    if (this.bookOnLines.get(i) == book) {
                        index = i;
                        break;
                    }
                }
            }
            this.bookShelfRemoveHelper.showRemoveMenu(this.swipeRefreshLayout);
//            if (index != -1) {
//                this.bookShelfRemoveHelper.setCheckPosition(index);
//            }
            if (bookshelf_float_ad != null) {
                bookshelf_float_ad.setVisibility(View.GONE);
            }
            StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.SHELF_PAGE,
                    StartLogClickUtil.LONGTIMEBOOKSHELFEDIT);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initListener();

        if (fragmentCallback != null) {
            fragmentCallback.frameHelper();
        }

        Activity activity = weakReference.get();
        if (activity == null) {
            return;
        }

//        if (frameBookHelper == null) {
//            if (activity instanceof HomeActivity) {
//                frameBookHelper = ((HomeActivity) activity).frameHelper;
//            }
//        }

        if (frameBookHelper != null) {
            frameBookHelper.setBookUpdate(this);
            frameBookHelper.setDownLoadState(this);
            frameBookHelper.setDownNotify(this);
            frameBookHelper.setNotification(this);
            frameBookHelper.initDownUpdateService();
            frameBookHelper.clickNotification(activity.getIntent());
            frameBookHelper.setBookChanged(this);
        }

        //根据书架数量确定是否刷新
        if (bookOnLines.size() > 0) {
            swipeRefreshLayout.setRefreshing(true);
        }
    }

    private void initRemoveHelper() {
        if (bookShelfRemoveHelper == null) {
            bookShelfRemoveHelper = new BookShelfRemoveHelper(mContext, bookShelfReAdapter);
        }
        if (recyclerView != null) {
            bookShelfRemoveHelper.setLayout(swipeRefreshLayout);
        }
        if (fragmentCallback != null) {
            fragmentCallback.getRemoveMenuHelper(bookShelfRemoveHelper);
        }
        bookShelfRemoveHelper.setOnMenuStateListener(this);
        bookShelfRemoveHelper.setOnMenuDeleteListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


        if (frameBookHelper != null) {
            frameBookHelper.recycleCallback();
        }

        if (bookOnLines != null) {
            bookOnLines.clear();
        }
    }

    /**
     * 根据网络及书架数据设置下拉刷新模式为直接完成或显式下拉
     */
    protected boolean isPullAction(ArrayList<Book> rackBookList,
            SuperSwipeRefreshLayout refreshLayout) {
        if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
            refreshLayout.setRefreshing(false);
            showToastDelay(R.string.bookshelf_refresh_network_problem);
            return false;
        }

        //根据书架数量确定是否刷新
//        if (rackBookList.size() > 0) {
//            refreshLayout.setRefreshing(false);
//        }
        return true;
    }

    /**
     * 查Book数据库更新界面
     */
    public void updateUI() {

        getBookListData();

        if (!sharedPreferences.getBoolean(versionCode + Constants.BOOKSHELF_GUIDE_TAG, false)
                && iBookList.size() > 0 && mShowGuideLable != null) {
            mShowGuideLable.showGuidLable();
        }
        if (bookShelfReAdapter == null) {
            bookShelfReAdapter = new BookShelfReAdapter(getActivity(), iBookList, this, this,
                    isList);
        }
        if (bookShelfReAdapter != null) {
            for (int i = 0; i < bookOnLines.size(); i++) {
                Book book = bookOnLines.get(i);
                setUpdateState(book);
            }

            bookShelfReAdapter.setUpdate_table(update_table);
            bookShelfReAdapter.notifyDataSetChanged();
        }
        //判断用户是否是当日首次打开应用,并上传书架的id
        long first_time = sharedPreferences.getLong(Constants.TODAY_FIRST_POST_BOOKIDS, 0);

        long currentTime = System.currentTimeMillis();
        boolean b = AppUtils.isToday(first_time, currentTime);
        if (!b) {
            StringBuilder bookIdList = new StringBuilder();
            for (int i = 0; i < iBookList.size(); i++) {
                Book book = iBookList.get(i);
                if (!TextUtils.isEmpty(book.book_id)) {
                    bookIdList.append(book.book_id);
                    bookIdList.append((book.readed == 1) ? "_1" : "_0");//1已读，0未读
                    bookIdList.append((i == iBookList.size() - 1) ? "" : "$");
                }
            }
            Map<String, String> data = new HashMap<>();
            data.put("bookid", bookIdList.toString());
            StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.MAIN_PAGE,
                    StartLogClickUtil.BOOKLIST, data);
            sharedPreferences.edit().putLong(Constants.TODAY_FIRST_POST_BOOKIDS,
                    currentTime).apply();
        }
    }

    private void initData() {
        Activity activity = weakReference.get();
        if (activity == null) {
            return;
        }
        if (bookDaoHelper == null && mContext != null) {
            bookDaoHelper = BookDaoHelper.getInstance();
        }

        esBookOnlineList = bookDaoHelper.getBooksOnLineListYS();

        if (update_table == null) {
            update_table = new ArrayList<>();
        }
        update_table.clear();

        bookCollect_checked = new ArrayList<>();
    }

    private void initListener() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnPullRefreshListener(
                    new SuperSwipeRefreshLayout.OnPullRefreshListener() {

                        @Override
                        public void onRefresh() {
                            head_text_view.setText("正在刷新");
                            head_image_view.setVisibility(View.GONE);
                            head_pb_view.setVisibility(View.VISIBLE);
                            checkBookUpdate();
                        }

                        @Override
                        public void onPullDistance(int distance) {
                            // pull distance
                        }

                        @Override
                        public void onPullEnable(boolean enable) {
                            head_pb_view.setVisibility(View.GONE);
                            head_text_view.setText(enable ? "松开刷新" : "下拉刷新");
                            head_image_view.setVisibility(View.VISIBLE);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                head_image_view.setRotation(enable ? 180 : 0);
                            }
                        }
                    });
        }
        if (bookshelf_empty_btn != null) {
            bookshelf_empty_btn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (fragmentCallback != null) {
                        fragmentCallback.setSelectTab(1);

                        StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.SHELF_PAGE,
                                StartLogClickUtil.TOBOOKCITY);
                    }
                }
            });
        }
    }

    private void initRecyclerView() {

        if (bookOnLines == null) {
            bookOnLines = new ArrayList<>();
        }
        if (bookShelfReAdapter == null) {
            bookShelfReAdapter = new BookShelfReAdapter(getActivity(), iBookList, this, this,
                    isList);
        }

        swipeRefreshLayout = (SuperSwipeRefreshLayout) bookshelf_content.findViewById(
                R.id.bookshelf_refresh_view);
        swipeRefreshLayout.setHeaderViewBackgroundColor(0x00000000);
        swipeRefreshLayout.setHeaderView(createHeaderView());
        swipeRefreshLayout.setTargetScrollWithLayout(true);

        recyclerView = (RecyclerView) bookshelf_content.findViewById(R.id.recycler_view);
        recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 12);
        if (isList) {
            layoutManager = new ShelfGridLayoutManager(mContext, 1);
        } else {
            layoutManager = new ShelfGridLayoutManager(mContext, 3);
            //有分割线的九宫格
            if (!"cc.quanbennovel".equals(ACTION_CHKHIDE)) {
                int typeColor = R.color.color_gray_e8e8e8;
                recyclerView.addItemDecoration(
                        new DividerItemDecoration(mContext, DividerItemDecoration.BOTH_SET, 2,
                                mContext.getResources().getColor(typeColor)));
            }
        }
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setFocusable(false);//放弃焦点
//      recyclerView.getItemAnimator().setSupportsChangeAnimations(false);
        recyclerView.getItemAnimator().setAddDuration(0);
        recyclerView.getItemAnimator().setChangeDuration(0);
        recyclerView.getItemAnimator().setMoveDuration(0);
        recyclerView.getItemAnimator().setRemoveDuration(0);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.setAdapter(bookShelfReAdapter);

    }

    /**
     * 从数据库中取书架中书本显示内容
     */
    private ArrayList<Book> getBookListData() {

        ArrayList<Book> booksOnLine = bookDaoHelper.getBooksOnLineList();
        if (bookOnLines == null) {
            bookOnLines = new ArrayList<>();
        }

        if (bookOnLines != null) {
            bookOnLines.clear();
            bookOnLines.addAll(booksOnLine);

            setBookListHeadData(bookOnLines.size());
        }
        if (iBookList != null) {
            iBookList.clear();
            if (!booksOnLine.isEmpty()) {
                Collections.sort(booksOnLine,
                        new CommonContract.MultiComparator(Constants.book_list_sort_type));
                iBookList.addAll(booksOnLine);

            }
        }
        return iBookList;
    }


    private void setBookListHeadData(int num) {
        if (num == 0 && swipeRefreshLayout != null) {
            swipeRefreshLayout.setPullToRefreshEnabled(false);
            handler.obtainMessage(NO_BOOK_DATA_VIEW_SHOW).sendToTarget();
        } else if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setPullToRefreshEnabled(true);
            handler.obtainMessage(NO_BOOK_DATA_VIEW_GONE).sendToTarget();
        }
    }


    private void setUpdateState(Book book) {
        if (book != null) {
            if (book.update_status == 1) {
                if (!update_table.contains(book.book_id)) {
                    update_table.add(book.book_id);
                }
            } else {
                if (update_table.contains(book.book_id)) {
                    update_table.remove(book.book_id);
                }
            }
        }
    }

    /**
     * 下拉时检查更新
     */
    private void checkBookUpdate() {

        if (!isPullAction(bookOnLines, swipeRefreshLayout)) {
            return;
        }

        long start_pull_time = System.currentTimeMillis();
        long delay = Math.abs(start_pull_time - load_data_finish_time);

        // 刷新间隔小于30秒无效
        if (delay <= PULL_REFRESH_DELAY) {
            swipeRefreshLayout.onRefreshComplete();
            AppLog.d(TAG, "刷新间隔小于30秒不请求数据");
            showToastDelay(R.string.main_update_no_new);
        } else {
            // 刷新间隔大于30秒直接请求更新，
            addUpdateTask();
            AppLog.d(TAG, "刷新间隔大于30秒请求数据");
        }

    }

    private void addUpdateTask() {
        if (frameBookHelper != null) {
            CheckNovelUpdateService updateService = frameBookHelper.getUpdateService();
            if (bookDaoHelper.getBooksCount() > 0 && updateService != null) {
                ArrayList<Book> list = bookDaoHelper.getBooksList();
                AppLog.e("BookUpdateCount", "BookUpdateCount: " + list.size());
                updateService.checkUpdate(
                        BookHelperContract.INSTANCE.loadBookUpdateTaskData(list, this));
            }
        }
    }

    private void showToastDelay(final int textId) {
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                Activity activity = weakReference.get();
                if (isAdded() && activity != null) {
                    showToast(textId);
                }
            }
        }, 2000);
    }

    private void showToastDelay(final String text) {
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                Activity activity = weakReference.get();
                if (isAdded() && activity != null) {

                }
            }
        }, 2000);
    }

    private void showToast(final int text) {
        Activity activity = weakReference.get();
        if (isAdded() && activity != null) {
            showToastDelay(text);
        }
    }

    private String getSelfString(Context context, int StringId) {
        if (isAdded() && context != null) {
            try {
                return context.getResources().getString(StringId);
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
                return "";
            }
        }
        return "";
    }

    private void emptyViewShow() {
        bookshelf_empty.setVisibility(View.VISIBLE);
    }

    private void emptyViewGone() {
        bookshelf_empty.setVisibility(View.GONE);
    }

    private void longPressEdit() {

    }

    private void refreshDataAfterDelete() {
//        recyclerView.smoothScrollToPosition(0);
        updateUI();
        if (bookShelfRemoveHelper != null) {
            bookShelfRemoveHelper.dismissRemoveMenu();
        }
        if (bookCollect_checked != null && bookCollect_checked.size() > 0) {
            bookCollect_checked.clear();
        }
    }

    //打开长按编辑模式时，过滤掉广告
    private void filterAd() {
        for (int i = 0; i < iBookList.size(); i++) {
            //若当前的书籍是广告，则长按状态删除广告
            if (iBookList.get(i).book_type == -2) {
                iBookList.remove(i);
            }
        }
    }

    /**
     * 点击书架条目情况
     */
    private void intoNovelContent(int index) {
        if (index >= iBookList.size() || index < 0) {
            return;
        }

        Book book = iBookList.get(index);
        clickAction(book);

        if (book != null) {
            Map<String, String> data = new HashMap<>();
            data.put("bookid", book.book_id);
            data.put("rank", String.valueOf(index + 1));
            StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.SHELF_PAGE,
                    StartLogClickUtil.BOOKCLICK, data);
        }
    }

    /**
     * listitem点击或更新通知点击处理
     */
    private void clickAction(Book book) {
        final Activity activity = weakReference.get();
        if (activity == null || book == null) {
            return;
        }
        if (!TextUtils.isEmpty(book.book_id) && book.book_type == 0) {
            cancelUpdateStatus(book.book_id);
        }

        if (Constants.isShielding && !noBookSensitive && bookSensitiveWords.contains(
                String.valueOf(book.book_id))) {
            ToastUtils.showToastNoRepeat("抱歉，该小说已下架！");
        } else {
            BookRouter.INSTANCE.navigateCoverOrRead(activity, book, 0);
//            BookHelper.goToCoverOrRead(weakReference.get().getApplicationContext(),
// weakReference.get(), book,0);
        }
    }

    /**
     * 消除数据库中更新状态
     */
    private void cancelUpdateStatus(String book_id) {
        Book book = new Book();
        book.book_id = book_id;
        book.update_status = 0;
        if (update_table.contains(book.book_id)) {
            update_table.remove(book_id);
            bookDaoHelper.updateBook(book);
        }
    }

    public void setResultRefresh() {
        updateUI();
    }

    @Override
    public void onSuccess(BookUpdateResult result) {
        load_data_finish_time = System.currentTimeMillis();
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.onRefreshComplete();
        }
        onUpdateSuccessToast(result);
        bookrack_update_time = System.currentTimeMillis();
        AppUtils.setLongPreferences(mContext, "bookrack_update_time", bookrack_update_time);
        AppLog.e(TAG, "onSuccess的刷新ui调用");
        isShowAD = true;
        updateUI();
        isGetAdEvent = false;
        if (!isUpdateFinish) {
            isUpdateFinish = true;
        }
    }

    @Override
    public void onException(Exception e) {
        load_data_finish_time = System.currentTimeMillis();
        showToastDelay(R.string.bookshelf_refresh_network_problem);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.onRefreshComplete();
        }
        if (!isUpdateFinish) {
            isUpdateFinish = true;
        }
    }

    protected void onUpdateSuccessToast(BookUpdateResult result) {
        int newsCount = 0;
        ArrayList<BookUpdate> hasUpdateList = new ArrayList<>();
        if (result != null && result.items != null && !result.items.isEmpty()) {
            ArrayList<BookUpdate> bookUpdates = result.items;
            int size = bookUpdates.size();
            for (int i = 0; i < size; i++) {
                BookUpdate item = bookUpdates.get(i);
                if (!TextUtils.isEmpty(item.book_id) && item.update_count != 0) {
                    newsCount++;
                    hasUpdateList.add(item);
                }
            }
            if (hasUpdateList.size() != 0) {
                showMoreToast(newsCount, hasUpdateList);
            }
        } else {
            showToastDelay(R.string.main_update_no_new);
        }
    }

    private void showMoreToast(int newsCount, ArrayList<BookUpdate> hasUpdateList) {
        BookUpdate bookUpdate = hasUpdateList.get(0);
        String book_name = bookUpdate.book_name;
        Activity activity = weakReference.get();
        if (activity == null) {
            return;
        }
        if (book_name != null && !TextUtils.isEmpty(book_name)) {

            if (newsCount == 1) {
                showToastDelay("《" + book_name + getSelfString(mContext,
                        R.string.bookshelf_one_book_update) + bookUpdate.last_chapter_name);
            } else {
                int update_size = hasUpdateList.size();
                showToastDelay("《" + book_name + getSelfString(mContext,
                        R.string.bookshelf_more_book_update) + update_size + getSelfString(mContext,
                        R.string.bookshelf_update_chapters));
            }
        }
    }

    public void doUpdateBook() {
        addUpdateTask();
    }

    @Override
    public void doUpdateBook(CheckNovelUpdateService updateService) {
        Activity activity = weakReference.get();
        if (updateService != null) {
            updateService.setBookUpdateListener(
                    (CheckNovelUpdateService.OnBookUpdateListener) activity);
        }

        addUpdateTask();

    }

    @Override
    public void changeDownLoadBtn(boolean isDownLoading) {
    }

    @Override
    public void doNotifyDownload() {
        updateUI();
    }

    @Override
    public void notification(String gid) {
        if (!TextUtils.isEmpty(gid)) {
            Book book = (Book) bookDaoHelper.getBook(gid, 0);
            if (book != null) {
                clickAction(book);
            }
        }
    }

    /**
     * 菜单删除按钮触发删除动作
     */
    private void deleteBooks(final ArrayList<Book> deleteBooks, ArrayList<Book> rankList,
            final boolean justDeleteCache) {
        final MyDialog myDialog = new MyDialog(getActivity(), R.layout.dialog_download_clean);
        myDialog.setCanceledOnTouchOutside(false);
        myDialog.setCancelable(false);
        ((TextView) myDialog.findViewById(R.id.dialog_msg)).setText(R.string.tip_cleaning);
        myDialog.show();
        final int size = deleteBooks.size();
        new Thread(new Runnable() {

            @Override
            public void run() {

                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < size; i++) {
                    Book book = deleteBooks.get(i);

                    sb.append(book.book_id);
                    sb.append((book.readed == 1) ? "_1" : "_0");
                    sb.append((i == size - 1) ? "" : "$");


                }
                // 删除书架数据库和章节数据库
                if (bookDaoHelper != null && !justDeleteCache) {
                    bookDaoHelper.deleteBook(deleteBooks);
                } else {
                    for (Book book : deleteBooks) {
                        CacheManager.INSTANCE.remove(book.book_id);
                        BaseBookHelper.removeChapterCacheFile(book);
                    }
                }


                myDialog.dismiss();

                handler.obtainMessage(REFRESH_DATA_AFTER_DELETE).sendToTarget();

                if (justDeleteCache) {
                    Map<String, String> data1 = new HashMap<>();
                    data1.put("type", "1");
                    data1.put("number", String.valueOf(size));
                    data1.put("bookids", sb.toString());
                    StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.SHELFEDIT_PAGE,
                            StartLogClickUtil.DELETE1, data1);
                }
            }
        }).start();
    }

    @Override
    public void onMenuDelete(HashSet<Integer> checked_state) {
        ArrayList<Book> checkedBooks = new ArrayList<>();
        checkedBooks.clear();
        int size = iBookList.size();
        for (int i = 0; i < size; i++) {
            if (checked_state.contains(i)) {
                checkedBooks.add(iBookList.get(i));
            }
        }
        onMenuDeleteAction(checkedBooks);
    }

    private void onMenuDeleteAction(final ArrayList<Book> deleteBooks) {
        Activity activity = weakReference.get();
        if (activity == null) {
            return;
        }
        if (deleteBooks.size() > 0) {
            deleteDialog = new MyDialog(activity, R.layout.layout_addshelf_dialog);
            TextView base_dialog_title = (TextView) deleteDialog.findViewById(R.id.dialog_title);
            base_dialog_title.setText(R.string.prompt);
            TextView textView = (TextView) deleteDialog.findViewById(R.id.tv_update_info_dialog);
            textView.setGravity(17);
            textView.setText(R.string.determine_delete_book);
            final CheckBox checkBox = (CheckBox) deleteDialog.findViewById(R.id.cb_hint);
            checkBox.setText(R.string.determine_clear_book);
            Button button = (Button) deleteDialog.findViewById(R.id.bt_cancel);
            button.setText(R.string.cancel);
            Button button2 = (Button) deleteDialog.findViewById(R.id.bt_ok);
            button2.setText(R.string.confirm);
            button2.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    deleteDialog.dismiss();
                    deleteBooks(deleteBooks, bookOnLines, checkBox.isChecked());
                    StatServiceUtils.statAppBtnClick(mContext,
                            StatServiceUtils.bs_click_delete_ok_btn);
                }
            });
            button.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    deleteDialog.dismiss();
                    Map<String, String> data = new HashMap<>();
                    data.put("type", "2");
                    StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.SHELFEDIT,
                            StartLogClickUtil.DELETE1, data);
                    StatServiceUtils.statAppBtnClick(mContext,
                            StatServiceUtils.bs_click_delete_cancel_btn);
                }
            });
            deleteDialog.show();

        }
    }

    @Override
    public void getMenuShownState(boolean state) {
        if (state) {
            swipeRefreshLayout.setPullToRefreshEnabled(false);
            if (isShowDownloadBtn) {
                download_bookshelf.setVisibility(View.GONE);
            }
        } else {
            if (bookOnLines.size() != 0) {
                swipeRefreshLayout.setPullToRefreshEnabled(true);
            }
            if (isShowDownloadBtn) {
                download_bookshelf.setVisibility(View.VISIBLE);
            }
            updateUI();
        }
//        if (fragmentCallback != null) {
//            fragmentCallback.getMenuShownState(state);
//        }
        showEditor(state);
    }

    public void showEditor(boolean state) {
        if (state) {
            if (!content_head_editor.isShown()) {
                Animation showAnimation = new AlphaAnimation(0.0f, 1.0f);
                showAnimation.setDuration(200);
                content_head_editor.startAnimation(showAnimation);
                content_head_editor.setVisibility(View.VISIBLE);

            }
//            AnimationHelper.smoothScrollTo(viewPager, 0);
        } else {
            if (content_head_editor.isShown()) {
                content_head_editor.setVisibility(View.GONE);

            }
//            AnimationHelper.smoothScrollTo(viewPager, 0);
        }
    }

    @Override
    public void getAllCheckedState(boolean isAll) {
        if (fragmentCallback != null) {
            fragmentCallback.getAllCheckedState(isAll);
        }
    }

    @Override
    public void doHideAd() {
        if (isShowAD) {
            filterAd();
        }
    }

    public void updateBook() {
        AppLog.e(TAG, "updateBook的刷新UI调用");
        updateUI();
    }

    private Book changeBookSource(Source source, boolean changeReadFlag) {
        BookDaoHelper bookDaoHelper = BookDaoHelper.getInstance();
        Book iBook = bookDaoHelper.getBook(source.book_id, 0);
        iBook.book_source_id = source.book_source_id;
        iBook.site = source.host;
        iBook.dex = source.dex;
        iBook.last_updatetime_native = source.update_time;

        if (changeReadFlag) {
            // 更新阅读标记为未阅读0、章节序为-1
            iBook.readed = 0;
            iBook.sequence = -1;
        }

        if (bookDaoHelper.isBookSubed(source.book_id)) {
            bookDaoHelper.updateBook(iBook);
        }
        return iBook;
    }

    private View createHeaderView() {
        View headerView = LayoutInflater.from(swipeRefreshLayout.getContext())
                .inflate(R.layout.layout_head, null);
        head_pb_view = (ProgressBar) headerView.findViewById(R.id.head_pb_view);
        head_text_view = (TextView) headerView.findViewById(R.id.head_text_view);
        head_text_view.setText("下拉刷新");
        head_image_view = (ImageView) headerView.findViewById(R.id.head_image_view);
        head_image_view.setVisibility(View.VISIBLE);
        head_image_view.setImageResource(R.drawable.pulltorefresh_down_arrow);
        head_pb_view.setVisibility(View.GONE);
        return headerView;
    }

    public void initClick() {

        content_head_setting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shake.check(R.id.content_head_setting)) {
                    return;
                }
                StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.MAIN_PAGE,
                        StartLogClickUtil.PERSONAL);
                RouterUtil.INSTANCE.navigation(RouterConfig.SETTING_ACTIVITY);
                EventBus.getDefault().post(new ConsumeEvent(R.id.redpoint_home_setting));
//                startActivity(new Intent(context, SettingActivity.class));
                net.lzbook.kit.utils.StatServiceUtils.statAppBtnClick(mContext,
                        net.lzbook.kit.utils.StatServiceUtils.bs_click_mine_menu);
            }
        });
        content_head_search.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                RouterUtil.INSTANCE.navigation(RouterConfig.SEARCHBOOK_ACTIVITY);
//                if(bottomType ==2){
//                    StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil
// .RECOMMEND_PAGE, StartLogClickUtil.QG_TJY_SEARCH);
//                }else if(bottomType==3){
//                    StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.TOP_PAGE,
// StartLogClickUtil.QG_BDY_SEARCH);
//                }else if(bottomType == 4){
//                    StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.CLASS_PAGE,
// StartLogClickUtil.QG_FL_SEARCH);
//                }else{
//                    StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.MAIN_PAGE,
// StartLogClickUtil.SEARCH);
//                }

                net.lzbook.kit.utils.StatServiceUtils.statAppBtnClick(mContext,
                        net.lzbook.kit.utils.StatServiceUtils.bs_click_search_btn);
            }
        });
        content_download_manage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                RouterUtil.INSTANCE.navigation(RouterConfig.DOWNLOAD_MANAGER_ACTIVITY);

//                downloadIntent.setClass(context, DownloadManagerActivity.class);
//                startActivity(downloadIntent);
                net.lzbook.kit.utils.StatServiceUtils.statAppBtnClick(mContext,
                        net.lzbook.kit.utils.StatServiceUtils.bs_click_download_btn);
                StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.MAIN_PAGE,
                        StartLogClickUtil.CACHEMANAGE);
            }
        });
        home_edit_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                bookShelfRemoveHelper.dismissRemoveMenu();
                StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.SHELFEDIT_PAGE,
                        StartLogClickUtil.CANCLE1);
            }
        });
        home_edit_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                bookShelfRemoveHelper.dismissRemoveMenu();
                StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.SHELFEDIT_PAGE,
                        StartLogClickUtil.CANCLE1);
            }
        });
    }

    public static class UiHandler extends Handler {
        private WeakReference<BookShelfFragment> reference;

        UiHandler(BookShelfFragment vpBook) {
            reference = new WeakReference<>(vpBook);
        }

        @Override
        public void handleMessage(Message msg) {
            BookShelfFragment bookShelfFragment = reference.get();
            if (bookShelfFragment == null) {
                return;
            }
            switch (msg.what) {

                case NO_BOOK_DATA_VIEW_SHOW:
                    bookShelfFragment.emptyViewShow();
                    break;
                case NO_BOOK_DATA_VIEW_GONE:
                    bookShelfFragment.emptyViewGone();
                    break;
                case LONG_PRESS_EDIT:
                    bookShelfFragment.longPressEdit();
                    break;
                case REFRESH_DATA_AFTER_DELETE:
                    bookShelfFragment.refreshDataAfterDelete();
                    break;
            }
        }
    }


    //显示引导图
    public interface ShowGuideLable {
        void showGuidLable();
    }

    public void setShowGuideLable(ShowGuideLable mShowGuideLable) {
        this.mShowGuideLable = mShowGuideLable;
    }

    public void showToastShort(int resId) {
        if (TextUtils.isEmpty(String.valueOf(resId))) {
            return;
        }
        if (toast == null) {
            toast = Toast.makeText(BaseBookApplication.getGlobalContext(), resId,
                    Toast.LENGTH_SHORT);
        } else {
            toast.setText(resId);
        }
        if (toast != null) {
            toast.show();
        }
    }

}
