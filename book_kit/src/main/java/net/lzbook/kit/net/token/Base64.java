//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.lzbook.kit.net.token;

import android.os.Build;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public class Base64 {
    private Base64() {
    }

    public static Base64.Encoder getEncoder() {
        return Base64.Encoder.RFC4648;
    }

    public static Base64.Encoder getUrlEncoder() {
        return Base64.Encoder.RFC4648_URLSAFE;
    }

    public static Base64.Encoder getMimeEncoder() {
        return Base64.Encoder.RFC2045;
    }

    public static Encoder getMimeEncoder(int lineLength, byte[] lineSeparator) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(lineSeparator);
        }
        int[] base64 = Decoder.fromBase64;
        byte[] var3 = lineSeparator;
        int var4 = lineSeparator.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            byte b = var3[var5];
            if (base64[b & 255] != -1) {
                throw new IllegalArgumentException("Illegal base64 line separator character 0x" + Integer.toString(b, 16));
            }
        }

        if (lineLength <= 0) {
            return Encoder.RFC4648;
        } else {
            return new Encoder(false, lineSeparator, lineLength >> 2 << 2, true);
        }
    }

    public static Base64.Decoder getDecoder() {
        return Base64.Decoder.RFC4648;
    }

    public static Base64.Decoder getUrlDecoder() {
        return Base64.Decoder.RFC4648_URLSAFE;
    }

    public static Base64.Decoder getMimeDecoder() {
        return Base64.Decoder.RFC2045;
    }

    private static class DecInputStream extends InputStream {
        private final InputStream is;
        private final boolean isMIME;
        private final int[] base64;
        private int bits = 0;
        private int nextin = 18;
        private int nextout = -8;
        private boolean eof = false;
        private boolean closed = false;
        private byte[] sbBuf = new byte[1];

        DecInputStream(InputStream is, int[] base64, boolean isMIME) {
            this.is = is;
            this.base64 = base64;
            this.isMIME = isMIME;
        }

        public int read() throws IOException {
            return this.read(this.sbBuf, 0, 1) == -1 ? -1 : this.sbBuf[0] & 255;
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (this.closed) {
                throw new IOException("Stream is closed");
            } else if (this.eof && this.nextout < 0) {
                return -1;
            } else if (off >= 0 && len >= 0 && len <= b.length - off) {
                int oldOff = off;
                if (this.nextout >= 0) {
                    do {
                        if (len == 0) {
                            return off - oldOff;
                        }

                        b[off++] = (byte) (this.bits >> this.nextout);
                        --len;
                        this.nextout -= 8;
                    } while (this.nextout >= 0);

                    this.bits = 0;
                }

                while (true) {
                    if (len > 0) {
                        int v = this.is.read();
                        if (v == -1) {
                            this.eof = true;
                            if (this.nextin != 18) {
                                if (this.nextin == 12) {
                                    throw new IOException("Base64 stream has one un-decoded dangling byte.");
                                }

                                b[off++] = (byte) (this.bits >> 16);
                                --len;
                                if (this.nextin == 0) {
                                    if (len == 0) {
                                        this.bits >>= 8;
                                        this.nextout = 0;
                                    } else {
                                        b[off++] = (byte) (this.bits >> 8);
                                    }
                                }
                            }

                            if (off == oldOff) {
                                return -1;
                            }

                            return off - oldOff;
                        }

                        if (v != 61) {
                            if ((v = this.base64[v]) == -1) {
                                if (!this.isMIME) {
                                    throw new IOException("Illegal base64 character " + Integer.toString(v, 16));
                                }
                                continue;
                            }

                            this.bits |= v << this.nextin;
                            if (this.nextin != 0) {
                                this.nextin -= 6;
                                continue;
                            }

                            this.nextin = 18;
                            this.nextout = 16;

                            while (this.nextout >= 0) {
                                b[off++] = (byte) (this.bits >> this.nextout);
                                --len;
                                this.nextout -= 8;
                                if (len == 0 && this.nextout >= 0) {
                                    return off - oldOff;
                                }
                            }

                            this.bits = 0;
                            continue;
                        }

                        if (this.nextin == 18 || this.nextin == 12 || this.nextin == 6 && this.is.read() != 61) {
                            throw new IOException("Illegal base64 ending sequence:" + this.nextin);
                        }

                        b[off++] = (byte) (this.bits >> 16);
                        --len;
                        if (this.nextin == 0) {
                            if (len == 0) {
                                this.bits >>= 8;
                                this.nextout = 0;
                            } else {
                                b[off++] = (byte) (this.bits >> 8);
                            }
                        }

                        this.eof = true;
                    }

                    return off - oldOff;
                }
            } else {
                throw new IndexOutOfBoundsException();
            }
        }

        public int available() throws IOException {
            if (this.closed) {
                throw new IOException("Stream is closed");
            } else {
                return this.is.available();
            }
        }

        public void close() throws IOException {
            if (!this.closed) {
                this.closed = true;
                this.is.close();
            }

        }
    }

    private static class EncOutputStream extends FilterOutputStream {
        private int leftover = 0;
        private int b0;
        private int b1;
        private int b2;
        private boolean closed = false;
        private final char[] base64;
        private final byte[] newline;
        private final int linemax;
        private final boolean doPadding;
        private int linepos = 0;

        EncOutputStream(OutputStream os, char[] base64, byte[] newline, int linemax, boolean doPadding) {
            super(os);
            this.base64 = base64;
            this.newline = newline;
            this.linemax = linemax;
            this.doPadding = doPadding;
        }

        public void write(int b) throws IOException {
            byte[] buf = new byte[]{(byte) (b & 255)};
            this.write(buf, 0, 1);
        }

        private void checkNewline() throws IOException {
            if (this.linepos == this.linemax) {
                this.out.write(this.newline);
                this.linepos = 0;
            }

        }

        public void write(byte[] b, int off, int len) throws IOException {
            if (this.closed) {
                throw new IOException("Stream is closed");
            } else if (off >= 0 && len >= 0 && off + len <= b.length) {
                if (len != 0) {
                    if (this.leftover != 0) {
                        if (this.leftover == 1) {
                            this.b1 = b[off++] & 255;
                            --len;
                            if (len == 0) {
                                ++this.leftover;
                                return;
                            }
                        }

                        this.b2 = b[off++] & 255;
                        --len;
                        this.checkNewline();
                        this.out.write(this.base64[this.b0 >> 2]);
                        this.out.write(this.base64[this.b0 << 4 & 63 | this.b1 >> 4]);
                        this.out.write(this.base64[this.b1 << 2 & 63 | this.b2 >> 6]);
                        this.out.write(this.base64[this.b2 & 63]);
                        this.linepos += 4;
                    }

                    int nBits24 = len / 3;

                    for (this.leftover = len - nBits24 * 3; nBits24-- > 0; this.linepos += 4) {
                        this.checkNewline();
                        int bits = (b[off++] & 255) << 16 | (b[off++] & 255) << 8 | b[off++] & 255;
                        this.out.write(this.base64[bits >>> 18 & 63]);
                        this.out.write(this.base64[bits >>> 12 & 63]);
                        this.out.write(this.base64[bits >>> 6 & 63]);
                        this.out.write(this.base64[bits & 63]);
                    }

                    if (this.leftover == 1) {
                        this.b0 = b[off++] & 255;
                    } else if (this.leftover == 2) {
                        this.b0 = b[off++] & 255;
                        this.b1 = b[off++] & 255;
                    }

                }
            } else {
                throw new ArrayIndexOutOfBoundsException();
            }
        }

        public void close() throws IOException {
            if (!this.closed) {
                this.closed = true;
                if (this.leftover == 1) {
                    this.checkNewline();
                    this.out.write(this.base64[this.b0 >> 2]);
                    this.out.write(this.base64[this.b0 << 4 & 63]);
                    if (this.doPadding) {
                        this.out.write(61);
                        this.out.write(61);
                    }
                } else if (this.leftover == 2) {
                    this.checkNewline();
                    this.out.write(this.base64[this.b0 >> 2]);
                    this.out.write(this.base64[this.b0 << 4 & 63 | this.b1 >> 4]);
                    this.out.write(this.base64[this.b1 << 2 & 63]);
                    if (this.doPadding) {
                        this.out.write(61);
                    }
                }

                this.leftover = 0;
                this.out.close();
            }

        }
    }

    public static class Decoder {
        private final boolean isURL;
        private final boolean isMIME;
        private static final int[] fromBase64 = new int[256];
        private static final int[] fromBase64URL;
        static final Base64.Decoder RFC4648;
        static final Base64.Decoder RFC4648_URLSAFE;
        static final Base64.Decoder RFC2045;

        private Decoder(boolean isURL, boolean isMIME) {
            this.isURL = isURL;
            this.isMIME = isMIME;
        }

        public byte[] decode(byte[] src) {
            byte[] dst = new byte[this.outLength(src, 0, src.length)];
            int ret = this.decode0(src, 0, src.length, dst);
            if (ret != dst.length) {
                dst = Arrays.copyOf(dst, ret);
            }

            return dst;
        }

        public byte[] decode(String src) {
            return this.decode(src.getBytes(StandardCharsets.ISO_8859_1));
        }

        public int decode(byte[] src, byte[] dst) {
            int len = this.outLength(src, 0, src.length);
            if (dst.length < len) {
                throw new IllegalArgumentException("Output byte array is too small for decoding all input bytes");
            } else {
                return this.decode0(src, 0, src.length, dst);
            }
        }

        public ByteBuffer decode(ByteBuffer buffer) {
            int pos0 = buffer.position();

            try {
                byte[] iae;
                int sp;
                int sl;
                if (buffer.hasArray()) {
                    iae = buffer.array();
                    sp = buffer.arrayOffset() + buffer.position();
                    sl = buffer.arrayOffset() + buffer.limit();
                    buffer.position(buffer.limit());
                } else {
                    iae = new byte[buffer.remaining()];
                    buffer.get(iae);
                    sp = 0;
                    sl = iae.length;
                }

                byte[] dst = new byte[this.outLength(iae, sp, sl)];
                return ByteBuffer.wrap(dst, 0, this.decode0(iae, sp, sl, dst));
            } catch (IllegalArgumentException var7) {
                buffer.position(pos0);
                throw var7;
            }
        }

        public InputStream wrap(InputStream is) {
            Objects.requireNonNull(is);
            return new Base64.DecInputStream(is, this.isURL ? fromBase64URL : fromBase64, this.isMIME);
        }

        private int outLength(byte[] src, int sp, int sl) {
            int[] base64 = this.isURL ? fromBase64URL : fromBase64;
            int paddings = 0;
            int len = sl - sp;
            if (len == 0) {
                return 0;
            } else if (len < 2) {
                if (this.isMIME && base64[0] == -1) {
                    return 0;
                } else {
                    throw new IllegalArgumentException("Input byte[] should at least have 2 bytes for base64 bytes");
                }
            } else {
                if (this.isMIME) {
                    int n = 0;

                    while (sp < sl) {
                        int b = src[sp++] & 255;
                        if (b == 61) {
                            len -= sl - sp + 1;
                            break;
                        }

                        if (base64[b] == -1) {
                            ++n;
                        }
                    }

                    len -= n;
                } else if (src[sl - 1] == 61) {
                    ++paddings;
                    if (src[sl - 2] == 61) {
                        ++paddings;
                    }
                }

                if (paddings == 0 && (len & 3) != 0) {
                    paddings = 4 - (len & 3);
                }

                return 3 * ((len + 3) / 4) - paddings;
            }
        }

        private int decode0(byte[] src, int sp, int sl, byte[] dst) {
            int[] base64 = this.isURL ? fromBase64URL : fromBase64;
            int dp = 0;
            int bits = 0;
            int shiftto = 18;

            while (sp < sl) {
                int b = src[sp++] & 255;
                if ((b = base64[b]) < 0) {
                    if (b == -2) {
                        if ((shiftto != 6 || sp != sl && src[sp++] == 61) && shiftto != 18) {
                            break;
                        }

                        throw new IllegalArgumentException("Input byte array has wrong 4-byte ending unit");
                    }

                    if (!this.isMIME) {
                        throw new IllegalArgumentException("Illegal base64 character " + Integer.toString(src[sp - 1], 16));
                    }
                } else {
                    bits |= b << shiftto;
                    shiftto -= 6;
                    if (shiftto < 0) {
                        dst[dp++] = (byte) (bits >> 16);
                        dst[dp++] = (byte) (bits >> 8);
                        dst[dp++] = (byte) bits;
                        shiftto = 18;
                        bits = 0;
                    }
                }
            }

            if (shiftto == 6) {
                dst[dp++] = (byte) (bits >> 16);
            } else if (shiftto == 0) {
                dst[dp++] = (byte) (bits >> 16);
                dst[dp++] = (byte) (bits >> 8);
            } else if (shiftto == 12) {
                throw new IllegalArgumentException("Last unit does not have enough valid bits");
            }

            do {
                if (sp >= sl) {
                    return dp;
                }
            } while (this.isMIME && base64[src[sp++]] < 0);

            throw new IllegalArgumentException("Input byte array has incorrect ending byte at " + sp);
        }

        static {
            Arrays.fill(fromBase64, -1);

            int i;
            for (i = 0; i < Base64.Encoder.toBase64.length; fromBase64[Base64.Encoder.toBase64[i]] = i++) {
                ;
            }

            fromBase64[61] = -2;
            fromBase64URL = new int[256];
            Arrays.fill(fromBase64URL, -1);

            for (i = 0; i < Base64.Encoder.toBase64URL.length; fromBase64URL[Base64.Encoder.toBase64URL[i]] = i++) {
                ;
            }

            fromBase64URL[61] = -2;
            RFC4648 = new Base64.Decoder(false, false);
            RFC4648_URLSAFE = new Base64.Decoder(true, false);
            RFC2045 = new Base64.Decoder(false, true);
        }
    }

    public static class Encoder {
        private final byte[] newline;
        private final int linemax;
        private final boolean isURL;
        private final boolean doPadding;
        private static final char[] toBase64 = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
        private static final char[] toBase64URL = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};
        private static final int MIMELINEMAX = 76;
        private static final byte[] CRLF = new byte[]{13, 10};
        static final Base64.Encoder RFC4648 = new Base64.Encoder(false, (byte[]) null, -1, true);
        static final Base64.Encoder RFC4648_URLSAFE = new Base64.Encoder(true, (byte[]) null, -1, true);
        static final Base64.Encoder RFC2045;

        private Encoder(boolean isURL, byte[] newline, int linemax, boolean doPadding) {
            this.isURL = isURL;
            this.newline = newline;
            this.linemax = linemax;
            this.doPadding = doPadding;
        }

        private final int outLength(int srclen) {
            boolean len = false;
            int len1;
            if (this.doPadding) {
                len1 = 4 * ((srclen + 2) / 3);
            } else {
                int n = srclen % 3;
                len1 = 4 * (srclen / 3) + (n == 0 ? 0 : n + 1);
            }

            if (this.linemax > 0) {
                len1 += (len1 - 1) / this.linemax * this.newline.length;
            }

            return len1;
        }

        public byte[] encode(byte[] src) {
            int len = this.outLength(src.length);
            byte[] dst = new byte[len];
            int ret = this.encode0(src, 0, src.length, dst);
            return ret != dst.length ? Arrays.copyOf(dst, ret) : dst;
        }

        public int encode(byte[] src, byte[] dst) {
            int len = this.outLength(src.length);
            if (dst.length < len) {
                throw new IllegalArgumentException("Output byte array is too small for encoding all input bytes");
            } else {
                return this.encode0(src, 0, src.length, dst);
            }
        }

        public String encodeToString(byte[] src) {
            byte[] encoded = this.encode(src);
            return new String(encoded, 0, 0, encoded.length);
        }

        public ByteBuffer encode(ByteBuffer buffer) {
            int len = this.outLength(buffer.remaining());
            byte[] dst = new byte[len];
            boolean ret = false;
            int ret1;
            if (buffer.hasArray()) {
                ret1 = this.encode0(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.arrayOffset() + buffer.limit(), dst);
                buffer.position(buffer.limit());
            } else {
                byte[] src = new byte[buffer.remaining()];
                buffer.get(src);
                ret1 = this.encode0(src, 0, src.length, dst);
            }

            if (ret1 != dst.length) {
                dst = Arrays.copyOf(dst, ret1);
            }

            return ByteBuffer.wrap(dst);
        }

        public OutputStream wrap(OutputStream os) {
            Objects.requireNonNull(os);
            return new Base64.EncOutputStream(os, this.isURL ? toBase64URL : toBase64, this.newline, this.linemax, this.doPadding);
        }

        public Base64.Encoder withoutPadding() {
            return !this.doPadding ? this : new Base64.Encoder(this.isURL, this.newline, this.linemax, false);
        }

        private int encode0(byte[] src, int off, int end, byte[] dst) {
            char[] base64 = this.isURL ? toBase64URL : toBase64;
            int sp = off;
            int slen = (end - off) / 3 * 3;
            int sl = off + slen;
            if (this.linemax > 0 && slen > this.linemax / 4 * 3) {
                slen = this.linemax / 4 * 3;
            }

            int dp = 0;

            while (true) {
                int b0;
                int b1;
                int bits;
                do {
                    do {
                        if (sp >= sl) {
                            if (sp < end) {
                                b0 = src[sp++] & 255;
                                dst[dp++] = (byte) base64[b0 >> 2];
                                if (sp == end) {
                                    dst[dp++] = (byte) base64[b0 << 4 & 63];
                                    if (this.doPadding) {
                                        dst[dp++] = 61;
                                        dst[dp++] = 61;
                                    }
                                } else {
                                    b1 = src[sp++] & 255;
                                    dst[dp++] = (byte) base64[b0 << 4 & 63 | b1 >> 4];
                                    dst[dp++] = (byte) base64[b1 << 2 & 63];
                                    if (this.doPadding) {
                                        dst[dp++] = 61;
                                    }
                                }
                            }

                            return dp;
                        }

                        b0 = Math.min(sp + slen, sl);
                        b1 = sp;

                        for (int dp0 = dp; b1 < b0; dst[dp0++] = (byte) base64[bits & 63]) {
                            bits = (src[b1++] & 255) << 16 | (src[b1++] & 255) << 8 | src[b1++] & 255;
                            dst[dp0++] = (byte) base64[bits >>> 18 & 63];
                            dst[dp0++] = (byte) base64[bits >>> 12 & 63];
                            dst[dp0++] = (byte) base64[bits >>> 6 & 63];
                        }

                        b1 = (b0 - sp) / 3 * 4;
                        dp += b1;
                        sp = b0;
                    } while (b1 != this.linemax);
                } while (b0 >= end);

                byte[] var16 = this.newline;
                bits = var16.length;

                for (int var14 = 0; var14 < bits; ++var14) {
                    byte b = var16[var14];
                    dst[dp++] = b;
                }
            }
        }

        static {
            RFC2045 = new Base64.Encoder(false, CRLF, 76, true);
        }
    }
}
