package se.infomaker.livecontentmanager.query.lcc.querystreamer;

import com.google.gson.JsonObject;

public class CreateStream {
    private JsonObject destination;
    private JsonObject query;
    private Config config = Config.DEFAULT;
    private Meta meta;

    private CreateStream(JsonObject destination, JsonObject query, Config config, Meta meta) {
        this.destination = destination;
        this.query = query;
        this.config = config;
        this.meta = meta;
    }

    public static class Builder {
        private JsonObject destination;
        private JsonObject query;
        private Config config = Config.DEFAULT;
        private Meta meta;

        public Builder setDestination(JsonObject destination) {
            this.destination = destination;
            return this;
        }

        public Builder setQuery(JsonObject query) {
            this.query = query;
            return this;
        }

        public Builder setConfig(Config config) {
            this.config = config;
            return this;
        }

        public Builder setMeta(Meta meta) {
            this.meta = meta;
            return this;
        }

        public CreateStream create() {
            return new CreateStream(destination, query, config, meta);
        }
    }
}
