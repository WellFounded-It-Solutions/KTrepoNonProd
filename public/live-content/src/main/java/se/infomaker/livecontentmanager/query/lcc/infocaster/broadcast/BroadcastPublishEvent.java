package se.infomaker.livecontentmanager.query.lcc.infocaster.broadcast;

import se.infomaker.livecontentmanager.query.lcc.infocaster.Event;

public class BroadcastPublishEvent implements Event {
    private BroadcastPublishData data;
    public String getUUID() {
        return data.getPayload().get("uuid").getAsString();
    }
}
