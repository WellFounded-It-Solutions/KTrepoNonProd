package se.infomaker.livecontentmanager.query.lcc.opencontent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import se.infomaker.frtutilities.TextUtils;
import com.navigaglobal.mobile.auth.AuthorizationProvider;
import com.navigaglobal.mobile.auth.BasicAuthAuthorizationProvider;
import se.infomaker.livecontentmanager.network.NetworkAvailabilityManager;
import se.infomaker.livecontentmanager.util.HttpDate;
import timber.log.Timber;

public class OpenContentBuilder {

    public static final String AUTHORIZATION = "Authorization";
    private String baseUrl;
    private AuthorizationProvider authorizationProvider;
    private String username;
    private String password;
    private List<Interceptor> interceptors = new ArrayList<>();
    private NetworkAvailabilityManager networkAvailabilityManager;
    private File cacheDir;
    private OkHttpClient client;

    public OpenContentBuilder setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public OpenContentBuilder setAuthorizationProvider(AuthorizationProvider authorizationProvider) {
        this.authorizationProvider = authorizationProvider;
        return this;
    }

    @Deprecated
    public OpenContentBuilder setUsername(String username) {
        this.username = username;
        return this;
    }

    @Deprecated
    public OpenContentBuilder setPassword(String password) {
        this.password = password;
        return this;
    }

    @Deprecated
    public OpenContentBuilder setLog(boolean log) {
        return this;
    }

    public OpenContentBuilder setNetworkAvailabilityManager(NetworkAvailabilityManager manager) {
        this.networkAvailabilityManager = manager;
        return this;
    }

    public OpenContentBuilder setCacheDir(File cacheDir) {
        this.cacheDir = cacheDir;
        return this;
    }

    public OpenContentBuilder setClient(OkHttpClient client) {
        this.client = client;
        return this;
    }

    public OpenContentService build() {
        if (authorizationProvider == null && username != null && password != null) {
            authorizationProvider = new BasicAuthAuthorizationProvider(username, password);
        }
        OkHttpClient.Builder builder = client.newBuilder();
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
        if (cacheDir != null) {
            long cacheSize = 20 * 1024 * 1024;
            builder.cache(new Cache(cacheDir, cacheSize));
        }
        if (networkAvailabilityManager != null) {
            // Rewrite cache behaviour depending on device being online/offline
            builder.addInterceptor(chain -> {
                Request request = chain.request();
                if (networkAvailabilityManager.hasNetwork()) {
                    return chain.proceed(request.newBuilder()
                            .cacheControl(CacheControl.FORCE_NETWORK)
                            .build());
                }
                else {
                    Response response = chain.proceed(request.newBuilder()
                            .cacheControl(CacheControl.FORCE_CACHE)
                            .build());
                    return response;
                }
            });
            builder.addNetworkInterceptor(chain -> {
                Request request = chain.request();
                Response response = chain.proceed(request);

                Response.Builder responseBuilder = response.newBuilder();
                if (response.header("date") == null) {
                    Timber.d("Added date header, since it was missing.");
                    responseBuilder.addHeader("date", HttpDate.format(new Date()));
                }
                // Make sure we can cache all requests for offline use
                if (networkAvailabilityManager.hasNetwork()) {
                    responseBuilder.removeHeader("Cache-Control");
                    responseBuilder.removeHeader("Pragma");
                    responseBuilder.addHeader("Cache-Control", "public, max-age=31536000");
                }
                return responseBuilder.build();
            });
        }

        // Request normalizer
        /*
         * To allow caching on url basis, the order of queryparams MUST be the same between
         * client implementations. Here we sort queryparams and make sure we encode them
         * in the same manner as on iOS.
         */
        builder.addInterceptor(chain -> {
            HttpUrl old = chain.request().url();
            Request request = chain.request().newBuilder().url(normalisedUrl(old, value -> {
                // Latest OC cache solution requires | to be encoded
                value = value.replace("|", "%7C");
                // iOS encodes [ and ] so we need to do it to make the request cacheable
                value = value.replace("[", "%5B");
                return value.replace("]", "%5D");
            })).build();
            return chain.proceed(request);
        });

        for (Interceptor interceptor : interceptors) {
            builder.addInterceptor(interceptor);
        }

        Retrofit retrofit = new Retrofit.Builder()
                .client(builder.build())
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        return retrofit.create(OpenContentService.class);
    }

    public OpenContentBuilder addInterceptor(Interceptor interceptor) {
        interceptors.add(interceptor);
        return this;
    }

    private HttpUrl normalisedUrl(HttpUrl url, Encoder encoder) {
        HttpUrl.Builder builder = new HttpUrl.Builder();
        builder.scheme(url.scheme());
        builder.username(url.username());
        builder.password(url.password());
        builder.host(url.host());
        if (url.port() > 0) {
            builder.port(url.port());
        }
        builder.addPathSegments(TextUtils.join("/", url.pathSegments()));

        Set<String> queryParameterNames = url.queryParameterNames();
        List<String> names = new ArrayList<>(queryParameterNames);
        Collections.sort(names);
        for (String key : names) {
            String value = url.queryParameter(key);

            builder.addEncodedQueryParameter(key, encoder.encode(value));
        }
        return builder.build();
    }

    private interface Encoder {
        String encode(String value);
    }
}
