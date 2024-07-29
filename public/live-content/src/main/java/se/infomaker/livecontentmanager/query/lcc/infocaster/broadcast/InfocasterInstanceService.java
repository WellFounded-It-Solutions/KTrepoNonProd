package se.infomaker.livecontentmanager.query.lcc.infocaster.broadcast;

import com.google.gson.JsonObject;

import java.util.Map;

import io.reactivex.Single;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import se.infomaker.livecontentmanager.query.lcc.infocaster.SessionData;

public interface InfocasterInstanceService {

    @POST("v1/instance/{instanceId}/v1/publisher/{publisherId}/broadcast/{broadcastId}/subscribe")
    Single<JsonObject> subscribeBroadcast(@Path("instanceId") String instanceId,
                                          @Path("publisherId") String publisherId,
                                          @Path("broadcastId") String broadcastId,
                                          @QueryMap Map<String, String> queryParams,
                                          @Body SubscriptionRequest request);


    @HTTP(method = "DELETE", path = "v1/instance/{instanceId}/v1/publisher/{publisherId}/broadcast/{broadcastId}/subscribe", hasBody = true)
    Single<JsonObject> unsubscribeBroadcast(@Path("instanceId") String instanceId,
                                            @Path("publisherId") String publisherId,
                                            @Path("broadcastId") String broadcastId,
                                            @QueryMap Map<String, String> queryParams,
                                            @Body SubscriptionRequest request);

    class Builder {
        private SessionData data;
        private OkHttpClient okHttpClient;

        public Builder() {

        }

        public Builder setData(SessionData data) {
            this.data = data;
            return this;
        }

        public Builder setOkHttpClient(OkHttpClient okHttpClient) {
            this.okHttpClient = okHttpClient;
            return this;
        }

        @Deprecated
        public Builder setLog(boolean log) {
            return this;
        }

        public InfocasterInstanceService build() {
            OkHttpClient.Builder builder = okHttpClient.newBuilder();
            builder.retryOnConnectionFailure(true);

            Retrofit retrofit = new Retrofit.Builder()
                    .client(builder.build())
                    .baseUrl(data.getBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();

            return retrofit.create(InfocasterInstanceService.class);
        }
    }
}