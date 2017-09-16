package net.lzbook.kit.cache.imagecache;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.net.volley.VolleyRequestManager;

import android.content.Context;

public class ImageCacheManager {

    /**
     * Volley recommends in-memory L1 cache but both a disk and memory cache are provided. Volley
     * includes a L2 disk cache out of the box but you can technically use a disk cache as an L1
     * cache provided you can live with potential i/o blocking.
     */
    private static ImageCacheManager mInstance;
    /**
     * Volley image loader
     */
    private ImageLoader mImageLoader;

    /**
     * @return instance of the cache manager
     */
    public static ImageCacheManager getInstance() {
        if (mInstance == null) {
            mInstance = new ImageCacheManager();
            int DISK_IMAGECACHE_SIZE = 1024 * 1024;
            mInstance.init(BaseBookApplication.getGlobalContext(), ReplaceConstants.getReplaceConstants().APP_PATH_IMAGE, DISK_IMAGECACHE_SIZE, ImageCacheManager
                    .ImageCacheType.COMPLEX);
        }

        return mInstance;
    }

    /**
     * Initializer for the manager. Must be called prior to use.
     *
     * context        application context
     * path           name for the cache location
     * cacheSize      max size for the cache
     * compressFormat file type compression format.
     * quality
     */
    public void init(Context context, String path, int cacheSize, ImageCacheType type) {
        /**
         * Image cache implementation
         */
        ImageCache mImageCache;
        switch (type) {
            case DISK:
                mImageCache = new DiskCache(path);
                break;
            case MEMORY:
                mImageCache = new BitmapLruImageCache(cacheSize);
                break;
            case COMPLEX:
                mImageCache = new ComplexImageCache(cacheSize, path);
                break;
            default:
                mImageCache = new BitmapLruImageCache(cacheSize);
                break;
        }
        mImageLoader = new ImageLoader(VolleyRequestManager.getRequestQueue(), mImageCache);
    }

    /**
     * @return instance of the image loader
     */
    public ImageLoader getImageLoader() {
        return mImageLoader;
    }


    public enum ImageCacheType {
        DISK, MEMORY, COMPLEX
    }

}
