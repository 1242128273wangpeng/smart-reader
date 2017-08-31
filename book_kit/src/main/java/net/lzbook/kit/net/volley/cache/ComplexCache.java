package net.lzbook.kit.net.volley.cache;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.android.volley.toolbox.ImageLoader.ImageCache;

/**
 * 两级缓存，缓存至磁盘和内存
 **/
public class ComplexCache implements ImageCache {

    private ImageCache memoryCache;
    private ImageCache diskCache;
    private static final String URL_FLAG = "#W0#H0";

    public ComplexCache(int maxSize, String path) {
        memoryCache = new MemoryCache(maxSize);
        diskCache = new DiskCache(path);
    }

    @Override
    public Bitmap getBitmap(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        url = url.replaceFirst(URL_FLAG, "");
        Bitmap bitmap = null;
        if (memoryCache != null) {
            bitmap = memoryCache.getBitmap(url);
        }
        if (bitmap == null && diskCache != null) {
            bitmap = diskCache.getBitmap(url);
            if (bitmap != null && memoryCache != null) {
                memoryCache.putBitmap(url, bitmap);
            }
        }
        return bitmap;
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        url = url.replaceFirst(URL_FLAG, "");
        if (memoryCache != null) {
            memoryCache.putBitmap(url, bitmap);
        }

        if (diskCache != null) {
            diskCache.putBitmap(url, bitmap);
        }
    }
}
