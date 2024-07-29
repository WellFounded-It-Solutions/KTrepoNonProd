package se.infomaker.livecontentmanager.query.lcc.infocaster;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SessionData {
    private String sessionSecret;
    private String webhookUrl;
    private JsonObject destination;

    public JsonObject getDestination() {
        return destination;
    }

    public String getSessionId() {
        return getDestinationValue("sessionId");
    }

    public String getInstanceId() {
        return getDestinationValue("instanceId");
    }

    public String getPublisherId() {
        return getDestinationValue("publisherId");
    }

    public String getBaseUrl() {
        return getDestinationValue("baseUrl");
    }

    private String getDestinationValue(String key) {
        JsonElement element = destination != null ? destination.get(key) : null;
        if (element != null) {
            return element.getAsString();
        }
        return null;
    }

    public String getSessionSecret() {
        return sessionSecret;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }
}
