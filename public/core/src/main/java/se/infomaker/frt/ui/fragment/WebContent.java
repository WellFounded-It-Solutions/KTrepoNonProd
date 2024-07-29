package se.infomaker.frt.ui.fragment;

import android.content.Context;
import android.content.Intent;

public class WebContent {

    /**
     * Open a new activity
     * @param context
     * @param url to load
     * @param moduleId to use configuration for
     */
    public static void open(Context context, String url, String moduleId) {
        open(context, url, moduleId, false);
    }

    public static void open(Context context, String url, String moduleId, boolean autoplay) {
        Intent intent = new Intent(context, WebContentActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("moduleId", moduleId);
        intent.putExtra("autoplay", autoplay);
        context.startActivity(intent);
    }
}
