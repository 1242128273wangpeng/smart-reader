package com.intelligent.reader.activity;

import com.intelligent.reader.R;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.RequestItem;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

import iyouqu.theme.FrameActivity;

public class SwitchModeActivity extends FrameActivity {
    int sequence;
    RequestItem requestItem;
    int offset;
    Book book;
    String thememode;
    ImageView imageView_night;
    ImageView imageView_light;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // 设置全屏,不需要可去掉

        setContentView(R.layout.activity_switch_mode);
        initView();

        initData();
        //设置两秒后执行当前activity的销毁操作
        new Handler().postDelayed(new Runnable() {
            public void run() {
                //execute the task
                back(null);
            }
        }, 1000);
    }

    private void initView() {
        imageView_night = (ImageView) findViewById(R.id.switch_mode_night);
        imageView_light = (ImageView) findViewById(R.id.switch_mode_light);
        if (mThemeHelper.isNight()) {
            imageView_night.setVisibility(View.VISIBLE);
            imageView_light.setVisibility(View.GONE);
        } else {
            imageView_light.setVisibility(View.VISIBLE);
            imageView_night.setVisibility(View.GONE);
        }
    }

    private void initData() {
        Bundle bundle = getIntent().getExtras();
        sequence = bundle.getInt("sequence", 0);
        requestItem = (RequestItem) bundle.getSerializable(Constants.REQUEST_ITEM);
        offset = bundle.getInt("offset", 0);
        book = (Book) bundle.getSerializable("book");
        thememode = bundle.getString("thememode");
    }

    public void back(View v) {
        try {
            Intent intent = new Intent(this, ReadingActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("sequence", sequence);
            bundle.putInt("offset", offset);
            bundle.putSerializable("book", book);
            bundle.putSerializable(Constants.REQUEST_ITEM, requestItem);
            bundle.putString("thememode", thememode);
            intent.putExtras(bundle);
            startActivity(intent);
            this.finish();
        } catch (Exception e) {
        }
    }

    //按返回键
    @Override
    public void onBackPressed() {
        Map<String, String> data = new HashMap<>();
        data.put("type", "2");
        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.BACK, data);
        back(null);
    }
}
