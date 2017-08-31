package com.intelligent.reader.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.intelligent.reader.R;

import net.lzbook.kit.utils.update.ApkUpdateUtils;



public class DownloadErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_error);
    }

    public void clickView(View view) {
        switch (view.getId()) {
            case R.id.button_downloadError:
                Intent intent = getIntent();
                String downloadLink = intent.getStringExtra("downloadLink");
                String md5 = intent.getStringExtra("md5");
                String fileName = intent.getStringExtra("fileName");

                ApkUpdateUtils apkUpdateUtils = new ApkUpdateUtils(this);
                apkUpdateUtils.downloadService(downloadLink, md5, fileName);
                finish();
                break;
        }
    }
}
