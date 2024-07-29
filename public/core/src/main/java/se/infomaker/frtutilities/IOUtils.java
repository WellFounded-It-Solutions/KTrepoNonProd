package se.infomaker.frtutilities;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class IOUtils {
    public static void safeClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // do nothing
            }
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static void closeQuietly(Closeable inputStream){
        safeClose(inputStream);
    }

    /**
     * Read asset file as string from provided resource manager
     * @param resourceManager to use
     * @param file to read
     * @return the string read from file
     * @throws IOException if failing to read the file
     */
    public static String readAssetString(ResourceManager resourceManager, String file) throws IOException {
        StringBuilder buf = new StringBuilder();
        InputStream inputStream = null;
        BufferedReader in = null;
        try {
            inputStream = resourceManager.getAssetStream(file);
            in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String str;

            while ((str = in.readLine()) != null) {
                buf.append(str);
            }
            return buf.toString();
        } finally {
            closeQuietly(inputStream);
            closeQuietly(in);
        }
    }
}
