package net.lzbook.kit.widget;

import net.lzbook.kit.R;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.utils.logger.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.NetWorkUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;

public class LoadingPage extends FrameLayout {

    public static final int setting_result = 0x55;
    public int settingCode = 0;
    public boolean isCategory = false;
    String TAG = "LoadingPage";
    reloadCallback reload;
    boolean isCustomLoading = false;
    private ProgressBar loading_progressbar;
    private ImageView img_loading;
    private Runnable settingAction;
    private Callable<Void> reLoadAction;
    private RelativeLayout loading_error_view;
    private Button setting_btn, reload_btn;
    private TextView tv_network_error;
    private WeakReference<Activity> actReference;
    private View loadView;
    private View errorView;
    private ViewGroup root;
    private Runnable errorAction;
    private Runnable successAction;
    private TextView tv_loading_progress;
    private TextView tv_novel_source;
    private boolean isFromReadingPage = false;//标识来自于阅读页的加载页
    private String novelSource = "";

    public LoadingPage(Activity activity) {
        super(activity);
        actReference = new WeakReference<>(activity);
        Activity act = actReference.get();
        if (act != null) {
            root = (act.getWindow().getDecorView().findViewById(android.R.id.content));
            initView();
        }
    }

    public LoadingPage(Activity activity, boolean isFromReadingPage, String novelSource,
            int settingCode) {
        super(activity);
        this.isFromReadingPage = isFromReadingPage;
        this.novelSource = novelSource;
        this.settingCode = settingCode;
        actReference = new WeakReference<>(activity);
        Activity act = actReference.get();
        if (act != null) {
            root = (act.getWindow().getDecorView().findViewById(android.R.id.content));
            initView();
        }
    }

    public LoadingPage(Activity activity, int code) {
        super(activity);
        actReference = new WeakReference<>(activity);
        Activity act = actReference.get();
        if (act != null) {
            settingCode = code;
            root = (act.getWindow().getDecorView().findViewById(android.R.id.content));
            initView();
        }

    }

    public LoadingPage(Activity activity, ViewGroup layout) {
        super(activity);
        actReference = new WeakReference<>(activity);
        Activity act = actReference.get();
        if (act != null) {
            root = layout;
            initView();
        }
    }

    public LoadingPage(Activity activity, ViewGroup layout, int settingCode) {
        super(activity);
        actReference = new WeakReference<>(activity);
        Activity act = actReference.get();
        if (act != null) {
            root = layout;
            initView();
        }
    }

    public LoadingPage(Activity activity, AttributeSet attrs) {
        super(activity, attrs);
        actReference = new WeakReference<>(activity);
        Activity act = actReference.get();
        if (act != null) {
            root = (act.getWindow().getDecorView().findViewById(android.R.id.content));
            initView();
        }
    }

    public void setNovelSource(String novelSource) {
        this.novelSource = novelSource;
        setLoadUrl();
    }

    public void loading(final Callable<Void> task) {
        final Activity act = actReference.get();
        if (act == null) {
            return;
        }

        act.runOnUiThread(new Runnable() {
            public void run() {
//                initView();
                addRootView();
                if (NetWorkUtils.NETWORK_NONE == NetWorkUtils.getNetWorkType(act)
                        && Constants.is_reading_network_limit) {
                    onNetWorkError();
                } else {
                    try {
                        task.call();
                    } catch (final Exception e) {
                        if (errorAction != null) {
                            errorAction.run();
                        }
                        setSettingAction(new Runnable() {
                            @Override
                            public void run() {
                                // act.finish();
                                startNetSetting(settingCode);
                            }
                        });

                        setReloadAction(task);
                        Activity act = actReference.get();
                        if (act == null) {
                            return;
                        }
                        act.runOnUiThread(new Runnable() {
                            public void run() {
                                onError(e);
                            }
                        });

                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 根据不同阅读模式设置loading背景
     */
    @SuppressLint("ResourceAsColor")
    public void setCustomBackgroud() {
        isCustomLoading = true;
        if (Constants.MODE == 51) {
            setLoadingBg(R.color.reading_backdrop_first);
            setLoadingTextColor(R.color.reading_text_color_first);
        } else if (Constants.MODE == 52) {
            setLoadingBg(R.color.reading_backdrop_second);
            setLoadingTextColor(R.color.reading_text_color_second);
        } else if (Constants.MODE == 53) {
            setLoadingBg(R.color.reading_backdrop_third);
            setLoadingTextColor(R.color.reading_text_color_third);
        } else if (Constants.MODE == 54) {
            setLoadingBg(R.color.reading_backdrop_fourth);
            setLoadingTextColor(R.color.reading_text_color_fourth);
        } else if (Constants.MODE == 55) {
            setLoadingBg(R.color.reading_backdrop_fifth);
            setLoadingTextColor(R.color.reading_text_color_fifth);
        } else if (Constants.MODE == 56) {
            setLoadingBg(R.color.reading_backdrop_sixth);
            setLoadingTextColor(R.color.reading_text_color_sixth);
        } else if (Constants.MODE == 57) {
            setLoadingBg(R.color.reading_backdrop_seventh);
            setLoadingTextColor(R.color.reading_text_color_seventh);
        } else if (Constants.MODE == 58) {
            setLoadingBg(R.color.reading_backdrop_eighth);
            setLoadingTextColor(R.color.reading_text_color_eighth);
        } else if (Constants.MODE == 59) {
            setLoadingBg(R.color.reading_backdrop_ninth);
            setLoadingTextColor(R.color.reading_text_color_ninth);
        } else if (Constants.MODE == 60) {
            setLoadingBg(R.color.reading_backdrop_tenth);
            setLoadingTextColor(R.color.reading_text_color_tenth);
        } else if (Constants.MODE == 61) {
            setLoadingBg(R.color.reading_backdrop_night);
            setLoadingTextColor(R.color.reading_text_color_night);
        } else {
            setLoadingBg(R.color.reading_backdrop_first);
            setLoadingTextColor(R.color.reading_text_color_first);
        }
        Activity act = actReference.get();
        if (act == null) {
            return;
        }
    }

    private void setLoadingBg(int color) {
        Activity act = actReference.get();
        if (act == null) {
            return;
        }
        loadView.setBackgroundColor(act.getResources().getColor(color));
        errorView.setBackgroundColor(act.getResources().getColor(color));
    }

    private void setLoadingTextColor(int color) {
        Activity act = actReference.get();
        if (act == null) {
            return;
        }
        if(!"cc.quanben.novel".equals(AppUtils.getPackageName())){
            tv_loading_progress.setTextColor(act.getResources().getColor(color));
        }

    }

    @SuppressLint("ResourceAsColor")
    private void initView() {
        final Activity act = actReference.get();
        if (act == null) {
            return;
        }

        if (isFromReadingPage) {
            loadView = LayoutInflater.from(act).inflate(R.layout.loading_page_reading, null);
            tv_novel_source =  loadView.findViewById(R.id.tv_novel_source);
            setLoadUrl();
        } else {
            loadView = LayoutInflater.from(act).inflate(R.layout.loading_page, null);
        }


        errorView = LayoutInflater.from(act).inflate(R.layout.error_page2, null);
        errorView.setVisibility(View.GONE);
        addView(loadView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(errorView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        tv_loading_progress =  loadView.findViewById(R.id.tv_loading_progress);
        loading_error_view =  errorView.findViewById(R.id.loading_error_view);
        setting_btn =  errorView.findViewById(R.id.loading_error_setting);
        setting_btn.setVisibility(GONE);
        reload_btn =  errorView.findViewById(R.id.loading_error_reload);
        tv_network_error =  errorView.findViewById(R.id.tv_network_error);
        tv_network_error.setText(R.string.read_network_error);

        loading_progressbar =  loadView.findViewById(R.id.loading_progressbar);
        img_loading = loadView.findViewById(R.id.img_loading);
        if("cc.quanben.novel".equals(AppUtils.getPackageName())){
            img_loading.setVisibility(View.VISIBLE);
            loading_progressbar.setVisibility(GONE);
            ((AnimationDrawable)img_loading.getDrawable()).start();
        }else{
            img_loading.setVisibility(View.GONE);
            loading_progressbar.setVisibility(VISIBLE);
        }

//        setLoadingBg(R.color.color_white_faf9f7);
        setLoadingTextColor(R.color.color_brown_a8978d);

        setting_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (settingAction != null) {
                    settingAction.run();
                } else {
                    startNetSetting(settingCode);
                }
            }
        });
        reload_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                loadView.setVisibility(View.VISIBLE);
                errorView.setVisibility(View.GONE);

                Activity act = actReference.get();

                if (NetWorkUtils.getNetWorkType(act.getApplicationContext())
                        == NetWorkUtils.NETWORK_NONE && Constants.is_reading_network_limit) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Activity act = actReference.get();
                            if (act != null) {
                                act.runOnUiThread(new Runnable() {
                                    public void run() {
                                        onErrorVisable();
                                    }
                                });
                            }
                        }
                    }).start();
                } else if (reLoadAction != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                reLoadAction.call();
                                if (isCustomLoading && !isCategory) {
                                    Activity act = actReference.get();
                                    if (act == null) {
                                        return;
                                    }
                                    act.runOnUiThread(new Runnable() {
                                        public void run() {
                                            onSuccess();
                                        }
                                    });
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                Activity act = actReference.get();
                                if (act == null) {
                                    return;
                                }
                                act.runOnUiThread(new Runnable() {
                                    public void run() {
                                        onErrorVisable();
                                    }
                                });
                            }


                        }
                    }).start();

                } else if (reload != null) {
                    reload.doReload();
                }
            }
        });
        if (root != null) {
            root.addView(this,
                    new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }
    }

    private void addRootView() {
        if (root != null) {
            if (this.getParent() == null) {
                root.addView(this,
                        new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            }
        }
    }

    private void setLoadUrl() {
        AppLog.e(TAG, "setLoadUrl:" + novelSource);
        if (tv_novel_source != null && !TextUtils.isEmpty(novelSource) && !"null".equals(
                novelSource)) {
            Activity activity = actReference.get();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_novel_source.setText(novelSource);
                }
            });
        }
    }

    public void startLoading() {
        this.setVisibility(View.VISIBLE);
        loadView.setVisibility(View.VISIBLE);
    }

    public void setErrorAction(Runnable error) {
        this.errorAction = error;
    }

    public void setSettingAction(Runnable back) {
        this.settingAction = back;
    }

    public void setReloadAction(Callable<Void> reload) {
        this.reLoadAction = reload;
    }

    public void onSuccess() {
        if (root != null) {
            root.removeView(this);
        }
    }

    public void onError(Exception e) {
        loadView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);

        if (Constants.is_reading_network_limit) {
            tv_network_error.setText(R.string.read_network_error);
            setting_btn.setVisibility(VISIBLE);
        } else {
            tv_network_error.setText(R.string.read_network_error);
            setting_btn.setVisibility(GONE);
        }

        setSettingAction(new Runnable() {
            @Override
            public void run() {
                startNetSetting(settingCode);
            }
        });
    }

    public void onError() {
        loadView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);

        if (Constants.is_reading_network_limit) {
            tv_network_error.setText(R.string.read_network_error);
            setting_btn.setVisibility(VISIBLE);
        } else {
            tv_network_error.setText(R.string.read_network_error);
            setting_btn.setVisibility(GONE);
        }

        setSettingAction(new Runnable() {
            @Override
            public void run() {
                startNetSetting(settingCode);
            }
        });
    }

    public void onNetWorkError() {
        Constants.isNetWorkError = true;
        loadView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);

        if (Constants.is_reading_network_limit) {
            tv_network_error.setText(R.string.read_network_error);
            setting_btn.setVisibility(VISIBLE);
        } else {
            tv_network_error.setText(R.string.read_network_error);
            setting_btn.setVisibility(GONE);
        }
        setSettingAction(new Runnable() {
            @Override
            public void run() {
                startNetSetting(settingCode);
            }
        });
    }

    public void onSuccessGone() {
        this.setVisibility(View.GONE);
    }

    public void onErrorVisable() {
        this.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.VISIBLE);

        if (Constants.is_reading_network_limit) {
            tv_network_error.setText(R.string.read_network_error);
            setting_btn.setVisibility(VISIBLE);
        } else {
            tv_network_error.setText(R.string.read_network_error);
            setting_btn.setVisibility(GONE);
        }
        loadView.setVisibility(View.GONE);

        setSettingAction(new Runnable() {
            @Override
            public void run() {
                startNetSetting(settingCode);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    public void setLoadText(String text) {
        tv_loading_progress.setText(text);
    }

    public void startNetSetting(int requestCode) {
        String action_3 = android.provider.Settings.ACTION_SETTINGS;
        String action = android.provider.Settings.ACTION_WIRELESS_SETTINGS;
        if (requestCode != 0) {
            if (android.os.Build.VERSION.SDK_INT > 10) {
                // 3.0以上打开设置界面
                startForResult(requestCode, action_3);
            } else {
                startForResult(requestCode, action);
            }
        } else {
            if (android.os.Build.VERSION.SDK_INT > 10) {
                startOnly(action_3);
            } else {
                startOnly(action);
            }
        }
    }

    public void startForResult(int requestCode, String action) {
        Activity act = actReference.get();
        if (act == null) {
            return;
        }
        try {
            Intent intent = new Intent();
            intent.setAction(action);
            act.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void startOnly(String action) {
        Activity act = actReference.get();
        if (act == null) {
            return;
        }
        try {
            Intent intent = new Intent();
            intent.setAction(action);
            act.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setReloadAction(reloadCallback reload) {
        this.reload = reload;
    }

    public boolean isLoadingVisible() {
        return loadView.getVisibility() == View.VISIBLE && this.getVisibility() == View.VISIBLE;
    }

    public interface reloadCallback {
        void doReload();
    }
}
