package net.lzbook.kit.utils.update;

import android.app.Dialog;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.lzbook.kit.R;
import net.lzbook.kit.app.ActionConstants;
import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.MD5Utils;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.ResourceUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadIntentService extends IntentService {

    private static final String TAG = "DownloadIntentService";

    public static boolean isDownloading;
    public DownloadIntentService() {
        super("");
    }

    public DownloadIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String downloadLink = intent.getStringExtra("downloadLink");
        String md5 = intent.getStringExtra("md5");

        String fileName = intent.getStringExtra("fileName");
        //文件要保存的路径
        String filePath = ReplaceConstants.getReplaceConstants().APP_PATH_DOWNLOAD + fileName;

        NotificationManager manager = (NotificationManager) getSystemService(Context
                .NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(ResourceUtil.getStringById(R.string.app_name) + "下载中...")
                .setContentText("已经下载 0%")
                .setAutoCancel(true);
        manager.notify(0, builder.build());

        //调用网络访问，设置带有进度条的通知
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            bos = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
            URL url = new URL(downloadLink);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("GET");
            httpConn.setDoInput(true);
            httpConn.setDoOutput(false);
            httpConn.connect();

            if (httpConn.getResponseCode() == 200) {
                isDownloading = true;
                bis = new BufferedInputStream(httpConn.getInputStream());
                //获取网络访问的文件的总字节数
                int totalLength = httpConn.getContentLength();
                //定义当前已经下载的总字节数
                int currentLength = 0;

                //常规网络访问的代码
                byte[] buffer = new byte[1024 * 8];
                int c = 0;

                int progress = 0;
                while ((c = bis.read(buffer)) != -1) {
                    currentLength += c;
                    bos.write(buffer, 0, c);
                    bos.flush();

                    progress = (int) ((currentLength / (float) totalLength) * 100);
                    builder.setProgress(100, progress, false);
                    builder.setContentText("已经下载 " + progress + "%");
                    manager.notify(0, builder.build());
                }
                if (MD5Utils.getFileMD5(new File(filePath)).equals(md5)) {
                    //设置PendingINtent，实现点击播放——利用Intent的data和type、action属性
                    Intent myIntent = new Intent();
                    myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    myIntent.setAction(Intent.ACTION_VIEW);
                    myIntent.setDataAndType(Uri.fromFile(new File(filePath)), "application/vnd.android.package-archive");
                    PendingIntent pIntent = PendingIntent.getActivity(this, 1, myIntent, PendingIntent.FLAG_ONE_SHOT);
                    //下载完毕后，修改通知上的图标以及提示文字，并且设置PendingIntent
                    builder.setSmallIcon(android.R.drawable.stat_sys_download_done)
                            .setContentTitle("点击安装！")
                            .setContentIntent(pIntent);
                    manager.notify(0, builder.build());
                }else {
                    //下载完毕后，修改通知上的图标以及提示文字，并且设置PendingIntent
                    builder.setSmallIcon(android.R.drawable.stat_sys_download_done)
                            .setContentTitle("下载完成！");
                    manager.notify(0, builder.build());
                }
                if (progress == 100) {
                    //发送广播
                    Intent broakCastIntent = new Intent();
                    broakCastIntent.putExtra("count", progress);
                    broakCastIntent.putExtra("filePath", filePath);
                    broakCastIntent.putExtra("md5", md5);
                    broakCastIntent.putExtra("downloadLink", downloadLink);
                    broakCastIntent.setAction(ActionConstants.DOWN_APP_SUCCESS_ACTION);
                    sendBroadcast(broakCastIntent);
                }


                isDownloading = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (Exception ex) {

            }
        }
    }
}
