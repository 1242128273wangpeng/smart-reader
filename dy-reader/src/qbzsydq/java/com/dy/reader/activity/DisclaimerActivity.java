package com.dy.reader.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.dingyue.contract.router.RouterConfig;
import com.dy.reader.R;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;

import java.util.HashMap;
import java.util.Map;

@Route(path = RouterConfig.DISCLAIMER_ACTIVITY)
public class DisclaimerActivity extends iyouqu.theme.FrameActivity {
    private boolean isFromReadingPage = false;

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.act_disclaimer);
        if (getIntent() != null) {
            isFromReadingPage = getIntent().getBooleanExtra("isFromReadingPage", false);
            if (isFromReadingPage) {
                ((TextView) findViewById(R.id.tv_title_name)).setText(getResources().getString(R.string.translate_code_disclaimer));
                ((TextView) findViewById(R.id.tv_disclaimer_statement)).setText(getResources().getString(R.string.translate_code_description));
            }
        }
        findViewById(R.id.btn_left_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> data = new HashMap<>();
                data.put("type", "1");
                StartLogClickUtil.upLoadEventLog(DisclaimerActivity.this, StartLogClickUtil.PROCTCOL_PAGE, StartLogClickUtil.BACK, data);
                finish();
            }
        });
    }
}
