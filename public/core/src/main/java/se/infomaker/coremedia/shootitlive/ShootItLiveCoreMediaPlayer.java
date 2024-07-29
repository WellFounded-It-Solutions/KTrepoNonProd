package se.infomaker.coremedia.shootitlive;

import android.content.Context;
import android.content.Intent;

import java.util.Map;

import se.infomaker.coremedia.CoreMediaObject;
import se.infomaker.coremedia.CoreMediaPlayer;
import timber.log.Timber;

/**
 * Created by Magnus Ekström on 22/02/16.
 */
public class ShootItLiveCoreMediaPlayer implements CoreMediaPlayer {
    private static final String TAG = "ShootItLiveCMP";
    private static final String NAME = "shootitlive";

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
        Intent intent = new Intent(context, ShootItLivePlayerActivity.class);
        intent.putExtra("id", (String) mCoreMediaObject.getAttributes().get("id"));
        intent.putExtra("category", (String) mCoreMediaObject.getAttributes().get("category"));
        intent.putExtra("client", (String) mCoreMediaObject.getAttributes().get("client"));
        intent.putExtra("showAds", (String) mCoreMediaObject.getAttributes().get("ads"));
        intent.putExtra("color", mColor);
        context.startActivity(intent);
    }
}
