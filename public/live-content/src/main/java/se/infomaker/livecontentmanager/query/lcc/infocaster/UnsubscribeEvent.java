package se.infomaker.livecontentmanager.query.lcc.infocaster;

class UnsubscribeEvent implements Event {
    private UnsubscribeData data;

    public UnsubscribeData getData() {
        return data;
    }
}
