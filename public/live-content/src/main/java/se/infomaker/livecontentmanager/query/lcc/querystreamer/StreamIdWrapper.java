package se.infomaker.livecontentmanager.query.lcc.querystreamer;

public class StreamIdWrapper {
    private String streamId;

    public String getStreamId() {
        return streamId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StreamIdWrapper that = (StreamIdWrapper) o;

        return streamId != null ? streamId.equals(that.streamId) : that.streamId == null;
    }

    @Override
    public int hashCode() {
        return streamId != null ? streamId.hashCode() : 0;
    }
}
