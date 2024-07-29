package se.infomaker.livecontentmanager.query.lcc.infocaster;

import com.google.gson.JsonObject;

public class Status {
    private final SessionInitEvent session;
    public static final Status NOT_CONNECTED = new Status(null);

    public static Status withSession(SessionInitEvent session) {
        return new Status(session);
    }

    private Status(SessionInitEvent session) {
        this.session = session;
    }

    public boolean isConnected() {
        return session != null;
    }

    public SessionInitEvent getSession() {
        return session;
    }

    public JsonObject getDestination() {
        if (session != null && session.getData() != null && session.getData().getDestination() != null) {
            return getSession().getData().getDestination();
        }
        return null;
    }
}
