package net.lzbook.kit.utils.swipeback;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import net.lzbook.kit.utils.logger.AppLog;

import java.util.LinkedList;
import java.util.List;

/**
 * Desc Activity生命周期监听，用于保存Activity实例
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2017/11/20
 */

public class ActivityLifecycleHelper implements Application.ActivityLifecycleCallbacks {

    private static ActivityLifecycleHelper singleton;
    private static final Object lockObj = new Object();
    private static List<Activity> activities;

    private ActivityLifecycleHelper() {
        activities = new LinkedList<>();
    }

    public static ActivityLifecycleHelper build() {
        synchronized (lockObj) {
            if (singleton == null) {
                singleton = new ActivityLifecycleHelper();
            }
            return singleton;
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        addActivity(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if(activities != null) {
            if (activities.contains(activity)) {
                activities.remove(activity);
            }

            if (activities.size() == 0) {
                activities = null;
            }
        }
    }

    public void finishActivity(Activity activity) {
        if(activities != null) {
            if (activities.contains(activity)) {
                activities.remove(activity);
            }

            if (activities.size() == 0) {
                activities = null;
            }
        }
    }

    /**
     * 添加Activity到堆栈
     */
    public void addActivity(Activity activity) {
        if (activities == null) {
            activities = new LinkedList<>();
        }
        boolean isSplashActivity = getName(activity).equals("SplashActivity");
        boolean isGuideActivity = getName(activity).equals("GuideActivity");
        boolean isOfflineNotifyActivity = getName(activity).equals("OfflineNotifyActivity");
        if (isSplashActivity || isGuideActivity || isOfflineNotifyActivity) return;
        AppLog.e("ActivityLifecycleHelper", "add: " + getName(activity));
        activities.add(activity);
    }

    /**
     * 获取集合中当前Activity
     *
     * @return
     */
    public static Activity getLatestActivity() {
        if (activities == null) {
            activities = new LinkedList<>();
        }
        int count = activities.size();
        if (count == 0) {
            return null;
        }
        return activities.get(count - 1);
    }

    /**
     * 获取集合中上一个Activity
     *
     * @return
     */
    public static Activity getPreviousActivity() {
        if (activities == null) {
            activities = new LinkedList<>();
        }
        int count = activities.size();
        if (count < 2) {
            return null;
        }
        return activities.get(count - 2);
    }

    public static List<Activity> getActivities() {
        if (activities == null) {
            activities = new LinkedList<>();
        }
        return activities;
    }

    public static Boolean isInHome() {
        Activity activity = getLatestActivity();
        return activity != null && getName(activity).equals("HomeActivity");
    }

    private static String getName(Context context) {
        String contextString = context.toString();
        return contextString.substring(contextString.lastIndexOf(".") + 1, contextString.indexOf("@"));
    }

}
