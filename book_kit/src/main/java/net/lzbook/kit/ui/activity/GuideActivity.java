package net.lzbook.kit.ui.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.lzbook.kit.R;
import net.lzbook.kit.ui.activity.base.FrameActivity;
import net.lzbook.kit.ui.adapter.GuideAdapter;
import net.lzbook.kit.ui.fragment.GuideFragment;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.router.RouterConfig;
import net.lzbook.kit.utils.router.RouterUtil;

/**
 * guide 页面
 */
public class GuideActivity extends FrameActivity implements GuideFragment.FragmentActivityCallback {
    public static final String ACTION_CHKNUM = AppUtils.getPackageName();
    ViewPager viewPager;
    GuideAdapter guideAdapter;
    boolean isFromApp = false;
    boolean isLoop = true;
    FrameLayout /*imageController,*/ flMain;
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
        int guide_bg=AppUtils.getDrawableByName(this,"guide_bg");
        if(guide_bg!=-1) {
            guideBundle.putInt("image_center", guide_bg);
        }
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
            Bundle bundle = new Bundle();
            RouterUtil.INSTANCE.navigation(GuideActivity.this, RouterConfig.HOME_ACTIVITY,bundle);
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
    public void getImageCenter(FrameLayout flMain) {
        this.flMain = flMain;
        flMain.setOnClickListener(new View.OnClickListener() {
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
