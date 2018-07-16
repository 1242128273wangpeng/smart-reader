package com.intelligent.reader.fragment;

import com.intelligent.reader.R;
import com.intelligent.reader.adapter.GuideAdapter;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 */
public class GuideFragment extends Fragment {
    GuideAdapter guideAdapter;
    FragmentActivityCallback fragmentActivityCallback;
    private ImageView mGuide_center;
    private FrameLayout fl_main;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        fragmentActivityCallback = (FragmentActivityCallback) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View v = null;
        try {
            v = inflater.inflate(R.layout.frame_guide_new, container, false);
        } catch (InflateException e) {
            e.printStackTrace();
        }
        if (v == null)
            return null;


        mGuide_center = (ImageView) v.findViewById(R.id.guide_img);
        fl_main = v.findViewById(R.id.fl_main);
        if (getArguments().getInt("image_center") != 0) {
            mGuide_center.setVisibility(View.VISIBLE);
            mGuide_center.setImageResource(getArguments().getInt("image_center"));
        }
        if (fragmentActivityCallback != null)
            fragmentActivityCallback.getImageCenter(fl_main);
        return v;
    }

    public void setAdapter(GuideAdapter catsAdapter) {
        guideAdapter = catsAdapter;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public interface FragmentActivityCallback {

        void getImageController(ImageView imageController);

        void getGuideBtn(TextView guide_btn);

        void getImageCenter(FrameLayout flMain);
    }
}
