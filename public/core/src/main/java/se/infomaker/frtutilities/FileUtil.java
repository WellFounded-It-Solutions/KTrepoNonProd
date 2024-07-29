package se.infomaker.frtutilities;

import android.content.Context;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import timber.log.Timber;

public class FileUtil {

    @Nullable
    public static String loadJSONFromAssets(Context context, String filename) {
        return loadJSONFromAssets(context, filename, null);
    }

    @Nullable
    public static String loadJSONFromAssets(Context context, String filename, OnErrorListener listener) {
        InputStream is = null;
        try {
            is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            //noinspection ResultOfMethodCallIgnored
            is.read(buffer);
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            if (listener != null) {
                listener.onError(ex);
            }
            else {
                Timber.e(ex);
            }
            return null;
        } finally {
            IOUtils.safeClose(is);
        }
    }

    public interface OnErrorListener {
        void onError(IOException e);
    }
}
