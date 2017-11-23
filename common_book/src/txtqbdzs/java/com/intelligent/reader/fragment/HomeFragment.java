package com.intelligent.reader.fragment;

import com.baidu.mobstat.StatService;
import com.intelligent.reader.BuildConfig;
import com.intelligent.reader.R;
import com.intelligent.reader.activity.HomeActivity;
import com.intelligent.reader.activity.SearchBookActivity;
import com.intelligent.reader.activity.SettingActivity;
import com.intelligent.reader.activity.SplashActivity;
import com.intelligent.reader.app.BookApplication;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.view.ConsumeEvent;
import net.lzbook.kit.book.view.NonSwipeViewPager;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.encrypt.URLBuilderIntterface;
import net.lzbook.kit.request.UrlUtils;
import net.lzbook.kit.utils.AnimationHelper;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.FrameBookHelper;
import net.lzbook.kit.utils.SharedPreferencesUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.HashMap;

import de.greenrobot.event.EventBus;

/**
 * 主页面
 */
public class HomeFragment extends BaseFragment implements OnPageChangeListener, FrameBookHelper.SearchUpdateBook, OnClickListener {
    private final static int TOP_TWO_TABS = 1;
    private final static int BOTTOM_FOUR_TABS = 2;
    private final static int TOP_FOUR_TABS = 3;
    private final static int TOP_TWO_FRAME = 4;
    private static String TAG = HomeFragment.class.getSimpleName();
    private final MHandler handler = new MHandler(this);
    public NonSwipeViewPager viewPager;
    //头部四个TAB的布局
    private RelativeLayout content_head_four_tabs;
    private ImageView content_head_user;
    private ImageView content_download_manage_four_tabs;
    private RelativeLayout content_tab_bookshelf_four_tabs;
    private RelativeLayout content_tab_recommend_four_tabs;
    private RelativeLayout content_tab_ranking_four_tabs;
    private RelativeLayout content_tab_category_four_tabs;
    private RelativeLayout bookshelf_search_view;
    private BookStoreFragment bookStoreFragment;
    private int STYLE_CASE = 0;
    private ImageView content_head_setting;
    private TextView content_title;
    private ImageView content_head_search, content_download_manage;
    private BookShelfFragment bookShelfFragment;
    private WebViewFragment recommendFragment;
    private WebViewFragment rankingFragment;
    private WebViewFragment categoryFragment;
    private FragmentManager fragmentManager;
    private MainAdapter adapter;
    private FrameBookHelper frameHelper;
    private LinearLayout content_tab_selection;
    private RelativeLayout content_head_editor;
    private RelativeLayout content_head;
    private View content_tab_bookshelf;
    private View content_tab_recommend;
    private View content_tab_ranking;
    private View content_tab_category;
    private Context mContext;
    private int current_tab = 0;
    private int versionCode;
    private SharedPreferences sharedPreferences;

    private String[] titles = {"书架", "推荐", "榜单", "分类"};
    private View content_tab_selection_divider;
    private Boolean b = true;
    private ImageView home_edit_back;
    private TextView home_edit_cancel;
    private int bottomType;//青果打点搜索 2 推荐  3 榜单


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        versionCode = AppUtils.getVersionCode();
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        initData();
    }

    @Override
    protected View getFrameView(LayoutInflater inflater) {
        try {
            mFrameView = inflater.inflate(R.layout.content_view, null);
            AppLog.e(TAG, "-->>HomeFragment");
        } catch (InflateException e) {
            e.printStackTrace();

            //need restart app

            getActivity().finish();
            Intent intent = new Intent(getActivity(), SplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return new FrameLayout(getActivity());
        }
        if (mFrameView != null) {
            //初始化viewPager相关
            viewPager = (NonSwipeViewPager) mFrameView.findViewById(R.id.content_view);
            if (frameCallback != null) {
                frameCallback.getViewPager(viewPager);
            }
            viewPager.setOnPageChangeListener(new OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    switch (STYLE_CASE) {
                        case BOTTOM_FOUR_TABS:
                            bottomType = 1;
                            changeStatus(position);
                            break;
                        case TOP_TWO_TABS:
                            bottomType = 2;
                            changeStatusTopTwo(position);
                            break;
                        case TOP_FOUR_TABS:
                            bottomType = 3;
                            changeStatusTopFour(position);
                            break;
                        case TOP_TWO_FRAME:
                            bottomType = 4;
                            changeStatusTopTwoFrame(position);
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });

            //长按编辑栏布局
            content_head_editor = (RelativeLayout) mFrameView.findViewById(R.id.content_head_editor);
            home_edit_back = (ImageView) mFrameView.findViewById(R.id.home_edit_back);
            home_edit_cancel = (TextView) mFrameView.findViewById(R.id.home_edit_cancel);


            //底部4个tab的书架
            content_head = (RelativeLayout) mFrameView.findViewById(R.id.content_head);
            if (content_head.getVisibility() == View.VISIBLE) {
                content_head_setting = (ImageView) mFrameView.findViewById(R.id.content_head_setting);
                mFrameView.findViewById(R.id.content_head).setOnClickListener(this);
                content_head_search = (ImageView) mFrameView.findViewById(R.id.content_head_search);
                content_head_setting = (ImageView) mFrameView.findViewById(R.id.content_head_setting);
                content_head_setting = (ImageView) mFrameView.findViewById(R.id.content_head_setting);
                mFrameView.findViewById(R.id.content_head).setOnClickListener(this);
                content_head_search = (ImageView) mFrameView.findViewById(R.id.content_head_search);
//            content_community = (ImageView) mFrameView.findViewById(R.id.content_community);
                content_download_manage = (ImageView) mFrameView.findViewById(R.id.content_download_manage);
                content_title = (TextView) mFrameView.findViewById(R.id.content_title);
                content_tab_selection = (LinearLayout) mFrameView.findViewById(R.id.content_tab_selection);
                content_tab_selection_divider = mFrameView.findViewById(R.id.content_tab_selection_devider);
                content_tab_bookshelf = mFrameView.findViewById(R.id.content_tab_bookshelf);
                content_tab_recommend = mFrameView.findViewById(R.id.content_tab_recommend);
                content_tab_ranking = mFrameView.findViewById(R.id.content_tab_ranking);
                content_tab_category = mFrameView.findViewById(R.id.content_tab_category);
                viewPager.setOffscreenPageLimit(3);
                STYLE_CASE = BOTTOM_FOUR_TABS;
                viewPager.setScrollable(false);
            }


            //头部四个tab类型的书架
            content_head_four_tabs = (RelativeLayout) mFrameView.findViewById(R.id.content_head_four_tabs);
            if (content_head_four_tabs.getVisibility() == View.VISIBLE) {
                content_head_user = (ImageView) mFrameView.findViewById(R.id.content_head_user);
                content_download_manage_four_tabs = (ImageView) mFrameView.findViewById(R.id.content_download_manage_four_tabs);
                //四个tab
                content_tab_bookshelf_four_tabs = (RelativeLayout) mFrameView.findViewById(R.id.content_tab_bookshelf_four_tabs);
                content_tab_recommend_four_tabs = (RelativeLayout) mFrameView.findViewById(R.id.content_tab_recommend_four_tabs);
                content_tab_ranking_four_tabs = (RelativeLayout) mFrameView.findViewById(R.id.content_tab_ranking_four_tabs);
                content_tab_category_four_tabs = (RelativeLayout) mFrameView.findViewById(R.id.content_tab_category_four_tabs);
                //搜索栏
                bookshelf_search_view = (RelativeLayout) mFrameView.findViewById(R.id.bookshelf_search_view);
                viewPager.setOffscreenPageLimit(3);
                STYLE_CASE = TOP_FOUR_TABS;
                viewPager.setScrollable(true);
            }


            adapter = new MainAdapter(fragmentManager);
            viewPager.setAdapter(adapter);
            initGuide(mFrameView);
        }
        viewPager.setCurrentItem(current_tab);
        return mFrameView;
    }

    //头部2TAB的方式
    private void switchState(boolean isBookShelf) {

        if (!isBookShelf) {
            removeBookShelfMenu();
        }
    }

    //头部2TAB的方式（带边框）
    private void switchStateWithFrame(boolean isBookShelf) {

        if (!isBookShelf) {
            removeBookShelfMenu();
        }
    }

    //底部4TAB的方式
    private void changeStatus(int position) {
        current_tab = position;

        if (current_tab != 0) {
            removeBookShelfMenu();
        }

        if (content_title != null) {
            content_title.setText(titles[position]);
        }

        if (content_tab_bookshelf != null) {
            content_tab_bookshelf.setSelected(position == 0);
        }

        if (content_tab_recommend != null) {
            content_tab_recommend.setSelected(position == 1);
        }

        if (content_tab_ranking != null) {
            content_tab_ranking.setSelected(position == 2);
        }

        if (content_tab_category != null) {
            content_tab_category.setSelected(position == 3);
        }

    }

    //头部4tab的切换状态
    private void changeStatusTopFour(int position) {
        current_tab = position;

        if (current_tab != 0) {
            removeBookShelfMenu();
        }

        //头部四个tab
        if (content_tab_bookshelf_four_tabs != null) {
            content_tab_bookshelf_four_tabs.setSelected(position == 0);
        }

        if (content_tab_recommend_four_tabs != null) {
            content_tab_recommend_four_tabs.setSelected(position == 1);
        }

        if (content_tab_ranking_four_tabs != null) {
            content_tab_ranking_four_tabs.setSelected(position == 2);
        }

        if (content_tab_category_four_tabs != null) {
            content_tab_category_four_tabs.setSelected(position == 3);
        }
    }


    //头部两个top
    private void changeStatusTopTwo(int position) {
        current_tab = position;
        switch (position) {
            case 0:
                switchState(true);
                break;
            case 1:
                switchState(false);
                break;
            default:
                switchState(true);
                break;
        }
    }

    //头部两个top（带边框）
    private void changeStatusTopTwoFrame(int position) {
        current_tab = position;
        switch (position) {
            case 0:
                switchStateWithFrame(true);
                break;
            case 1:
                switchStateWithFrame(false);
                break;
            default:
                switchStateWithFrame(true);
                break;
        }
    }


    @Override
    public void searchUpdateBook() {

    }

    protected Fragment initView(int position) {
        Fragment frame = null;
        switch (position) {
            case 0:
                if (bookShelfFragment == null) {
                    bookShelfFragment = new BookShelfFragment();
                }
                frame = bookShelfFragment;
                break;
            case 1:
                if (bookStoreFragment == null) {
                    if (STYLE_CASE == TOP_TWO_TABS || STYLE_CASE == TOP_TWO_FRAME) {
                        bookStoreFragment = BookStoreFragment.newInstance();
                        frame = bookStoreFragment;
                    } else {
                        recommendFragment = new WebViewFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("type", "recommend");
                        String uri = URLBuilderIntterface.WEB_RECOMMEND.replace("{packageName}", AppUtils.getPackageName());
                        bundle.putString("url", UrlUtils.buildWebUrl(uri, new HashMap<String, String>()));
                        recommendFragment.setArguments(bundle);
                        frame = recommendFragment;
                    }
                }
                break;
            case 2:
                if (rankingFragment == null) {
                    rankingFragment = new WebViewFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("type", "rank");
                    String uri = URLBuilderIntterface.WEB_RANK.replace("{packageName}", AppUtils.getPackageName());
                    bundle.putString("url", UrlUtils.buildWebUrl(uri, new HashMap<String, String>()));
                    rankingFragment.setArguments(bundle);
                }
                frame = rankingFragment;
                break;
            case 3:
                if (categoryFragment == null) {
                    categoryFragment = new WebViewFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("type", "category");
                    String uri = URLBuilderIntterface.WEB_CATEGORY.replace("{packageName}", AppUtils.getPackageName());
                    bundle.putString("url", UrlUtils.buildWebUrl(uri, new HashMap<String, String>()));
                    categoryFragment.setArguments(bundle);
                }
                frame = categoryFragment;
        }
        return frame;
    }

    public void setTabSelected(int tabSelected) {
        if (current_tab == tabSelected) {
            return;
        }
        AppLog.e(TAG, "tabSelected: " + tabSelected);
        if (viewPager != null) {
            viewPager.setCurrentItem(tabSelected);
        }
        current_tab = tabSelected;
        changeView(current_tab);
        if (current_tab != 0) {
            removeBookShelfMenu();
        }
    }

    private void changeView(int type) {
        if (content_tab_bookshelf != null) {
            content_tab_bookshelf.setSelected(type == 0);
        }
        if (content_tab_recommend != null) {
            content_tab_recommend.setSelected(type == 1);
        }

        if (content_tab_ranking != null) {
            content_tab_ranking.setSelected(type == 2);
        }

        if (content_tab_category != null) {
            content_tab_category.setSelected(type == 3);
        }

        if (content_tab_bookshelf_four_tabs != null) {
            content_tab_bookshelf_four_tabs.setSelected(type == 0);
        }
        if (content_tab_recommend_four_tabs != null) {
            content_tab_recommend_four_tabs.setSelected(type == 1);
        }

        if (content_tab_ranking_four_tabs != null) {
            content_tab_ranking_four_tabs.setSelected(type == 2);
        }

        if (content_tab_category_four_tabs != null) {
            content_tab_category_four_tabs.setSelected(type == 3);
        }


        current_tab = type;
    }

    private void removeBookShelfMenu() {
        if (bookShelfFragment != null && bookShelfFragment.bookShelfRemoveHelper != null) {
            bookShelfFragment.bookShelfRemoveHelper.dismissRemoveMenu();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.content_head:

                break;
            case R.id.content_head_user:
            case R.id.book_button_left:
            case R.id.content_head_setting:
                StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.PERSONAL);
//                EventBus.getDefault().post(new ConsumeEvent(R.id.redpoint_home_setting));
                startActivity(new Intent(context, SettingActivity.class));
                net.lzbook.kit.utils.StatServiceUtils.statAppBtnClick(mContext, net.lzbook.kit.utils.StatServiceUtils.bs_click_mine_menu);
                break;
            case R.id.book_button_right:
            case R.id.bookshelf_search_view:
            case R.id.content_head_search:
                Intent searchInter = new Intent();
                searchInter.setClass(context, SearchBookActivity.class);
                AppLog.e(TAG, "SearchBookActivity -----> Start");
                startActivity(searchInter);
                if (bottomType == 2) {
                    StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.RECOMMEND_PAGE, StartLogClickUtil.QG_TJY_SEARCH);
                } else if (bottomType == 3) {
                    StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.TOP_PAGE, StartLogClickUtil.QG_BDY_SEARCH);
                }else if(bottomType == 4){
                    StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.CLASS_PAGE, StartLogClickUtil.QG_FL_SEARCH);
                }else{
                    StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.SEARCH);
                }

                net.lzbook.kit.utils.StatServiceUtils.statAppBtnClick(mContext, net.lzbook.kit.utils.StatServiceUtils.bs_click_search_btn);
                break;
            case R.id.content_download_manage_four_tabs:
            case R.id.content_download_manage:
                Intent downloadIntent = new Intent();
                downloadIntent.setClass(context, DownloadManagerActivity.class);
                startActivity(downloadIntent);
                net.lzbook.kit.utils.StatServiceUtils.statAppBtnClick(mContext, net.lzbook.kit.utils.StatServiceUtils.bs_click_download_btn);
                StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.CACHEMANAGE);
                break;
            case R.id.radiobutton_bookshelf:
            case R.id.content_tab_bookshelf_four_tabs:
            case R.id.content_tab_bookshelf:
                AppLog.e(TAG, "BookShelf Selected");
                setTabSelected(0);
                if (STYLE_CASE == BOTTOM_FOUR_TABS) {
                    content_title.setText("书架");
                }
                bottomType = 1;
                StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.BOOKSHELF);
                break;
            case R.id.radiobutton_bookstore:
            case R.id.content_tab_recommend_four_tabs:
            case R.id.content_tab_recommend:
                AppLog.e(TAG, "Selection Selected");
                setTabSelected(1);
                if (STYLE_CASE == BOTTOM_FOUR_TABS) {
                    content_title.setText("推荐");
                    //双击回到顶部
                    if (AppUtils.isDoubleClick(System.currentTimeMillis())) {
                        if (viewPager.getCurrentItem() == 1) {
                            recommendFragment.loadWebData(recommendFragment.url);
                        }
                    }
                }
                bottomType = 2;
                net.lzbook.kit.utils.StatServiceUtils.statAppBtnClick(mContext, net.lzbook.kit.utils.StatServiceUtils.bs_click_recommend_menu);
                StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.RECOMMEND);
                break;
            case R.id.content_tab_ranking_four_tabs:
            case R.id.content_tab_ranking:
                AppLog.e(TAG, "Ranking Selected");
                setTabSelected(2);
                if (STYLE_CASE == BOTTOM_FOUR_TABS) {
                    content_title.setText("榜单");
                }
                bottomType = 3;
                net.lzbook.kit.utils.StatServiceUtils.statAppBtnClick(mContext, net.lzbook.kit.utils.StatServiceUtils.bs_click_rank_menu);
                StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.TOP);
                break;

            case R.id.content_tab_category_four_tabs:
            case R.id.content_tab_category:
                AppLog.e(TAG, "Classify Selected");
                setTabSelected(3);
                if (STYLE_CASE == BOTTOM_FOUR_TABS) {
                    content_title.setText("分类");
                }
                bottomType = 4;
                net.lzbook.kit.utils.StatServiceUtils.statAppBtnClick(mContext, net.lzbook.kit.utils.StatServiceUtils.bs_click_category_menu);
                StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.CLASS);
                break;

            case R.id.home_edit_back:
            case R.id.home_edit_cancel:
                removeBookShelfMenu();
                StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.SHELFEDIT_PAGE, StartLogClickUtil.CANCLE1);
                break;
            default:
                setTabSelected(0);
                break;
        }
    }

    @Override
    protected void setOnActivityCreate() {
        initListener();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        if (frameCallback != null) {
            frameCallback.frameHelper();
        }
        if (frameHelper == null && actReference != null && actReference.get() != null) {
            frameHelper = ((HomeActivity) actReference.get()).frameHelper;
        }
        if (frameHelper != null) {
            frameHelper.setSearchUpdateUI(this);
            frameHelper.registSearchUpdateReceiver();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        AppLog.e(TAG, "onResume");
        if (bookShelfFragment != null && bookShelfFragment.bookShelfReAdapter != null) {
            bookShelfFragment.bookShelfReAdapter.notifyDataSetChanged();
        }
        changeView(current_tab);
        StatService.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (viewPager != null) {
            AppLog.e(TAG, "onPause currentItem:" + viewPager.getCurrentItem());
        }
        StatService.onPause(this);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        AppLog.e(TAG, "onDetach");
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
    public void onDestroyView() {
        if (handler != null) {
            handler.removeMessages(0);
        }
        super.onDestroyView();
        if (BuildConfig.DEBUG) {
            BookApplication.getRefWatcher().watch(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    /**
     * <FragmentUI创建完成后初始化数据>
     * void
     */
    private void initData() {
        Activity activity = actReference.get();
        if (activity == null) {
            return;
        }

        try {
            fragmentManager = this.getChildFragmentManager();
        } catch (NoSuchMethodError e) {
            e.printStackTrace();
        }
    }

    protected void initListener() {

        if (content_head_setting != null) {
            content_head_setting.setOnClickListener(this);
        }

        if (content_head_search != null) {
            content_head_search.setOnClickListener(this);
        }

        if (content_download_manage != null) {
            content_download_manage.setOnClickListener(this);
        }

        if (content_tab_bookshelf != null) {
            content_tab_bookshelf.setOnClickListener(this);
        }
        if (content_tab_recommend != null) {
            content_tab_recommend.setOnClickListener(this);
        }

        if (content_tab_ranking != null) {
            content_tab_ranking.setOnClickListener(this);
        }

        if (content_tab_category != null) {
            content_tab_category.setOnClickListener(this);
        }

        if (home_edit_back != null) {
            home_edit_back.setOnClickListener(this);
        }

        if (home_edit_cancel != null) {
            home_edit_cancel.setOnClickListener(this);
        }

        if (bookshelf_search_view != null) {
            bookshelf_search_view.setOnClickListener(this);
        }

        //顶部4tab的点击注册
        if (content_tab_bookshelf_four_tabs != null) {
            content_tab_bookshelf_four_tabs.setOnClickListener(this);
        }
        if (content_tab_recommend_four_tabs != null) {
            content_tab_recommend_four_tabs.setOnClickListener(this);
        }
        if (content_tab_ranking_four_tabs != null) {
            content_tab_ranking_four_tabs.setOnClickListener(this);
        }
        if (content_tab_category_four_tabs != null) {
            content_tab_category_four_tabs.setOnClickListener(this);
        }
        if (content_download_manage_four_tabs != null) {
            content_download_manage_four_tabs.setOnClickListener(this);
        }
        if (content_head_user != null) {
            content_head_user.setOnClickListener(this);
        }

    }

    private void sendBroadCastWithRemainTime() {
    }

    private void sendBroadCast() {

    }

    protected void setFBData(BookShelfFragment fb) {
        if (viewPager != null && fb != null && frameHelper != null) {
            if (frameCallback != null) {
                frameCallback.getFrameBookRankView(fb);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        changeView(position);
//        if (bookShelfFragment != null && bookShelfFragment.removeAdapterHelper != null) {
//            bookShelfFragment.removeAdapterHelper.dismissRemoveMenu();
//        }
    }

    public void onMenuShownState(boolean state) {
        if (STYLE_CASE == BOTTOM_FOUR_TABS) {
            if (state) {
                content_tab_selection.setVisibility(View.GONE);
                content_tab_selection_divider.setVisibility(View.GONE);
                if (!content_head_editor.isShown()) {
                    Animation showAnimation = new AlphaAnimation(0.0f, 1.0f);
                    showAnimation.setDuration(200);
                    content_head_editor.startAnimation(showAnimation);
                    content_head_editor.setVisibility(View.VISIBLE);
                    if (bookshelf_search_view != null) {
                        bookshelf_search_view.setVisibility(View.GONE);
                    }
                }
                AnimationHelper.smoothScrollTo(viewPager, 0);
            } else {
                if (content_head_editor.isShown()) {
                    content_head_editor.setVisibility(View.GONE);
                    if (bookshelf_search_view != null) {
                        bookshelf_search_view.setVisibility(View.VISIBLE);
                    }
                }
                content_tab_selection.setVisibility(View.VISIBLE);
                content_tab_selection_divider.setVisibility(View.VISIBLE);
                AnimationHelper.smoothScrollTo(viewPager, 0);
            }
        } else {
            if (state) {
                if (!content_head_editor.isShown()) {
                    Animation showAnimation = new AlphaAnimation(0.0f, 1.0f);
                    showAnimation.setDuration(200);
                    content_head_editor.startAnimation(showAnimation);
                    content_head_editor.setVisibility(View.VISIBLE);
                    if (bookshelf_search_view != null) {
                        bookshelf_search_view.setVisibility(View.GONE);
                    }
                }
                AnimationHelper.smoothScrollTo(viewPager, 0);
            } else {
                if (content_head_editor.isShown()) {
                    content_head_editor.setVisibility(View.GONE);
                    if (bookshelf_search_view != null) {
                        bookshelf_search_view.setVisibility(View.VISIBLE);
                    }
                }
                AnimationHelper.smoothScrollTo(viewPager, 0);
            }
        }

    }

    private void initGuide(View frameBookView) {
        final SharedPreferencesUtils sharedPreferencesUtils = new SharedPreferencesUtils(PreferenceManager.getDefaultSharedPreferences(context));
        if (!sharedPreferencesUtils.getBoolean(versionCode + Constants.BOOKSHELF_GUIDE_TAG)) {
            final View ll_guide_layout = frameBookView.findViewById(R.id.ll_guide_layout);
            ll_guide_layout.setVisibility(View.VISIBLE);
            final ImageView iv_gudie_download = (ImageView) frameBookView.findViewById(R.id.iv_guide_download);
            final ImageView iv_guide_remove = (ImageView) frameBookView.findViewById(R.id.iv_guide_remove);
            iv_guide_remove.setVisibility(View.VISIBLE);
            ll_guide_layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (b) {
                        iv_gudie_download.setVisibility(View.VISIBLE);
                        iv_guide_remove.setVisibility(View.GONE);
                        b = false;
                    } else {
                        sharedPreferencesUtils.putBoolean(versionCode + Constants.BOOKSHELF_GUIDE_TAG, true);
                        iv_gudie_download.setVisibility(View.GONE);
                        ll_guide_layout.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    public int getCurrentTab() {
        return current_tab;
    }

    private static class MHandler extends Handler {
        private WeakReference<HomeFragment> reference;

        MHandler(HomeFragment content) {
            reference = new WeakReference<>(content);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HomeFragment content = reference.get();
            if (content == null) {
                return;
            }
            switch (msg.what) {
                case 0:
                    content.sendBroadCastWithRemainTime();
                    break;
                case 1:
                    content.sendBroadCast();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * ViewPager 的Adapter
     */
    protected class MainAdapter extends FragmentPagerAdapter {

        /**
         * <默认构造函数>
         */
        public MainAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return (STYLE_CASE == TOP_TWO_TABS || STYLE_CASE == TOP_TWO_FRAME ? 2 : 4);
        }

        @Override
        public Fragment getItem(int position) {
            AppLog.e(TAG, "position: " + position);
            return initView(position);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            switch (position) {
                case 0:
                    BookShelfFragment bookShelfFragment = (BookShelfFragment) super.instantiateItem(container, position);
                    bookShelfFragment.doUpdateBook();
                    setFBData(bookShelfFragment);
                    return bookShelfFragment;
                default:
                    return super.instantiateItem(container, position);
            }
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }
    }

//    public void showGuideView(){
//        GuideBuilder builder = new GuideBuilder();
//        builder.setTargetView(content_head_user).set
//    }

}
