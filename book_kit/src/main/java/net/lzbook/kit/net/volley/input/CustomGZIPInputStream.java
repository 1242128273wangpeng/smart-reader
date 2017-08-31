package net.lzbook.kit.net.volley.input;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.zip.GZIPInputStream;

/**
 * 自定义的GZIPInputStream，主要用于读IEncoding为GZip方式的InputStream
 **/
public class CustomGZIPInputStream extends GZIPInputStream {

    private CustomGZIPInputStream parent;
    private CustomGZIPInputStream child;
    private int size;
    private boolean eos;

    public CustomGZIPInputStream(InputStream inputStream) throws IOException {
        super(new PushbackInputStream(inputStream, 1024));
        this.size = -1;
    }

    private CustomGZIPInputStream(CustomGZIPInputStream parent) throws IOException {
        super(parent.in);
        this.size = -1;
        this.parent = parent.parent == null ? parent : parent.parent;
        this.parent.child = this;
    }

    private CustomGZIPInputStream(CustomGZIPInputStream parent, int size) throws IOException {
        super(parent.in, size);
        this.size = size;
        this.parent = parent.parent == null ? parent : parent.parent;
        this.parent.child = this;
    }

    public int read(byte[] inputBuffer, int inputBufferOffset, int inputBufferLen) throws IOException {
        if (eos) {
            return -1;
        }
        if (this.child != null) {
            return this.child.read(inputBuffer, inputBufferOffset, inputBufferLen);
        }
        int index = super.read(inputBuffer, inputBufferOffset, inputBufferLen);
        if (index == -1) {
            int n = inf.getRemaining() - 8;
            if (n > 0) {
                ((PushbackInputStream) this.in).unread(buf, len - n, n);
            } else {
                byte[] b = new byte[1];
                int ret = in.read(b, 0, 1);
                if (ret == -1) {
                    eos = true;
                    return -1;
                } else
                    ((PushbackInputStream) this.in).unread(b, 0, 1);
            }
            CustomGZIPInputStream child;
            if (this.size == -1) {
                child = new CustomGZIPInputStream(this);
            } else {
                child = new CustomGZIPInputStream(this, this.size);
            }
            int result = child.read(inputBuffer, inputBufferOffset, inputBufferLen);
            child.close();
            return result;
        } else
            return index;
    }
}
