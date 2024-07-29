package se.infomaker.livecontentmanager.query.lca;

public class SearchPayload extends Payload{
    private SearchData data;

    public SearchPayload(String action, ContentProvider contentProvider, SearchData data) {
        super(action, contentProvider);
        this.data = data;
    }
}
