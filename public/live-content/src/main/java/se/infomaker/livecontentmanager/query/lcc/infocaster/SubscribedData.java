package se.infomaker.livecontentmanager.query.lcc.infocaster;

import java.util.Map;

class SubscribedData {
    private String publisherId;
    private String sessionId;
    private String broadcastId;
    private Map<String, String> filter;

    public String getPublisherId() {
        return publisherId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getBroadcastId() {
        return broadcastId;
    }

    public Map<String, String> getFilter() {
        return filter;
    }
}
