package se.infomaker.livecontentmanager.query.lca;

public class Payload {
    private String action;
    private final Auth auth = new Auth();
    private ContentProvider contentProvider;
    private final int version = 1;

    public Payload(String action, ContentProvider contentProvider) {
        this.action = action;
        this.contentProvider = contentProvider;
    }
}
