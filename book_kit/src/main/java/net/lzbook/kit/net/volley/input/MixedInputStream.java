package net.lzbook.kit.net.volley.input;

import net.lzbook.kit.net.InputStreamHelper;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * 继承自CustomGZIPInputStream，主要用于读IEncoding为GZIP和ESENC方式的InputStream
 **/
public class MixedInputStream extends CustomGZIPInputStream {

    public MixedInputStream(InputStream inputStream) throws IOException {
        super(inputStream);
    }

    @Override
    public int read() throws IOException {
        int index = super.read();
        byte[] b = new byte[]{((byte) index)};
        InputStreamHelper.encrypt(b);
        return b[0];
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
    public int read(byte[] buffer, int offset, int byteCount) throws IOException {
        int index = super.read(buffer, offset, byteCount);
        if (buffer.length > 0) {
            buffer = InputStreamHelper.encrypt(buffer);
        }
        return index;
    }

}
