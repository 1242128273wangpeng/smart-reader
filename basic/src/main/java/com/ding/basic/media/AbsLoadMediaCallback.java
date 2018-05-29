package com.ding.basic.media;

import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * @author lijun Lee
 * @desc 请求广告回调
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/3/12 15:58
 */

public abstract class AbsLoadMediaCallback {

    public void onResult(boolean adSwitch) {
    }

    /**
     * 拉取广告成功 单个广告
     */
    public void onResult(boolean adSwitch, View view) {
    }


    /**
     * 拉取广告成功 多个广告
     */
    public void onResult(boolean adSwitch, List<ViewGroup> views) {
    }

    /**
     * 广告补充
     */
    public void onRepairResult(boolean adSwitch, List<ViewGroup> views) {

    }

    /**
     * 拉取广告失败
     */
    public void onFailed() {
    }

    /**
     * 开屏广告被关闭
     */
    public void onMediaDismissed() {
    }
}
