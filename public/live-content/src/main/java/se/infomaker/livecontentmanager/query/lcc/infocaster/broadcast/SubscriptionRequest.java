package se.infomaker.livecontentmanager.query.lcc.infocaster.broadcast;

import java.util.List;
import java.util.Map;

public class SubscriptionRequest {
    private String sessionId;
    private String sessionSecret;
    private List<Map<String, String>> filters;

    public SubscriptionRequest(String sessionId, String sessionSecret, List<Map<String, String>> filters) {
        this.sessionId = sessionId;
        this.sessionSecret = sessionSecret;
        this.filters = filters;
    }
}
