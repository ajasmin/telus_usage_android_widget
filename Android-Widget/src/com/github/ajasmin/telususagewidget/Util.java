package com.github.ajasmin.telususagewidget;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Util {
    public static byte[] readStream(InputStream in) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int count;
        while ((count = in.read(buf)) > 0)
                byteArrayOutputStream.write(buf, 0, count);
        in.close();
        return byteArrayOutputStream.toByteArray();
    }
}
