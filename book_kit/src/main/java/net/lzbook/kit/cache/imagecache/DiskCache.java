package net.lzbook.kit.cache.imagecache;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import com.android.volley.toolbox.ImageLoader.ImageCache;

import net.lzbook.kit.cache.FileCache;
import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.utils.FileUtils;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DiskCache implements ImageCache {

    private FileCache.CacheType cacheType = FileCache.CacheType.IMAGE_COVER;
    private static CompressFormat DISK_IMAGECACHE_COMPRESS_FORMAT = CompressFormat.JPEG;
    private String cachePath = ReplaceConstants.getReplaceConstants().APP_PATH_IMAGE;

    public DiskCache(String path) {
        cachePath = path;
    }

    @Override
    public Bitmap getBitmap(String url) {
        String filePath = cachePath + FileCache.decodeKey(url) + cacheType.extension;
        Bitmap bitmap = null;
        try {
            byte[] data = FileUtils.readBytes(filePath);
            if (data != null && data.length > 0) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                options.inJustDecodeBounds = true;
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            System.gc();
            System.gc();
        }
        return bitmap;
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        String imgPath = cachePath.substring(0, cachePath.length() - 1);
        FileUtils.createFolderIfNotExist(imgPath);
        String filePath = cachePath + FileCache.decodeKey(url) + cacheType.extension;
        OutputStream out = null;
        int IO_BUFFER_SIZE = 8 * 1024;
        int DISK_IMAGECACHE_QUALITY = 100;
        try {
            out = new BufferedOutputStream(new FileOutputStream(filePath), IO_BUFFER_SIZE);
            bitmap.compress(DISK_IMAGECACHE_COMPRESS_FORMAT, DISK_IMAGECACHE_QUALITY, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

}
