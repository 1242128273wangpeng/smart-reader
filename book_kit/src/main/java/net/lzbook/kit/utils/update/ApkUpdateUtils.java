package net.lzbook.kit.utils.update;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;

import net.lzbook.kit.R;
import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.view.MyDialog;
import net.lzbook.kit.net.custom.service.NetService;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.ToastUtils;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/9/21.
 */
public class ApkUpdateUtils {
    private WeakReference<Activity> reference;
    private int type;

    public ApkUpdateUtils(Activity activity) {
        this.reference = new WeakReference<>(activity);
    }

    public void getApkUpdateInfo(final Context context, final Handler handler, final String from) throws Exception {
        Map<String, String> params = new HashMap<>();
        String versionName = AppUtils.getVersionName();
//        versionName = versionName.substring(0,3);
        params.put("versionName", versionName);

        NetService.INSTANCE.getUserService().checkAppUpdate(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<JsonObject>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        AppLog.e("ApkUpdateUtils", "开始检查是否需要升级应用");
                    }

                    @Override
                    public void onNext(@NonNull JsonObject result) {
                        try {
                            ApkUpdateInfo apkUpdateInfo = new ApkUpdateInfo(result.toString());
                            final ApkUpdateInfo finalApkUpdateInfo = apkUpdateInfo;
//                            handler.post(new Runnable() {
//                                @Override
//                                public void run() {
                            if ("SettingActivity".equals(from)) {
                                type = 2;
                                doUpdateFromSettingACT(finalApkUpdateInfo);
                            } else if ("HomeActivity".equals(from)) {
                                type = 1;
                                doUpdate(finalApkUpdateInfo);
                            }
//                                }
//                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        ToastUtils.showToastNoRepeat("网络不给力哦");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void doUpdate(ApkUpdateInfo apkUpdateInfo) {
        String fileName = AppUtils.getPackageName() + "_" + apkUpdateInfo.updateVersion + ".apk";
        if (apkUpdateInfo.isUpdate.equals("1")) {
            if (apkUpdateInfo.isForceUpdate.equals("1")) {
                Toast.makeText(BaseBookApplication.getGlobalContext(), "有新版本，需更新", Toast.LENGTH_SHORT).show();
                doForcedUpdate(apkUpdateInfo.downloadLink, apkUpdateInfo.updateContent, apkUpdateInfo.md5, fileName);
            } else {
                if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_WIFI) {
                    doSilentUpdate(apkUpdateInfo.downloadLink, apkUpdateInfo.md5, fileName);
                } else {
                    doNormalUpdate(apkUpdateInfo.downloadLink, apkUpdateInfo.updateContent, apkUpdateInfo.md5, fileName);
                }
            }
        }
    }

    public void doUpdateFromSettingACT(ApkUpdateInfo apkUpdateInfo) {
        String fileName = AppUtils.getPackageName() + "_" + apkUpdateInfo.updateVersion + ".apk";
        if (apkUpdateInfo.isUpdate.equals("1")) {
            if (apkUpdateInfo.isForceUpdate.equals("1")) {
                doForcedUpdate(apkUpdateInfo.downloadLink, apkUpdateInfo.updateContent, apkUpdateInfo.md5, fileName);
            } else {
                doNormalUpdate(apkUpdateInfo.downloadLink, apkUpdateInfo.updateContent, apkUpdateInfo.md5, fileName);
            }
        } else {
            Toast.makeText(BaseBookApplication.getGlobalContext(), "已是最新版本，暂无更新", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 静默下载
     */
    public void doSilentUpdate(String downloadLink, String md5, String fileName) {
        downloadService(downloadLink, md5, fileName);
    }


    /**
     * 正常下载逻辑
     *
     * @param downloadLink  下载链接
     * @param updateContent 更新内容
     */
    public void doNormalUpdate(final String downloadLink, String updateContent, final String md5, final String fileName) {
        final MyDialog updateDialog = new MyDialog(reference.get(), R.layout.own_update_dialog, Gravity.CENTER);
        updateDialog.setCanceledOnTouchOutside(true);
        TextView umeng_update_content = (TextView) updateDialog.findViewById(R.id.umeng_update_content);
        String content = updateContent.replace("\\r\\n", "\n");
        umeng_update_content.setText(content);

        Button umeng_update_id_ok = (Button) updateDialog.findViewById(R.id.umeng_update_id_ok);
        Button umeng_update_id_cancel = (Button) updateDialog.findViewById(R.id.umeng_update_id_cancel);

        umeng_update_id_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadService(downloadLink, md5, fileName);
                updateDialog.dismiss();

                if (type == 1) {
                    StartLogClickUtil.upLoadEventLog(reference.get(), StartLogClickUtil.SHELF_PAGE, StartLogClickUtil.VERSIONUPDATE2);
                } else {
                    StartLogClickUtil.upLoadEventLog(reference.get(), StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.VERSIONUPDATE);
                }

            }
        });

        umeng_update_id_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (updateDialog.isShowing()) {
                    updateDialog.dismiss();
                }
            }
        });
        if (!updateDialog.isShowing()) {
            try {
                updateDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 强制更新逻辑
     * <p>
     * 只显示一个按钮，点击后弹出进度框
     */
    public void doForcedUpdate(final String downloadLink, String updateContent, final String md5, final String fileName) {
        final MyDialog doForcedUpdateDialog = new MyDialog(reference.get(), R.layout.own_update_dialog, Gravity.CENTER);
        doForcedUpdateDialog.setCanceledOnTouchOutside(false);
        TextView umeng_update_content2 = (TextView) doForcedUpdateDialog.findViewById(R.id.umeng_update_content);
        umeng_update_content2.setText(updateContent);

        Button umeng_update_id_ok2 = (Button) doForcedUpdateDialog.findViewById(R.id.umeng_update_id_ok);
        Button umeng_update_id_cancel2 = (Button) doForcedUpdateDialog.findViewById(R.id.umeng_update_id_cancel);
        umeng_update_id_cancel2.setVisibility(View.GONE);
        umeng_update_id_ok2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadService(downloadLink, md5, fileName);
                doForcedUpdateDialog.dismiss();

                MyDialog myDialog = new MyDialog(reference.get(), R.layout.own_update_progress);
                myDialog.setCanceledOnTouchOutside(false);
                ProgressBar progressBar = (ProgressBar) myDialog.findViewById(R.id.loading_progressbar);
                progressBar.setVisibility(View.VISIBLE);
                myDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            reference.get().finish();

                        }
                        return false;
                    }
                });

                if (!myDialog.isShowing()) {
                    try {
                        myDialog.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                doForcedUpdateDialog.dismiss();

            }
        });
        if (!doForcedUpdateDialog.isShowing()) {
            try {
                doForcedUpdateDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        doForcedUpdateDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    reference.get().finish();
                }
                return false;
            }
        });
    }

    /**
     * 跳转至下载服务
     *
     * @param downloadLink 下载链接
     * @param md5          MD5
     */
    public void downloadService(String downloadLink, String md5, String fileName) {
        if (DownloadIntentService.isDownloading) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtra("downloadLink", downloadLink);
        intent.putExtra("md5", md5);
        intent.putExtra("fileName", fileName);
        intent.setClass(reference.get().getApplicationContext(), DownloadIntentService.class);
        reference.get().startService(intent);
    }


}
