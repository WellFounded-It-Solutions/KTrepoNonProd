package se.infomaker.frt.ui.fragment;

import android.os.Handler;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import se.infomaker.storagemodule.model.Subscription;

/**
 * Mutes realm update notifications to allow adapter update positions internally
 */
public abstract class MutableSubscriptionListener implements RealmChangeListener<RealmResults<Subscription>> {

    private boolean muted;
    private final Handler handler;

    public MutableSubscriptionListener() {
        handler = new Handler();
    }

    @Override
    public void onChange(RealmResults<Subscription> element) {
        if (muted) {
            return;
        }
        onChange();
    }

    void mute() {
        muted = true;
    }

    void unmute() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                muted = false;
            }
        }, 16);

    }

    public abstract void onChange();
}
