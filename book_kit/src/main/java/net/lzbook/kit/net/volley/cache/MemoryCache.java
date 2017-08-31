package net.lzbook.kit.net.volley.cache;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.android.volley.toolbox.ImageLoader.ImageCache;

/**
 * 缓存至内存
 **/

public class MemoryCache extends LruCache<String, Bitmap> implements ImageCache {

	private final String TAG = MemoryCache.class.getSimpleName();

	public MemoryCache(int maxSize) {
		super(maxSize);
	}

	@Override
	protected int sizeOf(String key, Bitmap value) {
		return value.getRowBytes() * value.getHeight();
	}

	@Override
	public Bitmap getBitmap(String url) {
		Log.e(TAG, "Retrieved item from Memory Cache " + url);
		return get(url);
	}

	@Override
	public void putBitmap(String url, Bitmap bitmap) {
		Log.e(TAG, "Added item to Memory Cache " + url);
		put(url, bitmap);
	}
}
