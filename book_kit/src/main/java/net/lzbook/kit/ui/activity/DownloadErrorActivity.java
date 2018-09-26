package net.lzbook.kit.ui.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import net.lzbook.kit.R;
import net.lzbook.kit.utils.ApkUpdateUtils;


public class DownloadErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_error);
        findViewById(R.id.button_downloadError).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                String downloadLink = intent.getStringExtra("downloadLink");
                String md5 = intent.getStringExtra("md5");
                String fileName = intent.getStringExtra("fileName");

                ApkUpdateUtils apkUpdateUtils = new ApkUpdateUtils(DownloadErrorActivity.this);
                apkUpdateUtils.downloadService(downloadLink, md5, fileName);
                finish();
            }
        });
    }

}
