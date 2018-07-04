package com.intelligent.reader.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.ding.basic.request.RequestService;
import com.intelligent.reader.R;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.view.NonSwipeViewPager;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.encrypt.URLBuilderIntterface;
import net.lzbook.kit.request.UrlUtils;
import net.lzbook.kit.utils.AppUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Function：书城
 *
 * Created by JoannChen on 2018/6/16 0016 10:38
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
public class BookStoreFragment extends Fragment {

    private NonSwipeViewPager bookstore_content;

    private LinearLayout bookstore_recommend;
    private LinearLayout bookstore_ranking;
    private LinearLayout bookstore_category;

    private BookStoreAdapter bookStoreAdapter;

    private WebViewFragment recommendFragment;
    private WebViewFragment rankingFragment;
    private WebViewFragment categoryFragment;

    private FragmentManager fragmentManager;

    private int current_tab;

    private View view;
    private int versionCode = 0;
    private SearchClickListener mSearchClickListener;
    private SharedPreferences sharedPreferences;

    public BookStoreFragment() {

    }

    public static BookStoreFragment newInstance() {
        return new BookStoreFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                getActivity().getApplicationContext());
        versionCode = AppUtils.getVersionCode();
        fragmentManager = getChildFragmentManager();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_bookstore, container, false);

        bookstore_content = view.findViewById(R.id.bookstore_content);

        bookstore_recommend = view.findViewById(R.id.bookstore_recommend);
        bookstore_ranking = view.findViewById(R.id.bookstore_ranking);
        bookstore_category = view.findViewById(R.id.bookstore_category);


        bookStoreAdapter = new BookStoreAdapter(fragmentManager);
        bookstore_content.setAdapter(bookStoreAdapter);
        bookstore_content.setOffscreenPageLimit(2);

        initListener();

        return view;
    }

    private void initListener() {

        if (bookstore_content != null) {
            bookstore_content.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset,
                        int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    current_tab = position;
                    switch (position) {
                        case 0:
                            switchState(position);
                            if (mSearchClickListener != null) {
                                mSearchClickListener.getCurrent(2);
                            }
                            sharedPreferences.edit().putString(Constants.FINDBOOK_SEARCH,
                                    "recommend").apply();
                            break;
                        case 1:
                            switchState(position);
                            sharedPreferences.edit().putString(Constants.FINDBOOK_SEARCH,
                                    "top").apply();
                            if (mSearchClickListener != null) {
                                mSearchClickListener.getCurrent(3);
                            }
                            break;
                        case 2:
                            switchState(position);
                            sharedPreferences.edit().putString(Constants.FINDBOOK_SEARCH,
                                    "class").apply();
                            if (mSearchClickListener != null) {
                                mSearchClickListener.getCurrent(4);
                            }
                            break;
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }

        if (bookstore_recommend != null) {
            bookstore_recommend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setTabSelected(0);
                    sharedPreferences.edit().putString(Constants.FINDBOOK_SEARCH,
                            "recommend").apply();
                    StartLogClickUtil.upLoadEventLog(getActivity(), StartLogClickUtil.MAIN_PAGE,
                            StartLogClickUtil.RECOMMEND);
                    if (mSearchClickListener != null) {
                        mSearchClickListener.getCurrent(2);
                    }
                }
            });
        }


        if (bookstore_ranking != null) {
            bookstore_ranking.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setTabSelected(1);
                    sharedPreferences.edit().putString(Constants.FINDBOOK_SEARCH, "top").apply();
                    StartLogClickUtil.upLoadEventLog(getActivity(), StartLogClickUtil.MAIN_PAGE,
                            StartLogClickUtil.TOP);
                    if (mSearchClickListener != null) {
                        mSearchClickListener.getCurrent(3);
                    }

                }
            });
        }

        if (bookstore_category != null) {
            bookstore_category.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setTabSelected(2);
                    sharedPreferences.edit().putString(Constants.FINDBOOK_SEARCH, "class").apply();
                    StartLogClickUtil.upLoadEventLog(getActivity(), StartLogClickUtil.MAIN_PAGE,
                            StartLogClickUtil.CLASS);
                    if (mSearchClickListener != null) {
                        mSearchClickListener.getCurrent(4);
                    }

                }
            });
        }
    }

    private void switchState(int type) {

        if (bookstore_recommend != null) {
            bookstore_recommend.setSelected(type == 0);
        }

        if (bookstore_ranking != null) {
            bookstore_ranking.setSelected(type == 1);
        }

        if (bookstore_category != null) {
            bookstore_category.setSelected(type == 2);
        }
    }

    public void setTabSelected(int tabSelected) {
        if (current_tab == tabSelected) {
            return;
        }

        if (bookstore_content != null) {
            bookstore_content.setCurrentItem(tabSelected);
        }
        current_tab = tabSelected;
        switchState(current_tab);
    }

    protected Fragment initView(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                if (recommendFragment == null) {
                    recommendFragment = new WebViewFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("type", "recommend");
                    String uri = RequestService.WEB_RECOMMEND_V4;
                    Map<String, String> params = new HashMap<>();
                    params.put("sex", sharedPreferences.getInt("gender", Constants.SGENDER) + "");
                    bundle.putString("url", UrlUtils.buildWebUrl(uri, params));
                    recommendFragment.setArguments(bundle);

                }

                fragment = recommendFragment;
                break;
            case 1:
                if (rankingFragment == null) {
                    rankingFragment = new WebViewFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("type", "rank");
                    String uri = RequestService.WEB_RANK_V3.replace("{packageName}",
                            AppUtils.getPackageName());
                    Map<String, String> params = new HashMap<>();
//                    params.put("sex",sharedPreferences.getInt("gender", Constants.SGENDER)+"");
                    bundle.putString("url", UrlUtils.buildWebUrl(uri, params));
                    rankingFragment.setArguments(bundle);
                }

                fragment = rankingFragment;
                break;
            case 2:
                if (categoryFragment == null) {
                    categoryFragment = new WebViewFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("type", "category");
                    String uri = RequestService.WEB_CATEGORY_V4;
                    Map<String, String> params = new HashMap<>();
                    params.put("sex", sharedPreferences.getInt("gender", Constants.SGENDER) + "");
                    bundle.putString("url", UrlUtils.buildWebUrl(uri, params));
                    categoryFragment.setArguments(bundle);
                }

                fragment = categoryFragment;
                break;
        }
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        switchState(current_tab);
    }


    //大数据 青果搜索打点用
    public interface SearchClickListener {
        void getCurrent(int position);
    }

    /**
     * ViewPager 的Adapter
     */
    protected class BookStoreAdapter extends FragmentPagerAdapter {

        BookStoreAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int position) {
            return initView(position);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return super.instantiateItem(container, position);
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
}