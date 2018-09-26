package net.lzbook.kit.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import net.lzbook.kit.ui.fragment.GuideFragment;
import net.lzbook.kit.utils.logger.AppLog;

import java.util.ArrayList;

public class GuideAdapter extends FragmentStatePagerAdapter {
    String TAG = "";
    private ArrayList<GuideFragment> mFragments;
    private ViewPager mPager;

    public GuideAdapter(FragmentManager fm) {
        super(fm);
        mFragments = new ArrayList<>();
    }

    @Override
    public Fragment getItem(int i) {
        return mFragments.get(i);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    public void add(GuideFragment guideFragment) {
        guideFragment.setAdapter(this);
        mFragments.add(guideFragment);
        notifyDataSetChanged();
        mPager.setCurrentItem(getCount() - 1, true);

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        AppLog.d(TAG, "destroyItem position " + position + " object " + object);

    }

    public void remove(int i) {
        mFragments.remove(i);
        notifyDataSetChanged();
    }

    public void remove(GuideFragment guideFragment) {
        mFragments.remove(guideFragment);

        int pos = mPager.getCurrentItem();
        notifyDataSetChanged();

        mPager.setAdapter(this);
        if (pos >= this.getCount()) {
            pos = this.getCount() - 1;
        }
        mPager.setCurrentItem(pos, true);

    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public void setPager(ViewPager pager) {
        mPager = pager;
    }
}
