package se.infomaker.livecontentmanager.query.lcc.querystreamer;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import com.navigaglobal.mobile.auth.AuthorizationProvider;
import com.navigaglobal.mobile.auth.BasicAuthAuthorizationProvider;

public class QueryStreamerServiceBuilder {

    public static final String AUTHORIZATION = "Authorization";
    private String baseUrl;
    private AuthorizationProvider authorizationProvider;
    private String username;
    private String password;
    private OkHttpClient okHttpClient;

    public QueryStreamerServiceBuilder setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public QueryStreamerServiceBuilder setAuthorizationProvider(AuthorizationProvider authorizationProvider) {
        this.authorizationProvider = authorizationProvider;
        return this;
    }

    public QueryStreamerServiceBuilder setId(String username) {
        this.username = username;
        return this;
    }

    public QueryStreamerServiceBuilder setReadToken(String password) {
        this.password = password;
        return this;
    }

    public QueryStreamerServiceBuilder setOkHttpClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
        return this;
    }

    /**
     * @deprecated Ignored. Will always log body in debug.
     */
    @Deprecated
    public QueryStreamerServiceBuilder setLog(boolean log) {
        return this;
    }

    public QueryStreamerService build() {
        OkHttpClient.Builder builder = okHttpClient.newBuilder();
        if (authorizationProvider == null && username != null && password != null) {
            authorizationProvider = new BasicAuthAuthorizationProvider(username, password);
        }
        if (authorizationProvider != null) {
            builder.addNetworkInterceptor(
                chain -> {
                    Request request = chain.request();
                    if (request.header(AUTHORIZATION) == null) {
                        String authorization = authorizationProvider.getAuthorization();
                        if (authorization != null) {
                            request = request.newBuilder().addHeader(AUTHORIZATION, authorization).build();
                        }
                    }
                    return chain.proceed(request);
                }
            );
        }

        Retrofit retrofit = new Retrofit.Builder()
                .client(builder.build())
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        return retrofit.create(QueryStreamerService.class);
    }

}
