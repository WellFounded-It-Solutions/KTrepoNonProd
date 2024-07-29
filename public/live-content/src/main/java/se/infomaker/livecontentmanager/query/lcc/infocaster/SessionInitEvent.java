package se.infomaker.livecontentmanager.query.lcc.infocaster;

public class SessionInitEvent implements Event {
    private SessionData data;

    @Override
    public String toString() {
        return "SessionInitEvent{" +
                "data=" + data +
                '}';
    }

    public SessionData getData() {
        return data;
    }
}
