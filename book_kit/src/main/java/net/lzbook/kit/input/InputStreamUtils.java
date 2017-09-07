package net.lzbook.kit.input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class InputStreamUtils {

    public static String getString(InputStream stream, String charsetName) throws UnsupportedEncodingException {
        BufferedReader in = new BufferedReader(new InputStreamReader(stream, charsetName));

        StringBuffer buffer = new StringBuffer();
        String line = null;
        try {
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }
}
