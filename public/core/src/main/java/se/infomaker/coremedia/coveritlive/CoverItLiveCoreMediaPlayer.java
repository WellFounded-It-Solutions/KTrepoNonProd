package se.infomaker.coremedia.coveritlive;

import android.content.Context;
import android.content.Intent;

import java.util.Map;

import se.infomaker.coremedia.CoreMediaObject;
import se.infomaker.coremedia.CoreMediaPlayer;
import timber.log.Timber;

/**
 * Created by Magnus Ekström on 22/02/16.
 */
public class CoverItLiveCoreMediaPlayer implements CoreMediaPlayer {
    private static final String TAG = "CoverItLiveCMP";
    private static final String NAME = "coveritlive";

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
        Intent intent = new Intent(context, CoverItLivePlayerActivity.class);
        intent.putExtra("id", (String) mCoreMediaObject.getAttributes().get("id"));
        intent.putExtra("color", mColor);
        context.startActivity(intent);
    }
}
