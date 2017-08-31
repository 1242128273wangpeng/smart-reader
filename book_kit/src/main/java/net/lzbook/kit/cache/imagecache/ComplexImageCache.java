package net.lzbook.kit.cache.imagecache;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.android.volley.toolbox.ImageLoader.ImageCache;

public class ComplexImageCache implements ImageCache {

	private ImageCache mOneImageCache;
	private ImageCache mTwoImageCache;
	private static final String URL_FLAG = "#W0#H0";

	public ComplexImageCache(int maxSize, String path) {
		mOneImageCache = new BitmapLruImageCache(maxSize);
		mTwoImageCache = new DiskCache(path);
	}

	@Override
	public Bitmap getBitmap(String url) {
		if (TextUtils.isEmpty(url)) {
			return null;
		}
		
		url = url.replaceFirst(URL_FLAG, "");
		Bitmap bitmap = null;
		if (mOneImageCache != null) {
			bitmap = mOneImageCache.getBitmap(url);
		}
		if (bitmap == null && mTwoImageCache != null) {
			bitmap = mTwoImageCache.getBitmap(url);
			if (bitmap != null && mOneImageCache != null) {
				mOneImageCache.putBitmap(url, bitmap);
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
		if (mOneImageCache != null) {
			mOneImageCache.putBitmap(url, bitmap);
		}

		if (mTwoImageCache != null) {
			mTwoImageCache.putBitmap(url, bitmap);
		}
	}
}
