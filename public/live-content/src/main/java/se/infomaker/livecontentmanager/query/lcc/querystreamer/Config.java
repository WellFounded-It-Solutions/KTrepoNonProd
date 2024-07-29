package se.infomaker.livecontentmanager.query.lcc.querystreamer;

public class Config {
    public static final Config DEFAULT = new Config(true); // TODO notifyOnNoLongerMatchingQuery is not ready for production
    private boolean notifyOnNoLongerMatchingQuery = true;
    private boolean allowIdenticalStreams = false;

    public Config(){

    }

    public Config(boolean notifyOnNoLongerMatchingQuery) {
        this.notifyOnNoLongerMatchingQuery = notifyOnNoLongerMatchingQuery;
    }

    public boolean isNotifyOnNoLongerMatchingQuery() {
        return notifyOnNoLongerMatchingQuery;
    }

    public void setNotifyOnNoLongerMatchingQuery(boolean notifyOnNoLongerMatchingQuery) {
        this.notifyOnNoLongerMatchingQuery = notifyOnNoLongerMatchingQuery;
    }
}
