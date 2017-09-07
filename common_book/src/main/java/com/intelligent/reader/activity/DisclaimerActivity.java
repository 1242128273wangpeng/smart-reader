package com.intelligent.reader.activity;

import com.intelligent.reader.R;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


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
                finish();
            }
        });
    }
}
