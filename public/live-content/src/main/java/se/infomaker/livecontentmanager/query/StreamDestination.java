package se.infomaker.livecontentmanager.query;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;

public class StreamDestination {
    private final String type;
    private final String arn;
    private final String platform;
    private final List<String> properties;
    private final Integer pushTTL;
    private final String appId;
    private final String token;

    @Deprecated
    public StreamDestination(String type, String arn, String platform, List<String> properties, Integer pushTTL) {
        this.type = type;
        this.arn = arn;
        this.platform = platform;
        this.properties = properties;
        this.pushTTL = pushTTL;
        this.appId = null;
        this.token = null;
    }

    private StreamDestination(Builder builder) {
        this.type = builder.type;
        this.arn = builder.arn;
        this.platform = builder.platform;
        this.properties = builder.properties;
        this.pushTTL = builder.pushTTL;
        this.appId = builder.appId;
        this.token = builder.token;
    }

    public JsonObject toJsonObject() {
        return new Gson().toJsonTree(this).getAsJsonObject();
    }

    @NonNull
    public static Builder builder() {
        return new Builder();
    }

    @NonNull
    public Builder buildUpon() {
        return new Builder()
                .setType(type)
                .setArn(arn)
                .setPlatform(platform)
                .setProperties(properties)
                .setPushTTL(pushTTL)
                .setAppId(appId)
                .setToken(token);
    }

    public static class Builder {

        private String type;
        private String arn;
        private String platform;
        private List<String> properties;
        private Integer pushTTL;
        private String appId;
        private String token;

        private Builder() {

        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setArn(String arn) {
            this.arn = arn;
            return this;
        }

        public Builder setPlatform(String platform) {
            this.platform = platform;
            return this;
        }

        public Builder setProperties(List<String> properties) {
            this.properties = properties;
            return this;
        }

        public Builder setPushTTL(Integer pushTTL) {
            this.pushTTL = pushTTL;
            return this;
        }

        public Builder setAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public Builder setToken(String token) {
            this.token = token;
            return this;
        }

        public StreamDestination build() {
            return new StreamDestination(this);
        }
    }
}
