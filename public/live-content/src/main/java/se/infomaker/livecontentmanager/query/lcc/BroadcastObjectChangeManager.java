package se.infomaker.livecontentmanager.query.lcc;

import com.jakewharton.rxrelay2.PublishRelay;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import se.infomaker.livecontentmanager.query.lcc.infocaster.InfocasterConnection;
import se.infomaker.livecontentmanager.query.lcc.infocaster.Status;
import timber.log.Timber;

@Singleton
public class BroadcastObjectChangeManager {

    public static final String UUID = "uuid";

    private final InfocasterConnection connection;
    private final String managerUUID = java.util.UUID.randomUUID().toString();
    private PublishRelay<String> relay = PublishRelay.create();
    private HashMap<String, AtomicInteger> refCount = new HashMap<>();

    @Inject
    BroadcastObjectChangeManager(InfocasterConnection connection) {
        this.connection = connection;
        // Make sure we setup all things we are interested of when we are reconnected
        connection.getStatus().filter(Status::isConnected).subscribe(status -> {
                    if (refCount.size() > 0) {
                        resubscribe();
                    }
                }
        , throwable -> Timber.e(throwable, "Failed to get isConnected event"));
        // Relay broadcast publish events
        connection.getBroadcastEvents()
                .filter(publishEvent -> {
                    return refCount.keySet().contains(publishEvent.getUUID());
                })
                .subscribe(publishEvent -> relay.accept(publishEvent.getUUID()),
                        throwable -> Timber.e(throwable, "Failed to relay event"));
    }

    private void resubscribe() {
        Observable.fromIterable(refCount.keySet()).map((Function<String, Map<String, String>>) uuid -> {
            HashMap<String, String> map = new HashMap<>();
            map.put(UUID, uuid);
            return map;
        }).toList().observeOn(AndroidSchedulers.mainThread()).subscribe(filter -> {
            connection.subscribeBroadcast(managerUUID, filter);
        }, throwable -> Timber.e(throwable, "Failed to resubscribe"));
    }

    public synchronized void subscribe(Set<String> uuids) {
        // increase reference count on all and subscribe to all new
        Observable.fromIterable(uuids)
                .filter(this::retain).map((Function<String, Map<String, String>>) uuid -> {
            HashMap<String, String> map = new HashMap<>();
            map.put(UUID, uuid);
            return map;
        }).toList().filter(maps -> !maps.isEmpty()).subscribe(filter -> {
            connection.subscribeBroadcast(managerUUID, filter);
        }, throwable -> Timber.e(throwable, "Failed to subscribe"));
    }

    public synchronized void unsubscribe(Set<String> uuids) {
        Observable.fromIterable(uuids)
                .filter(this::release).map((Function<String, Map<String, String>>) uuid -> {
            HashMap<String, String> map = new HashMap<>();
            map.put(UUID, uuid);
            return map;
        }).toList().filter(maps -> !maps.isEmpty()).subscribe(filter -> {
            if (refCount.size() == 0) {
                connection.close(managerUUID);
            }
            else {
                connection.unsubscribeBroadcast(filter);
            }
        }, throwable -> Timber.e(throwable, "Failed to unsubscribe"));
    }

    /**
     * @param uuid
     * @return true if this is the first time the managerUUID is retained
     */
    private synchronized boolean retain(String uuid) {
        if (refCount.containsKey(uuid)) {
            refCount.get(uuid).incrementAndGet();
            return false;
        }
        refCount.put(uuid, new AtomicInteger(1));
        return true;
    }

    /**
     * @param uuid
     * @return true if the managerUUID should no longer be tracked
     */
    private synchronized boolean release(String uuid) {
        AtomicInteger value = this.refCount.get(uuid);
        if (value != null) {
            int updated = value.decrementAndGet();
            if (updated == 0) {
                refCount.remove(uuid);
                return true;
            }
        }
        return false;
    }

    public Observable<String> observe() {
        return relay;
    }

    public Observable<String> observeWithFilter(Set<String> uuids) {
        return relay.filter(uuids::contains);
    }
}
