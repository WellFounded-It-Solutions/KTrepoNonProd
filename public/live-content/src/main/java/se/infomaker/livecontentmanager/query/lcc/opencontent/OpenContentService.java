package se.infomaker.livecontentmanager.query.lcc.opencontent;

import com.google.gson.JsonObject;

import java.util.Map;

import io.reactivex.Single;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface OpenContentService {

    @GET("opencontent/search")
    Single<Response<JsonObject>> search(@Query("start") int start,
                                       @Query("limit") int limit,
                                       @Query("properties") String properties,
                                       @Query("q") String query,
                                       @Query("contenttype") String contentType,
                                       @QueryMap(encoded = true) Map<String, String> sort);

    @GET("opencontent/search")
    Single<Response<JsonObject>> search(@Query("start") int start,
                              @Query("limit") int limit,
                              @Query("properties") String properties,
                              @Query("filters") String filters,
                              @Query("q") String query,
                              @Query("contenttype") String contentType,
                              @QueryMap(encoded = true) Map<String, String> sort);

    @GET("opencontent/search")
    Single<Response<JsonObject>> search(@QueryMap(encoded = true) Map<String, String> queryParams);
}