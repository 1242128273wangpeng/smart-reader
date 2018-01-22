package com.intelligent.reader.activity;

import com.intelligent.reader.R;
import com.intelligent.reader.adapter.GuideAdapter;
import com.intelligent.reader.fragment.GuideFragment;

import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.utils.AppUtils;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;

import iyouqu.theme.FrameActivity;

/**
 * guide 页面
 */
public class GuideActivity extends FrameActivity implements GuideFragment.FragmentActivityCallback {
    public static final String ACTION_CHKNUM = AppUtils.getPackageName();
    ViewPager viewPager;
    GuideAdapter guideAdapter;
    boolean isFromApp = false;
    boolean isLoop = true;
    ImageView /*imageController,*/ imageCenter;
    private BookDaoHelper bookDaoHelper;
    private String[] PACKAGE_1 = {"cn.zsqbydq.reader", "cn.kkqbtxtxs.reader", "cn.qbzsydsq.reader", "cc.remennovel", "cc.quanbennovel", "cc.kdqbxs.reader", "cc.mianfeinovel", "cc.quanben.novel"};
    private String[] PACKAGE_2 = {"com.mianfeinovel"};
    private String[] PACKAGE_3 = {"com.lianzainovel", "cn.txtkdxsdq.reader", "cn.kdqbxs.reader", "com.quanben.novel", "cn.txtzsydsq.reader", "com.remennovel", "com.quanbennovel", "cc.lianzainovel"};

    @Override
    public void onCreate(Bundle paramBundle) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(paramBundle);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.act_guide_new);
        viewPager = (ViewPager) findViewById(R.id.vp_guide_pager);
        guideAdapter = new GuideAdapter(getSupportFragmentManager());
        guideAdapter.setPager(viewPager);

//        Bundle guide_first = new Bundle();
//        guide_first.putInt("image_center", R.drawable.guide_bg);
//        GuideFragment guideFragmentFirst = new GuideFragment();
//        guideFragmentFirst.setArguments(guide_first);
//
//        guideAdapter.add(guideFragmentFirst);
//
//        if (Arrays.asList(PACKAGE_2).contains(ACTION_CHKNUM)) {
//            Bundle guide_second = new Bundle();
//            guide_second.putInt("image_center", getResources().getIdentifier("splash_icon2", "drawable", getPackageName()));
//            GuideFragment guideFragmentSecond = new GuideFragment();
//            guideFragmentSecond.setArguments(guide_second);
//
//            guideAdapter.add(guideFragmentSecond);
//        } else if (Arrays.asList(PACKAGE_3).contains(ACTION_CHKNUM)) {
//            Bundle guide_second = new Bundle();
//            guide_second.putInt("image_center", getResources().getIdentifier("splash_icon2", "drawable", getPackageName()));
//            GuideFragment guideFragmentSecond = new GuideFragment();
//            guideFragmentSecond.setArguments(guide_second);
//
//            Bundle guide_third = new Bundle();
//            guide_third.putInt("image_center", getResources().getIdentifier("splash_icon3", "drawable", getPackageName()));
//            GuideFragment guideFragmentThird = new GuideFragment();
//            guideFragmentThird.setArguments(guide_third);
//
//            guideAdapter.add(guideFragmentSecond);
//            guideAdapter.add(guideFragmentThird);
//        }


        Bundle guideBundle = new Bundle();
        guideBundle.putInt("image_center", R.drawable.guide_bg);
        GuideFragment guideFragment = new GuideFragment();
        guideFragment.setArguments(guideBundle);

        guideAdapter.add(guideFragment);

        viewPager.setAdapter(guideAdapter);

        bookDaoHelper = BookDaoHelper.getInstance();
        if (getIntent() != null) {
            isFromApp = getIntent().getBooleanExtra("isFromApp", false);
        }
    }


    private void intoApp() {
        if (bookDaoHelper == null)
            bookDaoHelper = BookDaoHelper.getInstance();
        if (!isFromApp && bookDaoHelper != null) {
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            intent.setClass(GuideActivity.this, HomeActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
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
