package com.intelligent.reader.activity;

import com.intelligent.reader.R;
import com.intelligent.reader.adapter.GuideAdapter;
import com.intelligent.reader.fragment.GuideFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;


import iyouqu.theme.FrameActivity;

/**
 * guide 页面
 */
public class GuideActivity extends FrameActivity implements GuideFragment.FragmentActivityCallback {
    ViewPager viewPager;
    GuideAdapter guideAdapter;
    boolean isFromApp = false;
    boolean isLoop = true;
    ImageView imageCenter;

    @Override
    public void onCreate(Bundle paramBundle) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(paramBundle);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.act_guide_new);
        viewPager = findViewById(R.id.vp_guide_pager);
        guideAdapter = new GuideAdapter(getSupportFragmentManager());
        guideAdapter.setPager(viewPager);

        Bundle guideBundle = new Bundle();
        guideBundle.putInt("image_center", R.drawable.splash_icons);
        GuideFragment guideFragment = new GuideFragment();
        guideFragment.setArguments(guideBundle);

        guideAdapter.add(guideFragment);

        viewPager.setAdapter(guideAdapter);

        if (getIntent() != null) {
            isFromApp = getIntent().getBooleanExtra("isFromApp", false);
        }
    }


    private void intoApp() {
        if (!isFromApp) {
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            intent.setClass(GuideActivity.this, HomeActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }

    }

    public boolean  onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isFromApp) {
                finish();
            } else {
                intoApp();
            }
        }

        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isLoop = false;
        isFromApp = false;
    }

    public void getImageController(ImageView imageController) {
//        this.imageController = imageController;
    }

    public void getGuideBtn(TextView guideBtn) {
//        guideBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isFromApp) {
//                    finish();
//                } else {
//                    intoApp();
//                }
//            }
//        });
    }

    @Override
    public void getImageCenter(ImageView imageCenter) {
        this.imageCenter = imageCenter;
        imageCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewPager.getCurrentItem() == guideAdapter.getCount() - 1) {
                    intoApp();
                }
            }
        });
    }
    @Override
    public boolean supportSlideBack() {
        return false;
    }
}
