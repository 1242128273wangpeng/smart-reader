package net.lzbook.kit.net.volley.cache;

import net.lzbook.kit.utils.MD5Utils;

/**
 * 缓存帮助类
 **/
public class CacheHelp {
	private static String TAG = CacheHelp.class.getSimpleName();

	public enum CacheType {

		IMAGE_COVER(".image_cover");

		public final String extension;

		CacheType(String extension) {
			this.extension = extension;
		}

	}

	public static String decodeKey(String url) {
		if (url != null) {
			return MD5Utils.encodeMD5String(url);
		}
		return null;
	}
}
