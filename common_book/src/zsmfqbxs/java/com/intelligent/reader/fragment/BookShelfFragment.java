package com.intelligent.reader.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.dingyueads.sdk.Bean.AdSceneData;
import com.dingyueads.sdk.Bean.Advertisement;
import com.dingyueads.sdk.Native.YQNativeAdInfo;
import com.dingyueads.sdk.NativeInit;
import com.intelligent.reader.BuildConfig;
import com.intelligent.reader.R;
import com.intelligent.reader.activity.HomeActivity;
import com.intelligent.reader.adapter.BookShelfReAdapter;
import com.intelligent.reader.app.BookApplication;
import com.intelligent.reader.read.help.BookHelper;
import com.intelligent.reader.util.BookShelfRemoveHelper;
import com.intelligent.reader.util.ShelfGridLayoutManager;

import net.lzbook.kit.ad.OwnNativeAdManager;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.component.service.CheckNovelUpdateService;
import net.lzbook.kit.book.view.MyDialog;
import net.lzbook.kit.cache.imagecache.ImageCacheManager;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.UpdateCallBack;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.BookUpdate;
import net.lzbook.kit.data.bean.BookUpdateResult;
import net.lzbook.kit.data.bean.EventBookshelfAd;
import net.lzbook.kit.data.bean.SensitiveWords;
import net.lzbook.kit.data.bean.Source;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.pulllist.SuperSwipeRefreshLayout;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.FrameBookHelper;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.StatServiceUtils;
import net.lzbook.kit.utils.StatisticManager;
import net.lzbook.kit.utils.ToastUtils;
import net.lzbook.kit.utils.Tools;
import net.lzbook.kit.utils.pulllist.DividerItemDecoration;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * 书架页Fragment
 */
public class BookShelfFragment extends Fragment implements UpdateCallBack,
        FrameBookHelper.BookUpdateService, FrameBookHelper.DownLoadStateCallback, FrameBookHelper.DownLoadNotify, FrameBookHelper
                .NotificationCallback, BookShelfRemoveHelper.OnMenuDeleteClickListener, BookShelfRemoveHelper.OnMenuStateListener, FrameBookHelper
                .BookChanged, BookShelfReAdapter.ShelfItemClickListener, BookShelfReAdapter.ShelfItemLongClickListener {

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
    //自有广告管理类
    private OwnNativeAdManager ownNativeAdManager;
    private LinearLayout bookshelf_empty;
    private ArrayList<Book> bookOnLines;
    private ArrayList<String> update_table;
    private ArrayList<String> down_table;
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
    private StatisticManager statisticManager;//AD 位

    private HashMap<Integer, YQNativeAdInfo> adInfoHashMap = new HashMap<>();

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
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        if (!Constants.isHideAD) {
            ownNativeAdManager = OwnNativeAdManager.getInstance(getActivity());
            ownNativeAdManager.setActivity(getActivity());
        }
        initData();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

            bookshelf_empty = (LinearLayout) bookshelf_content.findViewById(R.id.bookshelf_empty);
            bookshelf_empty_btn = (ImageView) bookshelf_content.findViewById(R.id.bookshelf_empty_btn);
            bookshelf_empty.setVisibility(View.GONE);

            bookrack_update_time = AppUtils.getLongPreferences(mContext, "bookrack_update_time", System.currentTimeMillis());
            headerReleative = (RelativeLayout) bookshelf_content.findViewById(R.id.headerLayout);//header AD

//            book_shelf_loading = (RelativeLayout) bookshelf_content.findViewById(R.id.book_shelf_loading);
//            book_shelf_loading.setVisibility(View.GONE);
            loading_progress_bar = (ProgressBar) bookshelf_content.findViewById(R.id.loading_progressbar);
//            download_bookshelf = (ImageView) bookshelf_content.findViewById(R.id.fab_goto_down_act);
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
//            loading_progress = (ProgressBar) bookshelf_content.findViewById(R.id.loading_progress);
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
            bookShelfRemoveHelper.showRemoveMenu(swipeRefreshLayout);
            StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.SHELF_PAGE, StartLogClickUtil.LONGTIMEBOOKSHELFEDIT);
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

        if (frameBookHelper == null) {
            if (activity instanceof HomeActivity) {
                frameBookHelper = ((HomeActivity) activity).frameHelper;
            }
        }

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
        if (ownNativeAdManager != null) {
            ownNativeAdManager.setActivity(getActivity());
        }
    }


//    private boolean isGetEvent;

    public void onEvent(final EventBookshelfAd eventBookshelfAd) {
//        if (!isGetEvent) return;
        if (eventBookshelfAd.type_ad.equals(NativeInit.CustomPositionName.SHELF_POSITION.toString())) {
            Activity activity = getActivity();
            if (activity != null && isAdded()) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        isShowAD = true;
                        if (eventBookshelfAd.yqNativeAdInfo != null) {
                            eventBookshelfAd.yqNativeAdInfo.setAvailableTime(System.currentTimeMillis());
                        }
                        adInfoHashMap.put(eventBookshelfAd.position, eventBookshelfAd.yqNativeAdInfo);
                        getBookListData();
                        bookShelfReAdapter.notifyDataSetChanged();
                        AppLog.e(TAG, "notifyDataSetChanged");
                        StatServiceUtils.statBookEventShow(mContext, StatServiceUtils.type_ad_shelf);
                    }
                });
            }
        } else if (eventBookshelfAd.type_ad.equals("bookshelfclick_360")) {
            if (eventBookshelfAd.yqNativeAdInfo != null) {
                eventBookshelfAd.yqNativeAdInfo.setAvailableTime(System.currentTimeMillis() + 2000);
            }
            adInfoHashMap.put(eventBookshelfAd.position, eventBookshelfAd.yqNativeAdInfo);
        }
    }


    private void setAdBook(ArrayList<Book> booksOnLine) {
        AppLog.e("wyhad1-1", adInfoHashMap.toString());

        //长按删除状态下不请求广告
        if (!isShowAD || bookShelfRemoveHelper.isRemoveMode()) {
            return;
        }
        AppLog.e("wyhad1-1", this.isResumed() + "");
        if (!this.isResumed()) {
            return;
        }
        if(Constants.book_shelf_state !=3){//表示横条，九宫格一起显示 列表首位不显示广告 其他位置间隔显示
        YQNativeAdInfo adInfo;
        if (adInfoHashMap.containsKey(0) && adInfoHashMap.get(0) != null && adInfoHashMap.get(0).getAdvertisement() != null
                && (System.currentTimeMillis() - adInfoHashMap.get(0).getAvailableTime() < 3000 || !adInfoHashMap.get(0).getAdvertisement().isShowed)) {
            adInfo = adInfoHashMap.get(0);
        } else {
            adInfo = ownNativeAdManager.getSingleADInfoNew(0, NativeInit.CustomPositionName.SHELF_POSITION);
            if (adInfo != null) {
                adInfo.setAvailableTime(System.currentTimeMillis());
                adInfoHashMap.put(0, adInfo);
            }
        }
        if (adInfo != null) {
            Book book1 = new Book();
            book1.book_type = -2;
            book1.info = adInfo;
            AppLog.e("wyhad1-1", "adInfo：" + adInfo.getAdvertisement().toString());
            book1.rating = Tools.getIntRandom();
            try {
                iBookList.add(0, book1);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }

        } else {
            AppLog.e("wyhad1-1", "adInfo == null");
        }
        }
        int distance;
        if(Constants.book_shelf_state !=3){
            distance = booksOnLine.size() / Constants.dy_shelf_ad_freq;//计算广告展现频率  dy_shelf_ad_freq 为间隔
        }else{
            //状态为3的时候
            distance = (booksOnLine.size()-1) / Constants.dy_shelf_ad_freq;//计算广告展现频率  dy_shelf_ad_freq 为间隔
        }

        int currentPostion = 1;//当状态为3的时候 从1 开始计算
        for (int i = 0; i < distance; i++) {
            YQNativeAdInfo info;
            if (adInfoHashMap.containsKey(i + 1) && adInfoHashMap.get(i + 1) != null && adInfoHashMap.get(i + 1).getAdvertisement() != null
                    && (System.currentTimeMillis() - adInfoHashMap.get(i + 1).getAvailableTime() < 3000 || !adInfoHashMap.get(i + 1).getAdvertisement().isShowed)) {
                info = adInfoHashMap.get(i + 1);
            } else {
                info = ownNativeAdManager.getSingleADInfoNew(i + 1, NativeInit.CustomPositionName.SHELF_POSITION);
                if (info != null) {
                    info.setAvailableTime(System.currentTimeMillis());
                    adInfoHashMap.put(i + 1, info);
                }
            }

            if (info != null) {
                Book book1 = new Book();
                book1.book_type = -2;
                book1.info = info;
                AppLog.e("wyhad1-1", "info：" + info.getAdvertisement().toString());
                book1.rating = Tools.getIntRandom();
                try {
                    iBookList.add(Constants.dy_shelf_ad_freq + currentPostion, book1);
                    currentPostion = currentPostion+Constants.dy_shelf_ad_freq +1;
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                    break;
                }
            } else {
                AppLog.e("wyhad1-1", "info == null");
            }
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
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

        if (BuildConfig.DEBUG) {
            BookApplication.getRefWatcher().watch(this);
        }

        if (frameBookHelper != null) {
            frameBookHelper.recycleCallback();
        }

        if (bookOnLines != null) {
            bookOnLines.clear();
        }
        if (adInfoHashMap != null) {
            adInfoHashMap.clear();
        }
    }

    /**
     * 根据网络及书架数据设置下拉刷新模式为直接完成或显式下拉
     */
    protected boolean isPullAction(ArrayList<Book> rackBookList, SuperSwipeRefreshLayout refreshLayout) {
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
    private void updateUI() {

        getBookListData();

        if (bookShelfReAdapter == null) {
            bookShelfReAdapter = new BookShelfReAdapter(getActivity(), iBookList, this, this, isList);
        }
        if (bookShelfReAdapter != null) {
            for (int i = 0; i < bookOnLines.size(); i++) {
                Book book = bookOnLines.get(i);
                setUpdateState(book);
            }

            bookShelfReAdapter.setUpdate_table(update_table);
            bookShelfReAdapter.setBookDownLoad(down_table);
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
                bookIdList.append(book.book_id);

                bookIdList.append((book.readed == 1) ? "_1" : "_0");//1已读，0未读
                bookIdList.append((i == iBookList.size()) ? "" : "$");

            }
            Map<String, String> data = new HashMap<>();
            data.put("bookid", bookIdList.toString());
            StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.BOOKLIST, data);
            sharedPreferences.edit().putLong(Constants.TODAY_FIRST_POST_BOOKIDS, currentTime).apply();
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
        if (down_table == null) {
            down_table = new ArrayList<>();
        }
        down_table.clear();

        bookCollect_checked = new ArrayList<>();
    }

    private void initListener() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnPullRefreshListener(new SuperSwipeRefreshLayout.OnPullRefreshListener() {

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

                        StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.SHELF_PAGE, StartLogClickUtil.TOBOOKCITY);
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
            bookShelfReAdapter = new BookShelfReAdapter(getActivity(), iBookList, this, this, isList);
        }

        swipeRefreshLayout = (SuperSwipeRefreshLayout) bookshelf_content.findViewById(R.id.bookshelf_refresh_view);
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
                recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.BOTH_SET, 2, mContext.getResources().getColor(typeColor)));
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
        if (bookOnLines == null)
            bookOnLines = new ArrayList<>();

        if (bookOnLines != null) {
            bookOnLines.clear();
            bookOnLines.addAll(booksOnLine);

            setBookListHeadData(bookOnLines.size());
        }
        if (iBookList != null) {
            iBookList.clear();
            if (!booksOnLine.isEmpty()) {
                Collections.sort(booksOnLine, new FrameBookHelper.MultiComparator());
                iBookList.addAll(booksOnLine);
                if(Constants.dy_shelf_ad_switch){//判断总开关是否开启
                    if(Constants.book_shelf_state!=0){
                        toShowAdList(booksOnLine);
                    }
                }else{
                    if(Constants.book_shelf_state !=0){//判断类型开关是否开启
                        toShowAdList(booksOnLine);
                    }
                }
            }
        }
        return iBookList;
    }

   public void toShowAdList(ArrayList<Book> booksOnLine){
       if (!Constants.isHideAD && ownNativeAdManager != null) {
                    if (Constants.book_shelf_state==1) {//headerview open
                        setHeaderAdBook(headerReleative);//设置书架header 位置广告
                    }else if(Constants.book_shelf_state==2){//只显示九宫格
                        setAdBook(booksOnLine);
                        headerReleative.setVisibility(View.GONE);//隐藏headerview
                    } else if(Constants.book_shelf_state==3){
               setAdBook(booksOnLine);
                        setHeaderAdBook(headerReleative);//设置书架header 位置广告

                    }
                }
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

        //下拉刷新时删除标记的360广告信息
        if (adInfoHashMap != null) {
            Iterator<Map.Entry<Integer, YQNativeAdInfo>> iterator = adInfoHashMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, YQNativeAdInfo> entry = iterator.next();
                Integer key = entry.getKey();
                YQNativeAdInfo value = entry.getValue();
                if (value != null && value.getAdvertisement() != null && value.getAdvertisement().platformId == com.dingyueads.sdk.Constants.AD_TYPE_360 && value.getAdvertisement().isClicked) {
                    iterator.remove();
                }
            }
        }

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
                updateService.checkUpdate(BookHelper.getBookUpdateTaskData(list, this));
            }
        }
    }

    private void showToastDelay(final int textId) {
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                Activity activity = weakReference.get();
                if (isAdded() && activity != null) {
                    if (activity instanceof HomeActivity) {
                        HomeActivity homeActivity = (HomeActivity) activity;
                        homeActivity.showToastShort(textId);
                    }
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
                    if (activity instanceof HomeActivity) {
                        HomeActivity homeActivity = (HomeActivity) activity;
                        homeActivity.showToastLong(text);
                    }
                }
            }
        }, 2000);
    }

    private void showToast(final int text) {
        Activity activity = weakReference.get();
        if (isAdded() && activity != null) {
            if (activity instanceof HomeActivity) {
                HomeActivity homeActivity = (HomeActivity) activity;
                homeActivity.showToastShort(text);
            }
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
            StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.SHELF_PAGE, StartLogClickUtil.BOOKCLICK, data);
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

        if (Constants.isShielding && !noBookSensitive && bookSensitiveWords.contains(String.valueOf(book.book_id))) {
            ToastUtils.showToastNoRepeat("抱歉，该小说已下架！");
        } else {
            BookHelper.goToCoverOrRead(weakReference.get().getApplicationContext(), weakReference.get(), book,0);
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
        }
        bookDaoHelper.updateBook(book);
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
        if (!isUpdateFinish)
            isUpdateFinish = true;
    }

    @Override
    public void onException(Exception e) {
        load_data_finish_time = System.currentTimeMillis();
        showToastDelay(R.string.bookshelf_refresh_network_problem);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.onRefreshComplete();
        }
        if (!isUpdateFinish)
            isUpdateFinish = true;
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
                showToastDelay("《" + book_name + getSelfString(mContext, R.string.bookshelf_one_book_update) + bookUpdate.last_chapter_name);
            } else {
                int update_size = hasUpdateList.size();
                showToastDelay("《" + book_name + getSelfString(mContext, R.string.bookshelf_more_book_update) + update_size + getSelfString(mContext,
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
            updateService.setBookUpdateListener((CheckNovelUpdateService.OnBookUpdateListener) activity);
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
    private void deleteBooks(final ArrayList<Book> deleteBooks, ArrayList<Book> rankList) {

        final int size = deleteBooks.size();
        new Thread(new Runnable() {

            @Override
            public void run() {
                String[] books = new String[size];
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < size; i++) {
                    Book book = deleteBooks.get(i);
                    books[i] = book.book_id;

                    sb.append(book.book_id);
                    sb.append((book.readed == 1) ? "_1" : "_0");
                    sb.append((i == size - 1) ? "" : "$");

                    handler.obtainMessage(REFRESH_DATA_AFTER_DELETE, book.book_id).sendToTarget();
                }
                // 删除书架数据库和章节数据库
                if (bookDaoHelper != null) {
                    bookDaoHelper.deleteBook(books);
                }

                Map<String, String> data1 = new HashMap<>();
                data1.put("type", "1");
                data1.put("number", String.valueOf(size));
                data1.put("bookids", sb.toString());
                StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.SHELFEDIT_PAGE, StartLogClickUtil.DELETE1, data1);
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
            deleteDialog = new MyDialog(activity, R.layout.publish_hint_dialog);
            TextView base_dialog_title = (TextView) deleteDialog.findViewById(R.id.dialog_title);
            base_dialog_title.setText(R.string.prompt);
            TextView base_dialog_content = (TextView) deleteDialog.findViewById(R.id.publish_content);
            base_dialog_content.setGravity(Gravity.CENTER);
            base_dialog_content.setText(R.string.determine_delete_book_cache);
            Button base_dialog_confirm = (Button) deleteDialog.findViewById(R.id.publish_stay);
            base_dialog_confirm.setText(R.string.cancel);
            Button base_dialog_abrogate = (Button) deleteDialog.findViewById(R.id.publish_leave);
            base_dialog_abrogate.setText(R.string.confirm);
            base_dialog_abrogate.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteDialog.dismiss();
                    deleteBooks(deleteBooks, bookOnLines);
                    StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.bs_click_delete_ok_btn);
                }
            });
            base_dialog_confirm.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteDialog.dismiss();
                    Map<String, String> data = new HashMap<>();
                    data.put("type", "2");
                    StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.SHELFEDIT, StartLogClickUtil.DELETE1, data);
                    StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.bs_click_delete_cancel_btn);
                }
            });
            deleteDialog.show();

        } else {
//            showToast(R.string.mian_delete_cache_no_choose);
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
        if (fragmentCallback != null) {
            fragmentCallback.getMenuShownState(state);
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
        Iterator<LinkedHashMap.Entry<String, String>> iterator = source.source.entrySet().iterator();
        ArrayList<String> list = new ArrayList<>();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String value = entry.getValue();
            list.add(value);
        }
        if (list.size() > 0) {
            iBook.parameter = list.get(0);
        }
        if (list.size() > 1) {
            iBook.extra_parameter = list.get(1);
        }
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

    //添加header AD 2017-11-03
    private void setHeaderAdBook(View view) {
        //长按删除状态下不请求广告
//        if (!isShowAD || bookShelfRemoveHelper.isRemoveMode()) {
//            return;
//        }
        AppLog.e("wyhad1-1", this.isResumed() + "");
        if (!this.isResumed()) {
            return;
        }
        YQNativeAdInfo adInfo;
        if (adInfoHashMap.containsKey(0) && adInfoHashMap.get(0) != null && adInfoHashMap.get(0).getAdvertisement() != null
                && (System.currentTimeMillis() - adInfoHashMap.get(0).getAvailableTime() < 3000 || !adInfoHashMap.get(0).getAdvertisement().isShowed)) {
            adInfo = adInfoHashMap.get(0);
        } else {
            adInfo = ownNativeAdManager.getSingleADInfoNew(0, NativeInit.CustomPositionName.SHELF_POSITION);
            if (adInfo != null) {
                adInfo.setAvailableTime(System.currentTimeMillis());
                adInfoHashMap.put(0, adInfo);
            }
        }
        if(adInfo==null)return;//没有请求到广告直接return
        checkAdeffective(view, adInfo);//判断当前广告是否有效
        headerReleative.setVisibility(View.VISIBLE);//显示headerview
    }

    //检验广告信息
    private void checkAdeffective(final View view, YQNativeAdInfo yqNativeAdInfo) {
        if (yqNativeAdInfo == null) {
            return;
        }
        Advertisement advertisement = yqNativeAdInfo.getAdvertisement();
        if (advertisement == null) {
            return;
        }

        final RelativeLayout ad_image_rl = (RelativeLayout) view.findViewById(R.id.item_ad_image_rl);//view 大小
        final ImageView ad_image = (ImageView) view.findViewById(R.id.item_ad_image);// 正常显示图片的位置
        if (advertisement.platformId == com.dingyueads.sdk.Constants.AD_TYPE_INMOBI && yqNativeAdInfo.getInMobiNative() != null) {
            View inMobiView = yqNativeAdInfo.getInMobiNative().getPrimaryViewOfWidth(view, null, ad_image_rl.getMeasuredWidth());
            if (inMobiView != null) {
                ad_image_rl.removeAllViews();
//                LogUtils.e("BookShelfReAdapter", "inmobiNative hash:" + nativeAdInfo.getInMobiNative().hashCode() + "1-1 inmobi hash:" + inMobiView.hashCode());
                ad_image_rl.addView(inMobiView);
                ad_image_rl.setVisibility(View.VISIBLE);
                ad_image.setVisibility(View.GONE);
            } else {
                view.setVisibility(View.GONE);
            }
            showHeaderView(view, yqNativeAdInfo, advertisement);
        } else if (!TextUtils.isEmpty(advertisement.iconUrl) || (com.dingyueads.sdk.Constants.AD_TYPE_KDXF == advertisement.platformId && !TextUtils.isEmpty(advertisement.imageUrl))) {
            String url = advertisement.iconUrl == null ? advertisement.imageUrl : advertisement.iconUrl;
            ImageCacheManager.getInstance().getImageLoader().get(url, new
                    ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            if (imageContainer != null) {
                                Bitmap bitmap = imageContainer.getBitmap();
                                if (bitmap != null) {
//                                    Bitmap roundedCornerBitmap = ImageUtils.getRoundedCornerBitmap
//                                            (bitmap, 40);
                                    if (bitmap != null && ad_image != null) {
                                        ad_image_rl.setVisibility(View.INVISIBLE);
                                        ad_image.setImageBitmap(bitmap);
                                        ad_image.setVisibility(View.VISIBLE);
                                    } else {
                                        view.setVisibility(View.GONE);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            view.setVisibility(View.GONE);
                        }
                    });
            showHeaderView(view, yqNativeAdInfo, advertisement);
        }

    }

    private void showHeaderView(final View view, final YQNativeAdInfo yqNativeAdInfo, Advertisement advertisement) {

        TextView header_title = (TextView) view.findViewById(R.id.item_ad_title);//标题
        TextView header_desc = (TextView) view.findViewById(R.id.item_ad_desc);//广告描述
        ImageView header_rightdown = (ImageView) view.findViewById(R.id.item_ad_right_down);//广告角标

        if (header_title != null) {
            header_title.setText(TextUtils.isEmpty(advertisement.title) ? "" : advertisement.title);
        }
        if (header_desc != null) {
            header_desc.setText(TextUtils.isEmpty(advertisement.description) ? "" : advertisement.description);
        }
        if (header_rightdown != null) {
            if ("广点通".equals(advertisement.rationName)) {
                header_rightdown.setImageResource(R.drawable.zhuishu_ad);
            } else if ("百度".equals(advertisement.rationName)) {
                header_rightdown.setImageResource(R.drawable.icon_ad_bd);
            } else if ("360".equals(advertisement.rationName)) {
                header_rightdown.setImageResource(R.drawable.icon_ad_360);
            } else {
                header_rightdown.setImageResource(R.drawable.icon_ad_default);
            }
        }
        try {
            if (statisticManager == null) {
                statisticManager = StatisticManager.getStatisticManager();
            }
            AdSceneData adSceneData = yqNativeAdInfo.getAdSceneData();
            if (adSceneData != null) {
                adSceneData.ad_showSuccessTime = String.valueOf(System.currentTimeMillis() / 1000L);
            }
            statisticManager.schedulingRequest((Activity) mContext, view, yqNativeAdInfo, null, StatisticManager.TYPE_SHOW, NativeInit
                    .ad_position[0]);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (statisticManager == null) {
                        statisticManager = StatisticManager.getStatisticManager();
                    }
                    statisticManager.schedulingRequest((Activity) mContext, view, yqNativeAdInfo, null, StatisticManager.TYPE_CLICK, NativeInit.ad_position[0]);
                    if (yqNativeAdInfo != null && com.dingyueads.sdk.Constants.AD_TYPE_360 == yqNativeAdInfo.getAdvertisement().platformId) {
                        EventBookshelfAd eventBookshelfAd = new EventBookshelfAd("bookshelfclick_360", 0 / Constants.dy_shelf_ad_freq, yqNativeAdInfo);
                        EventBus.getDefault().post(eventBookshelfAd);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
                StatServiceUtils.statBookEventClick(mContext, StatServiceUtils.type_ad_shelf);
                if (Constants.DEVELOPER_MODE) {
                    Toast.makeText(mContext, "你点击了广告", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
