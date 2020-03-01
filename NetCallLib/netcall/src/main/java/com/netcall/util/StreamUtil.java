package com.netcall.util;

import java.io.Closeable;
import java.io.IOException;

public class StreamUtil {

    public static void closeStream(Closeable stream) {
        if (stream == null) {
            return;
        }
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void closeStreams(Closeable[] streams) {
        if (streams == null) {
            return;
        }
        for (Closeable stream : streams) {
            closeStream(stream);
        }
    }
}
