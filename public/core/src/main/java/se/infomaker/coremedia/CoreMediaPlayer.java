package se.infomaker.coremedia;

import android.content.Context;

import java.util.Map;

/**
 * Created by Magnus Ekström on 22/02/16.
 */
public interface CoreMediaPlayer {
    String getName();

    void init(CoreMediaObject coreMediaObject, int color, Map<String, Object> config);

    void start(Context context);
}
