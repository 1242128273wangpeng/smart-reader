package net.lzbook.kit.net;

import net.lzbook.kit.net.volley.input.CustomBufferedInputStream;
import net.lzbook.kit.net.volley.input.CustomGZIPInputStream;
import net.lzbook.kit.net.volley.input.MixedInputStream;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class InputStreamHelper {

    public enum IEncoding {
        NONE(""), ESENC("esenc"), GZIP("gzip"), ESENCGZIP("gzip,esenc");

        public final String encoding;

        IEncoding(String encoding) {
            this.encoding = encoding;
        }
    }

    public static byte[] encrypt(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) ~bytes[i];
        }
        return bytes;
    }

    private static String getEncoding(Map<String, String> responseHeaders) {
        String accept = responseHeaders.get("Accept-Encoding");
        String content = responseHeaders.get("Content-Encoding");
        StringBuilder encoding = new StringBuilder();
        if (accept != null) {
            encoding.append(accept);
        }

        if (content != null) {
            encoding.append(content);
        }
        return encoding.toString();
    }


    public static InputStream getInputStream(Map<String, String> responseHeaders, InputStream inputStream) throws IOException {
        return getInputStream(getEncoding(responseHeaders), inputStream);
    }

    public static InputStream getInputStream(String encoding, InputStream inputStream) throws IOException {

        if (TextUtils.isEmpty(encoding)) {
            return inputStream;
        }

        if (encoding.contains(IEncoding.GZIP.encoding) && encoding.contains(IEncoding.ESENC.encoding)) {
            return new MixedInputStream(inputStream);
        }

        if (encoding.contains(IEncoding.GZIP.encoding)) {
            return new CustomGZIPInputStream(inputStream);
        }

        if (encoding.contains(IEncoding.ESENC.encoding)) {
            return new CustomBufferedInputStream(inputStream);
        }

        return inputStream;
    }

    public static String getString(InputStream stream, String charsetName) throws UnsupportedEncodingException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, charsetName));

        StringBuffer stringBuffer = new StringBuffer();
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuffer.toString();
    }
}
