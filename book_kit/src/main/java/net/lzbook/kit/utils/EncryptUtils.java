package net.lzbook.kit.utils;

/**
 * Created by kejun on 2016/9/25.
 */
public class EncryptUtils {

    public static byte[] encrypt(byte[] old) {
        try {
            byte[] end = new byte[old.length + 4];
            end[0] = 'w';
            end[1] = 'o';
            end[2] = 'c';
            end[3] = 'a';
            for (int i = 0; i < old.length; i++) {
                end[i + 4] = (byte) ~(old[i]);
            }
            return end;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return old;
    }

    public static byte[] decrypt(byte[] old) {
        try {
            int len = old.length - 4;
            byte[] dest = new byte[len];
            for (int i = 0; i < len; i++) {
                dest[i] = (byte) ~(old[4 + i]);
            }
            return dest;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return old;
    }

}
