package com.intelligent.reader.fragment;

import com.intelligent.reader.R;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.view.NonSwipeViewPager;
import net.lzbook.kit.request.UrlUtils;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.encrypt.URLBuilderIntterface;

import android.os.Bundle;
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

import java.util.HashMap;

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

    public BookStoreFragment() {

    }

    public static BookStoreFragment newInstance() {
        return new BookStoreFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        versionCode = AppUtils.getVersionCode();
        fragmentManager = getChildFragmentManager();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_bookstore, container, false);

        bookstore_content = (NonSwipeViewPager) view.findViewById(R.id.bookstore_content);

        bookstore_recommend = (LinearLayout) view.findViewById(R.id.bookstore_recommend);
        bookstore_ranking = (LinearLayout) view.findViewById(R.id.bookstore_ranking);
        bookstore_category = (LinearLayout) view.findViewById(R.id.bookstore_category);


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
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    current_tab = position;
                    switch (position) {
                        case 0:
                            switchState(position);
                            break;
                        case 1:
                            switchState(position);
                            break;
                        case 2:
                            switchState(position);
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
                    StartLogClickUtil.upLoadEventLog(getActivity(), StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.RECOMMEND);
                }
            });
        }


        if (bookstore_ranking != null) {
            bookstore_ranking.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setTabSelected(1);
                    StartLogClickUtil.upLoadEventLog(getActivity(), StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.TOP);
                }
            });
        }

        if (bookstore_category != null) {
            bookstore_category.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setTabSelected(2);
                    StartLogClickUtil.upLoadEventLog(getActivity(), StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.CLASS);
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
                    String uri = URLBuilderIntterface.WEB_RECOMMEND.replace("{packageName}", AppUtils.getPackageName());
                    bundle.putString("url", UrlUtils.buildWebUrl(uri, new HashMap<String, String>()));
                    recommendFragment.setArguments(bundle);
                }
                fragment = recommendFragment;
                break;
            case 1:
                if (rankingFragment == null) {
                    rankingFragment = new WebViewFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("type", "rank");
                    String uri = URLBuilderIntterface.WEB_RANK.replace("{packageName}", AppUtils.getPackageName());
                    bundle.putString("url", UrlUtils.buildWebUrl(uri, new HashMap<String, String>()));
                    rankingFragment.setArguments(bundle);
                }
                fragment = rankingFragment;
                break;
            case 2:
                if (categoryFragment == null) {
                    categoryFragment = new WebViewFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("type", "category");
                    String uri = URLBuilderIntterface.WEB_CATEGORY.replace("{packageName}", AppUtils.getPackageName());
                    bundle.putString("url", UrlUtils.buildWebUrl(uri, new HashMap<String, String>()));
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

    /**
     * ViewPager 的Adapter
     */
    protected class BookStoreAdapter extends FragmentPagerAdapter {

        public BookStoreAdapter(FragmentManager fragmentManager) {
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