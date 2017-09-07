package net.lzbook.kit.net.volley;

import com.android.volley.toolbox.ImageLoader;

import net.lzbook.kit.net.volley.cache.ComplexCache;
import net.lzbook.kit.net.volley.cache.DiskCache;
import net.lzbook.kit.net.volley.cache.MemoryCache;

/**
 * 自定义的Volley的三级缓存
 **/
public class VolleyCacheManager {
    private static VolleyCacheManager volleyCacheManager;
    private ImageLoader imageLoader;

    public static VolleyCacheManager getInstance() {
        if (volleyCacheManager == null) {
            volleyCacheManager = new VolleyCacheManager();
        }

        return volleyCacheManager;
    }

    public void init(String cache_path, int cache_size, ImageCacheType imageCacheType) {
        ImageLoader.ImageCache imageCache;
        switch (imageCacheType) {
            case DISK:
                imageCache = new DiskCache(cache_path);
                break;
            case MEMORY:
                imageCache = new MemoryCache(cache_size);
                break;
            case COMPLEX:
                imageCache = new ComplexCache(cache_size, cache_path);
                break;
            default:
                imageCache = new MemoryCache(cache_size);
                break;
        }

        imageLoader = new ImageLoader(VolleyRequestManager.getRequestQueue(), imageCache);
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public enum ImageCacheType {
        DISK, MEMORY, COMPLEX
    }

}
