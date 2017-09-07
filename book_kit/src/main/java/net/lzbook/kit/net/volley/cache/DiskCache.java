package net.lzbook.kit.net.volley.cache;

import com.android.volley.toolbox.ImageLoader.ImageCache;

import net.lzbook.kit.utils.FileUtils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 缓存至磁盘
 **/
public class DiskCache implements ImageCache {

    //缓存文件格式
    private static CompressFormat DISK_CACHE_COMPRESS_FORMAT = CompressFormat.JPEG;
    //缓存文件类型
    private CacheHelp.CacheType cache_type = CacheHelp.CacheType.IMAGE_COVER;
    //缓存路径
    private String cache_path = "/image/";

    public DiskCache(String cache_path) {
        this.cache_path = cache_path;
    }

    @Override
    public Bitmap getBitmap(String url) {
        String file_path = cache_path + CacheHelp.decodeKey(url) + cache_type.extension;
        Bitmap bitmap = null;
        try {
            byte[] bytes = FileUtils.readBytes(file_path);
            if (bytes != null && bytes.length > 0) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                options.inJustDecodeBounds = true;
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return bitmap;
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        String file_path = cache_path + CacheHelp.decodeKey(url) + cache_type.extension;
        OutputStream outputStream = null;
        int IO_BUFFER_SIZE = 8 * 1024;
        int DISK_CACHE_QUALITY = 100;

        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(file_path), IO_BUFFER_SIZE);
            bitmap.compress(DISK_CACHE_COMPRESS_FORMAT, DISK_CACHE_QUALITY, outputStream);


        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }
}
