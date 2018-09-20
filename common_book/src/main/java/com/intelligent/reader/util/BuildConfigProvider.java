package com.intelligent.reader.util;

import android.content.Context;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.ding.basic.util.IBuildConfigProvider;
import com.ding.basic.util.ReplaceConstants;
import com.intelligent.reader.BuildConfig;

import org.jetbrains.annotations.NotNull;
@Route(path = ReplaceConstants.BUILD_CONFIG_PROVIDER)
public class BuildConfigProvider implements IBuildConfigProvider {
    @Override
    public void init(Context context) {

    }

    @NotNull
    @Override
    public String getAppPath() {
        return BuildConfig.app_path;
    }

    @NotNull
    @Override
    public String getBookNovelDeployHost() {
        return BuildConfig.book_novel_deploy_host;
    }

    @NotNull
    @Override
    public String getBookWebviewHost() {
        return BuildConfig.book_webview_host;
    }

    @NotNull
    @Override
    public String getDatabaseName() {
        return BuildConfig.database_name;
    }

    @NotNull
    @Override
    public String getBaiduStatId() {
        return BuildConfig.baidu_stat_id;
    }

    @NotNull
    @Override
    public String getPushKey() {
        return BuildConfig.push_key;
    }

    @NotNull
    @Override
    public String getAlifeedbackKey() {
        return BuildConfig.alifeedback_key;
    }

    @NotNull
    @Override
    public String getAlifeedbackSecret() {
        return BuildConfig.alifeedback_secret;
    }

    @NotNull
    @Override
    public String getMicroApiHost() {
        return BuildConfig.micro_api_host;
    }

    @NotNull
    @Override
    public String getContentApiHost() {
        return BuildConfig.content_api_host;
    }
}
