package se.infomaker.frt.statistics;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tagmanager.ContainerHolder;
import com.google.android.gms.tagmanager.DataLayer;
import com.google.android.gms.tagmanager.TagManager;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 * Created by Magnus Ekström on 10/03/16.
 */
public class LegacyGoogleTagManagerImpl implements StatisticsManager.StatisticsService, ResultCallback<ContainerHolder>, ContainerHolder.ContainerAvailableListener {

    private static final String TAG = "LegacyGoogleTagManagerImpl";

    private ContainerHolder mContainerHolder;
    private DataLayer mDataLayer;
    private Queue<StatisticsEvent> mEventQueue = new ConcurrentLinkedQueue<>();
    private String containerId;

    @Override
    public String getIdentifier() {
        return "GoogleTagManager:" + containerId;
    }

    @Override
    public void init(Context context, Map<String, Object> config) {
        if (config != null && config.containsKey("containerId")) {
            Object containerIdObject = config.get("containerId");
            if (containerIdObject instanceof String) {
                containerId = (String) containerIdObject;
            } else {
                containerId = (String) ((Map<String, Object>) containerIdObject).get("android");
            }
        } else {
            Timber.w("No container id found in config");
            return;
        }

        TagManager tagManager = TagManager.getInstance(context);
        tagManager.setVerboseLoggingEnabled(true);

        mDataLayer = tagManager.getDataLayer();

        int identifier = context.getResources().getIdentifier("default_container", "raw", context.getPackageName());
        if (identifier > 0) {
            PendingResult<ContainerHolder> pending = tagManager.loadContainerPreferNonDefault(containerId, identifier);
            pending.setResultCallback(this, 2, TimeUnit.SECONDS);
        } else {
            Timber.w("No default container found for id %s", containerId);
        }
    }

    @Override
    public void logEvent(StatisticsEvent event) {
        Timber.d("Containerholder: %s", mContainerHolder);
        if (mContainerHolder == null) {
            mEventQueue.offer(event);
            return;
        }
        push(event);
    }

    private void push(StatisticsEvent event) {
        if (mDataLayer != null) {
            Timber.d("pushing event: " + event.getEventName() + " : " + event.getAttributes());
            mDataLayer.pushEvent(event.getEventName(), event.getAttributes());
        }
    }

    @Override
    public void onResult(@NonNull ContainerHolder containerHolder) {
        Timber.d("In onResult");
        mContainerHolder = containerHolder;
        containerHolder.setContainerAvailableListener(this);

        StatisticsEvent event;
        while ((event = mEventQueue.poll()) != null) {
            Timber.d("Event queue");
            push(event);
        }
    }

    @Override
    public void onContainerAvailable(ContainerHolder containerHolder, String s) {
    }

    @Override
    public void globalAttributesUpdated(@NonNull Map<String, Object> globalAttributes) {
        // NOP
    }
}
