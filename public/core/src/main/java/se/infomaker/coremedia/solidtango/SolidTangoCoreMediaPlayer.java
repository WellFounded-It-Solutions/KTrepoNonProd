package se.infomaker.coremedia.solidtango;

import android.content.Context;
import android.content.Intent;

import java.util.Map;

import se.infomaker.coremedia.CoreMediaObject;
import se.infomaker.coremedia.CoreMediaPlayer;
import timber.log.Timber;

/**
 * Created by Magnus Ekström on 22/02/16.
 */
public class SolidTangoCoreMediaPlayer implements CoreMediaPlayer {
    private static final String TAG = "SolidTangoCMP";
    private static final String NAME = "solidtango";

    private CoreMediaObject mCoreMediaObject;
    private int mColor;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void init(CoreMediaObject coreMediaObject, int color, Map<String, Object> config) {
        Timber.d("init()");
        mCoreMediaObject = coreMediaObject;
        mColor = color;
    }

    @Override
    public void start(Context context) {
        Timber.d("start()");
        Intent intent = new Intent(context, SolidTangoPlayerActivity.class);
        intent.putExtra("uri", (String) mCoreMediaObject.getAttributes().get("uri"));
        intent.putExtra("color", mColor);
        context.startActivity(intent);
    }
}
