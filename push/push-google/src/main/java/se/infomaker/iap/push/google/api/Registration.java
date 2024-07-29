package se.infomaker.iap.push.google.api;

public class Registration {
    private String application;
    private String topic;
    private String token;
    private String deviceId;
    private final String customData = "{}";

    public Registration(String token, String topic, String application) {
        this.token = token;
        this.topic = topic;
        this.application = application;
    }

    // Used for unregistration
    public Registration(String deviceId) {
        this.deviceId = deviceId;
    }
}
