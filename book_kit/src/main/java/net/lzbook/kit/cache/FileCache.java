package net.lzbook.kit.cache;


import net.lzbook.kit.utils.MD5Utils;

public class FileCache {

    public enum CacheType {
        NO_EXPIRED(""), DEFAULT(".cache"), IMAGE_COVER(".image_cover");

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
