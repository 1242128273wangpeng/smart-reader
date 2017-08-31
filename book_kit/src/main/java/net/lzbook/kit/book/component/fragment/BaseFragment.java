package net.lzbook.kit.book.component.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.lzbook.kit.utils.RemoveAdapterHelper;

import java.lang.ref.WeakReference;

public abstract class BaseFragment extends Fragment {
    protected View mFrameView;
    protected FragmentCallback frameCallback;
    protected Context context;
    protected WeakReference<Activity> actReference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setOnActivityCreate();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return getFrameView(inflater);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    protected View getFrameView(LayoutInflater inflater) {
        return mFrameView;
    }

    protected abstract void setOnActivityCreate();

    public interface FragmentCallback {

        //应该是NoeSwpieViewPager
        void getViewPager(ViewPager pager);

        void getRemoveMenuHelper(RemoveAdapterHelper helper);

        //应该是BookShelfFragment
        void getFrameBookRankView(Fragment bookView);

        void frameHelper();

        void getAllCheckedState(boolean isAllChecked);

        void getMenuShownState(boolean state);

        void setSelectTab(int index);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof FragmentCallback)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }
        frameCallback = (FragmentCallback) activity;
        context = activity.getApplicationContext();
        this.actReference = new WeakReference<>(activity);
    }
}