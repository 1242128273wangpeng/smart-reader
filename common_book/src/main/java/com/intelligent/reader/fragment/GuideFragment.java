package com.intelligent.reader.fragment;

import com.intelligent.reader.R;
import com.intelligent.reader.activity.HomeActivity;
import com.intelligent.reader.activity.LoginActivity;
import com.intelligent.reader.adapter.GuideAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

/**
 */
public class GuideFragment extends Fragment {
    GuideAdapter guideAdapter;
    FragmentActivityCallback fragmentActivityCallback;
    private ImageView mGuide_center;

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
        if (getArguments().getInt("image_center") != 0) {
            mGuide_center.setVisibility(View.VISIBLE);
            mGuide_center.setImageResource(getArguments().getInt("image_center"));
            if (fragmentActivityCallback != null)
                fragmentActivityCallback.getImageCenter(mGuide_center);
        }
        return v;
    }

    public void setAdapter(GuideAdapter catsAdapter) {
        guideAdapter = catsAdapter;
    }

    public interface FragmentActivityCallback {

        void getImageController(ImageView imageController);

        void getGuideBtn(TextView guide_btn);

        void getImageCenter(ImageView imageCenter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
