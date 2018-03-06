package com.intelligent.reader.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.intelligent.reader.R;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;

import java.util.HashMap;
import java.util.Map;


public class DisclaimerActivity extends iyouqu.theme.FrameActivity {

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.act_disclaimer);
        if (getIntent() != null) {
            boolean isFromReadingPage = getIntent().getBooleanExtra("isFromReadingPage", false);
            if (isFromReadingPage) {
                ((TextView) findViewById(R.id.txt_head_title)).setText(getResources().getString(R.string.translate_code_disclaimer));
                ((TextView) findViewById(R.id.txt_content)).setText(getResources().getString(R.string.translate_code_description));
            }
        }
        findViewById(R.id.img_head_back).setOnClickListener(new View.OnClickListener() {
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
