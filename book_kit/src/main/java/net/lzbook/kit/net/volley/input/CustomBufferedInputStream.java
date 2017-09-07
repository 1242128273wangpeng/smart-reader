package net.lzbook.kit.net.volley.input;

import net.lzbook.kit.net.InputStreamHelper;

import android.support.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 自定义的BufferedInputStream，主要用于读IEncoding为EXENC方式的InputStream
 **/
public class CustomBufferedInputStream extends BufferedInputStream {

    public CustomBufferedInputStream(InputStream inputStream) {
        super(inputStream);
    }

    @Override
    public int read() throws IOException {
        int index = super.read();
        byte[] bytes = new byte[]{((byte) index)};
        InputStreamHelper.encrypt(bytes);
        return bytes[0];
    }

    @Override
    public int read(@NonNull byte[] buffer) throws IOException {
        int index = super.read(buffer);
        if (buffer.length > 0) {
            buffer = InputStreamHelper.encrypt(buffer);
        }
        return index;
    }

    @Override
    public int read(@NonNull byte[] buffer, int offset, int byteCount) throws IOException {
        int index = super.read(buffer, offset, byteCount);
        if (buffer.length > 0) {
            buffer = InputStreamHelper.encrypt(buffer);
        }
        return index;
    }

}
