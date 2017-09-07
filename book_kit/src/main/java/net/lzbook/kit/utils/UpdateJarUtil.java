package net.lzbook.kit.utils;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.ParseJarBean;
import net.lzbook.kit.net.volley.request.Parser;
import net.lzbook.kit.net.volley.request.VolleyDataService;
import net.lzbook.kit.request.UrlUtils;
import net.lzbook.kit.request.own.OWNParser;
import net.xxx.yyy.go.spider.MainExtractorInterface;
import net.xxx.yyy.go.spider.URLBuilderIntterface;

import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import dalvik.system.DexClassLoader;

/**
 * @author Bruce
 * @ClassName: UpdateJarUtil
 * @Description: TODO(更新解析jar包工具类)
 * @date 2015-3-17 下午4:30:13
 */
public class UpdateJarUtil {

    private static byte[] lock = new byte[0];
    private static ParseJarBean parseJarBean;

    //检查jar包更新
    public synchronized static void chekcJarUpNew(final Context context) {
        //检查更新
        final int premVersion = PreferenceManager.getDefaultSharedPreferences(context).getInt(Constants.PARSER_PACKAGE_VERSION, Constants
                .CURRENT_DEX_VERSION);
        String uri = URLBuilderIntterface.DEX_CHECK.replace("{premVersion}", String.valueOf(premVersion));
        String url = UrlUtils.buildUrl(uri, new HashMap<String, String>());
        try {
            // 5分钟内请求一次更新一次
            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            long updateTime = sp.getLong(Constants.JAR_UPDATE_TIME, 0);
            final long currentTime = System.currentTimeMillis();
            AppLog.e("chekcJarUpNew current version:", (premVersion) + "");

            if (currentTime - updateTime > Constants.jarUpdateTime) {
                AppLog.e("chekcJarUpNew time:", (currentTime - updateTime) + "");
                VolleyDataService.publicCode(url, null, new VolleyDataService.DataServiceCallBack() {
                    @Override
                    public void onSuccess(Object result) {
                        parseJarBean = (ParseJarBean) result;
                        if (parseJarBean == null) {
                            return;
                        }
                        if (premVersion >= parseJarBean.version) {
                            return;
                        }

                        AppLog.e("chekcJarUpNew update version:", (parseJarBean.version) + "");

                        if (parseJarBean.url != null) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (downloadJar(context, parseJarBean)) {
                                        // 下载到最新版本jar 重新load
                                        loadDexToApk(context, parseJarBean.md5);
                                    }
                                }
                            }).start();
                        }
                        // 重置计时器
                        sp.edit().putLong(Constants.JAR_UPDATE_TIME, currentTime).apply();

                        AppLog.e("chekcJarUpNew", " chekcJarUpNew 完成");
                    }

                    @Override
                    public void onError(Exception error) {
                        AppLog.e("chekcJarUpNew", " 获取dex的更新信息获取失败");
                    }
                }, new Parser() {
                    @Override
                    public Object parserMethod(String response) throws JSONException, Exception {
                        if (response != null) {
                            return OWNParser.parserJarUpdateInfo(response);
                        } else {
                            return null;
                        }
                    }
                });
            } else {
                AppLog.e("chekcJarUpNew", "server is too busy");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadDexToApk(Context context) {

        synchronized (lock) {
            String clazzNameExtractor = PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.CLAZZ_NAME_EXTRACTOR_KEY, Constants
                    .CLAZZ_NAME_EXTRACTOR_VALUE);
            String clazzNameUrlBuilder = PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.CLAZZ_NAME_URL_BUILDER_KEY,
                    Constants.CLAZZ_NAME_URL_BUILDER_VALUE);
            String dexName = PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.DEX_NAME_UPDATE_KEY, Constants.DEX_NAME);
            AppLog.e("initJar", "loadDexToApk: " + dexName);
            File dexInternalStoragePath = new File(context.getDir("dex", Context.MODE_PRIVATE), dexName);
            AppLog.e("initJar", "loadDexToApk: " + dexInternalStoragePath);
            File optimizedDexOutputPath = context.getDir("outdex", Context.MODE_PRIVATE);
            if (dexInternalStoragePath.exists()) {
                if (Constants.DEVELOPER_MODE) {
                    AppLog.e("loadDexToApk", " md5:" + MD5Utils.getFileMD5(dexInternalStoragePath));
                }
                try {
                    // 转换
                    DexClassLoader dexClassLoader = new DexClassLoader(dexInternalStoragePath.getAbsolutePath(),
                            optimizedDexOutputPath.getAbsolutePath(), null, context.getClassLoader());
                    AppLog.e("----->", "Start");
                    AppLog.e("----->", clazzNameExtractor);
                    Class jarClazz = dexClassLoader.loadClass(clazzNameExtractor);
                    AppLog.e("----->", "end");
                    MainExtractorInterface mainExtractorInterface = (MainExtractorInterface) jarClazz.newInstance();
                    if (mainExtractorInterface != null) {
                        BaseBookApplication.getGlobalContext().setMainExtractorInterface(mainExtractorInterface);
                    }

                    // URL builder
                    AppLog.e("-------->", clazzNameUrlBuilder);
                    jarClazz = dexClassLoader.loadClass(clazzNameUrlBuilder);
                    URLBuilderIntterface urlBuilderIntterface = (URLBuilderIntterface) jarClazz.newInstance();
                    if (urlBuilderIntterface != null) {
                        BaseBookApplication.getGlobalContext().setUrlBuilderIntterface(urlBuilderIntterface);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void loadDexToApk(Context context, String md5) {

        synchronized (lock) {
            String clazzNameExtractor = PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.CLAZZ_NAME_EXTRACTOR_KEY, Constants
                    .CLAZZ_NAME_EXTRACTOR_VALUE);
            String clazzNameUrlBuilder = PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.CLAZZ_NAME_URL_BUILDER_KEY,
                    Constants.CLAZZ_NAME_URL_BUILDER_VALUE);
            AppLog.e("loadDexToApk", "loadDexToApk : " + md5 + ".dex");
            File dexInternalStoragePath = new File(context.getDir("dex", Context.MODE_PRIVATE), md5 + ".dex");
            AppLog.e("loadDexToApk", "loadDexToApk : " + dexInternalStoragePath);
            File optimizedDexOutputPath = context.getDir("outdex", Context.MODE_PRIVATE);
            if (dexInternalStoragePath.exists()) {
                if (Constants.DEVELOPER_MODE) {
                    AppLog.e("loadDexToApk", " md5:" + MD5Utils.getFileMD5(dexInternalStoragePath));
                }
                try {
                    // 转换
                    DexClassLoader dexClassLoader = new DexClassLoader(dexInternalStoragePath.getAbsolutePath(),
                            optimizedDexOutputPath.getAbsolutePath(), null, context.getClassLoader());
                    AppLog.e("----->", "Start");
                    AppLog.e("----->", clazzNameExtractor);
                    Class jarClazz = dexClassLoader.loadClass(clazzNameExtractor);
                    AppLog.e("----->", "end");
                    MainExtractorInterface mainExtractorInterface = (MainExtractorInterface) jarClazz.newInstance();
                    AppLog.e("----->", "end : " + mainExtractorInterface.getClass().getPackage());
                    if (mainExtractorInterface != null) {
                        BaseBookApplication.getGlobalContext().setMainExtractorInterface(mainExtractorInterface);
                    }

                    // URL builder
                    AppLog.e("-------->", clazzNameUrlBuilder);
                    jarClazz = dexClassLoader.loadClass(clazzNameUrlBuilder);
                    URLBuilderIntterface urlBuilderIntterface = (URLBuilderIntterface) jarClazz.newInstance();
                    if (urlBuilderIntterface != null) {
                        BaseBookApplication.getGlobalContext().setUrlBuilderIntterface(urlBuilderIntterface);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 启动应用初始化jar,检查jar更新
    public synchronized static void initJar(final Context context) {

        //重置dex名字、加载类路径等内容
//        final int versionCode = AppUtils.getVersionCode();
//        final boolean firstInstall = PreferenceManager.getDefaultSharedPreferences(BaseBookApplication.getGlobalContext()).getBoolean(versionCode + "first_install_or_override", true);
        final int premVersionCode = PreferenceManager.getDefaultSharedPreferences(BaseBookApplication.getGlobalContext()).getInt("app_version_code", -1);
        Constants.preVersionCode = premVersionCode;
        final int currentVersionCode = AppUtils.getVersionCode();


        if (premVersionCode != currentVersionCode) {

            PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(Constants.PARSER_PACKAGE_VERSION,
                    Constants.CURRENT_DEX_VERSION).apply();
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(Constants.CLAZZ_NAME_EXTRACTOR_KEY,
                    Constants.CLAZZ_NAME_EXTRACTOR_VALUE).apply();
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(Constants.CLAZZ_NAME_URL_BUILDER_KEY,
                    Constants.CLAZZ_NAME_URL_BUILDER_VALUE).apply();
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(Constants.DEX_NAME_UPDATE_KEY,
                    Constants.DEX_NAME).apply();

//            String dexName = PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.DEX_NAME_UPDATE_KEY, Constants
//                    .DEX_NAME);


//            File dexInternalStoragePath = new File(context.getDir("dex", Context.MODE_PRIVATE), dexName);
            //把整个文件夹删除
            File dexInternalStoragePath = context.getDir("dex", Context.MODE_PRIVATE);

            File optimizedDexOutputPath = context.getDir("outdex", Context.MODE_PRIVATE);
            if (dexInternalStoragePath.exists()) {
//                dexInternalStoragePath.delete();
                deleteFile(dexInternalStoragePath);
            }
            if (optimizedDexOutputPath.exists()) {
//                optimizedDexOutputPath.delete();
                deleteFile(optimizedDexOutputPath);
            }

//            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(versionCode + "first_install_or_override",
//                    false).apply();
            PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("app_version_code",
                    currentVersionCode).apply();
        }


        AppLog.e("initJar", "");
        String dexName = PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.DEX_NAME_UPDATE_KEY, Constants
                .DEX_NAME);
        AppLog.e("initJar", "initJar: " + dexName);
        File dexInternalStoragePath = new File(context.getDir("dex", Context.MODE_PRIVATE), dexName);
        AppLog.e("initJar", "initJar: " + dexInternalStoragePath);
        if (!dexInternalStoragePath.exists()) {
            //dex不存在,copy assets 目录dex
            getParseUrlJarAssets(context);
        }

//        // 检查jar更新
//        chekcJarUpNew(context);

        // 加载jar
        loadDexToApk(context);

    }

    public synchronized static void deleteFile(File f) {
        if (f.exists()) {
            if (f.isDirectory()) {
                for (File fi : f.listFiles()) {
                    if (fi.isDirectory()) {
                        deleteFile(fi);
                    } else {
                        fi.delete();
                    }
                }
            } else {
                f.delete();
            }
        }
    }


    public static void resetJar(Context context) {
        synchronized (lock) {
            String dexName = PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.DEX_NAME_UPDATE_KEY, Constants
                    .DEX_NAME);
            File dexInternalStoragePath = new File(context.getDir("dex", Context.MODE_PRIVATE), dexName);
            File optimizedDexOutputPath = context.getDir("outdex", Context.MODE_PRIVATE);
            if (dexInternalStoragePath.exists()) {
                dexInternalStoragePath.delete();
            }
            if (optimizedDexOutputPath.exists()) {
                optimizedDexOutputPath.delete();
            }
            // 重置dex版本
            PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(Constants.PARSER_PACKAGE_VERSION, Constants.CURRENT_DEX_VERSION)
                    .apply();
        }
    }

    private static boolean downloadJar(Context context, ParseJarBean parseJarBean) {
        synchronized (lock) {
            AppLog.e("downloadJar", " downloadJar 开始下载");
            try {
                InputStream inputStream = HttpUtils.getZIPInputStreamAndThrow(parseJarBean.url);
//                byte[] b = new byte[1024];
//                FileOutputStream fos = new FileOutputStream(ReplaceConstants.getReplaceConstants().APP_PATH + "wyh.dex");
//                while ((inputStream.read(b)) != -1) {
//                    fos.write(b);
//                }
//                inputStream.close();
//                fos.close();
                byte[] bytes = FileUtils.readBytes(inputStream);
                if (bytes != null && bytes.length > 0) {
                    File dexInternalStoragePath = new File(context.getDir("dex", Context.MODE_PRIVATE), parseJarBean.md5 + ".dex");
                    AppLog.e("downloadJar", " downloadJar 开始下载 : " + dexInternalStoragePath.getAbsolutePath());
                    bytes = EncryptUtils.decrypt(bytes);
                    boolean flag = FileUtils.writeByteFile(dexInternalStoragePath.getPath(), bytes);
                    if (flag) {
                        String md5 = MD5Utils.getFileMD5(dexInternalStoragePath);
                        AppLog.e("downloadJar", "parseJarBean.md5: " + parseJarBean.md5 + " >>> file md5：" + md5);
                        if (parseJarBean.md5 != null && parseJarBean.md5.equalsIgnoreCase(md5)) {
                            PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(Constants.PARSER_PACKAGE_VERSION, parseJarBean
                                    .version).apply();
                            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(Constants.CLAZZ_NAME_EXTRACTOR_KEY, parseJarBean
                                    .dynamicPackage + ".MainExtractor").apply();
                            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(Constants.CLAZZ_NAME_URL_BUILDER_KEY, parseJarBean
                                    .dynamicPackage + ".URLBuilder").apply();

                            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(Constants.DEX_NAME_UPDATE_KEY, parseJarBean
                                    .md5 + ".dex").apply();
                            AppLog.e("downloadJar", " downloadJar 包下载成功");
                            return true;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // 从assets中读取dex
    private static boolean getParseUrlJarAssets(Context context) {
        synchronized (lock) {
            File dexInternalStoragePath = new File(context.getDir("dex", Context.MODE_PRIVATE), Constants.DEX_NAME);
            InputStream input = null;
            try {
                input = context.getAssets().open(Constants.DEX_NAME);
                byte[] bytes = FileUtils.readBytes(input);
                if (bytes != null && bytes.length > 0) {
                    bytes = EncryptUtils.decrypt(bytes);
                    boolean flag = FileUtils.writeByteFile(dexInternalStoragePath.getPath(), bytes);
                    if (flag) {
                        AppLog.e("jar", " getParseUrlJarAssets 复制成功");
                        return true;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


}
