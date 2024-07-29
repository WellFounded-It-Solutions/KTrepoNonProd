package se.infomaker.streamviewer.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RemoteNotificationConfig {
    private String pubdateKey;
    private String titleKey;
    private String subtitleKey;
    private String contentIdKey;

    public String getPubdateKey() {
        return pubdateKey;
    }

    public String getTitleKey() {
        return titleKey;
    }

    public String getSubtitleKey() {
        return subtitleKey;
    }

    public String getContentIdKey() {
        return contentIdKey;
    }

    /**
     * @return a list of all config keys
     */
    public List<String> properties() {
        List<String> properties = new ArrayList<>();
        properties.add(pubdateKey);
        properties.add(titleKey);
        properties.add(subtitleKey);
        properties.add(contentIdKey);
        properties.add("eventtype");
        properties.removeIf(Objects::isNull);
        return properties;
    }
}
