package se.infomaker.livecontentmanager.query.lcc.querystreamer;

import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import se.infomaker.frtutilities.JSONUtil;
import se.infomaker.livecontentmanager.query.CreateStreamQuery;
import se.infomaker.livecontentmanager.query.DeleteStreamQuery;
import se.infomaker.livecontentmanager.query.Query;
import se.infomaker.livecontentmanager.query.QueryResponseListener;
import se.infomaker.livecontentmanager.query.UpdateStreamQuery;
import se.infomaker.livecontentmanager.query.lcc.infocaster.InfocasterConnection;
import se.infomaker.livecontentmanager.query.lcc.infocaster.Status;
import timber.log.Timber;

@Singleton
public class QueryStreamerManager {
    private final QueryStreamerService queryStreamerService;
    private final Meta meta;
    private final List<Stream> streams = new ArrayList<>();
    private final InfocasterConnection infocaster;
    private final String listenerId = UUID.randomUUID().toString();

    @Inject
    public QueryStreamerManager(InfocasterConnection infocaster, QueryStreamerService queryStreamerService, Meta meta) {
        this.infocaster = infocaster;
        this.meta = meta;
        this.queryStreamerService = queryStreamerService;

        infocaster.getStatus().distinctUntilChanged().subscribe(status -> destinationChanged(status.getDestination()), throwable -> {
            Timber.e(throwable, "Unexpected status");
        });
        infocaster.getEvents().subscribe(event -> {
            // Repackage event to the old LCA format
            JSONObject jsonEvent = event.toJSONObject();
            JSONObject result = JSONUtil.getJSONObject(jsonEvent, "data.payload.result");
            boolean shouldDelete = JSONUtil.optBoolean(jsonEvent, "data.payload.parameters.noLongerMatchingQuery", false);
            if (shouldDelete) {
                result.put("eventtype", "DELETE");
            }
            JSONObject body = JSONUtil.wrap("payload.data", new JSONObject().put("result", result.toString()));
            body.getJSONObject("payload").put("action", "streamNotify");
            synchronized (streams) {
                for (Stream stream : streams) {
                    if (stream.getStreamId().equals(event.getStreamId())) {
                        stream.getListener().onResponse(stream.getQuery(), body);
                    }
                }
            }
        }, throwable -> {
            Timber.e(throwable, "Unexpected event");
        });
    }

    public void create(CreateStreamQuery query, QueryResponseListener listener) throws JSONException {
        if (query.hasDestination()) {
            CreateStream createStream = buildCreateStream(query, query.getDestination().toJsonObject());
            Single.just(createStream).observeOn(Schedulers.io())
                    .flatMap(queryStreamerService::createStream)
                    .map(JSONUtil::toJSONObject)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        if (!query.finishedOnResponse()) {
                            synchronized (streams) {
                                streams.add(new Stream(query, response, listener));
                            }
                        }
                        JSONObject body = JSONUtil.wrap("payload.data", response);
                        body.getJSONObject("payload").put("action", "streamCreated");
                        listener.onResponse(query, body);
                    }, throwable -> {
                        Timber.e(throwable);
                        listener.onError(throwable);
                    });
        } else {
            infocaster.getStatus().observeOn(Schedulers.io()).filter(Status::isConnected)
                    .firstOrError()
                    .flatMap(status -> {
                        try {
                            return queryStreamerService.createStream(buildCreateStream(query, status.getDestination()));
                        }
                        catch (Throwable throwable) {
                            throw throwable;
                        }

                    })
                    .map(JSONUtil::toJSONObject)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        if (!query.finishedOnResponse()) {
                            synchronized (streams) {
                                Timber.d("Stream added: " + query);
                                streams.add(new Stream(query, response, listener));
                            }
                        }
                        // Repackage event to the old LCA format
                        JSONObject body = JSONUtil.wrap("payload.data", response);
                        body.getJSONObject("payload").put("action", "streamCreated");
                        listener.onResponse(query, body);
                    }, throwable -> {
                        Timber.e(throwable);
                        listener.onError(throwable);
                    });
            infocaster.open(listenerId);
        }
    }

    private CreateStream buildCreateStream(CreateStreamQuery query, JsonObject destination) throws JSONException {
        synchronized (streams) {
            return new CreateStream.Builder()
                    .setDestination(destination)
                    .setMeta(meta)
                    .setQuery(query.query())
                    .create();
        }
    }

    public void delete(DeleteStreamQuery query, QueryResponseListener listener) {
        Single.just(query.getStreamId()).observeOn(Schedulers.io())
                .flatMap(queryStreamerService::deleteStream)
                .map(JSONUtil::toJSONObject)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    synchronized (streams) {
                        List<Stream> streamsToRemove = streamsMatching(query);
                        this.streams.removeAll(streamsToRemove);
                    }
                    listener.onResponse(query, response);
                    if (streams.isEmpty()) {
                        infocaster.close(listenerId);
                    }
                }, listener::onError);
    }

    public void update(UpdateStreamQuery query, QueryResponseListener listener) {
        // update the stream
        // TODO Implement or no?
    }

    private void destinationChanged(JsonObject destination) {
        Timber.d("Destination changed " + destination);
        // Remove any streams using the old destination
        Observable.fromIterable(new ArrayList<>(streams)).observeOn(Schedulers.io())
                .filter(stream -> stream.getStreamId() != null)
                .map(stream -> {
                    String streamId = stream.getStreamId();
                    Timber.d("Removing stream " + streamId);
                    stream.setStreamId(null);
                    return streamId;
                }).flatMap(streamId1 -> queryStreamerService.deleteStream(streamId1).toObservable())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> Timber.d("Removed stream " + result), throwable -> {
                    Timber.e(throwable, "failed to delete");
                });
        // If we have a new destination, make all active streams use this
        if (destination != null) {
            ArrayList<Stream> oldStreams = new ArrayList<>(streams);
            synchronized (streams) {
                streams.clear();
            }
            Timber.d("Resuming " + oldStreams.size() + " streams with new destination");
            for (Stream old : oldStreams) {
                Observable.just(old).observeOn(Schedulers.io())
                        .singleOrError()
                        .flatMap(stream -> queryStreamerService.createStream(buildCreateStream(stream.getQuery(), destination)))
                        .map(JSONUtil::toJSONObject)
                        .subscribe(jsonObject -> {
                            synchronized (streams) {
                                streams.add(new Stream(old.getQuery(), jsonObject, old.getListener()));
                            }
                        }, throwable -> {
                            Timber.e(throwable, "Failed to resume stream: " + old);
                        });
            }
        }
    }

    public List<Stream> getActiveStreams() {
        return Observable.fromIterable(new ArrayList<>(streams)).filter(stream -> stream.getStreamId() != null).toList().blockingGet();
    }

    public boolean remove(Query query) {
        List<Stream> matchingStreams = streamsMatching(query);
        Observable.fromIterable(matchingStreams).observeOn(Schedulers.io()).filter(Stream::hasStreamId)
                .doOnNext(stream -> {
                    synchronized (streams) {
                        streams.remove(stream);
                    }
                })
                .firstOrError()
                .flatMap(stream -> queryStreamerService.deleteStream(stream.getStreamId()))
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(jsonObject -> {
                    Timber.d("Stream removed: " + jsonObject);
                    if (this.streams.isEmpty()) {
                        infocaster.close(listenerId);
                    }
                }, throwable -> Timber.w(throwable, "Failed to delete stream"));

        return !matchingStreams.isEmpty();
    }

    private List<Stream> streamsMatching(Query query) {
        synchronized (streams) {
            return Observable.fromIterable(this.streams)
                    .filter(stream -> query.equals(stream.getQuery()))
                    .toList().blockingGet();
        }
    }

    public String getListenerId() {
        return listenerId;
    }
}
