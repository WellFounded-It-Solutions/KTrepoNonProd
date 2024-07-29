package se.infomaker.livecontentmanager.query.lcc.querystreamer;

import com.google.gson.JsonObject;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Note: The base url is supposed to include "/v1/stream-provider/{streamProviderId}/"
 */
public interface QueryStreamerService {
    @POST("streams")
    Single<JsonObject> createStream(@Body() CreateStream body);

    @DELETE("streams/{streamId}")
    Single<JsonObject> deleteStream(@Path("streamId") String streamId);

    @PUT("streams/{streamId}")
    Single<JsonObject> updateStream(@Body() UpdateStream body, @Path("streamId") String streamId);
}
