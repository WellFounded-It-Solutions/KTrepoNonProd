package se.infomaker.frt.statistics;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by magnusekstrom on 08/04/16.
 */
public class StatisticsEvent {
    private static final String EVENT_NAME_VIEW_SHOW = "viewShow";
    private static final String ATTRIBUTE_KEY_MODULE_ID = "moduleID";
    private static final String ATTRIBUTE_KEY_MODULE_NAME = "moduleName";
    private static final String ATTRIBUTE_KEY_MODULE_TITLE = "moduleTitle";
    private static final String ATTRIBUTE_KEY_MODULE_PARENT = "moduleParent";
    private static final String ATTRIBUTE_KEY_VIEW_NAME = "viewName";

    private String eventName;
    private Map<String, Object> attributes;

    private StatisticsEvent(Builder builder) {
        this.eventName = builder.eventName;
        this.attributes = builder.attributes;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public boolean isViewEvent() {
        return EVENT_NAME_VIEW_SHOW.equals(eventName);
    }

    public String getViewName() {
        return (String) attributes.get(ATTRIBUTE_KEY_VIEW_NAME);
    }

    public String getViewParent() {
        return (String) attributes.get(ATTRIBUTE_KEY_MODULE_PARENT);
    }

    public void addAttributes(Map<String, Object> attributes) {
        this.attributes.putAll(attributes);
    }

    public Builder buildUpon() {
        Builder builder = new Builder();
        builder.event(eventName);
        builder.attributes(attributes);
        return builder;
    }

    public static class Builder {
        private String eventName = "";
        private final Map<String, Object> attributes = new HashMap<>();

        public Builder() {

        }

        public Builder event(String eventName) {
            this.eventName = eventName;
            return this;
        }

        public Builder viewShow() {
            this.eventName = EVENT_NAME_VIEW_SHOW;
            return this;
        }

        public Builder moduleId(String moduleId) {
            this.attributes.put(ATTRIBUTE_KEY_MODULE_ID, moduleId);
            return this;
        }

        public Builder moduleName(String moduleName) {
            this.attributes.put(ATTRIBUTE_KEY_MODULE_NAME, moduleName);
            return this;
        }

        public Builder moduleTitle(String moduleTitle) {
            this.attributes.put(ATTRIBUTE_KEY_MODULE_TITLE, moduleTitle);
            return this;
        }

        public Builder viewName(String viewName) {
            this.attributes.put(ATTRIBUTE_KEY_VIEW_NAME, viewName);
            return this;
        }

        public Builder parent(String ParentName) {
            this.attributes.put(ATTRIBUTE_KEY_MODULE_PARENT, ParentName);
            return this;
        }

        public Builder attribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Builder attributes(Map<String, Object> attributes) {
            this.attributes.putAll(attributes);
            return this;
        }

        public StatisticsEvent build() {
            return new StatisticsEvent(this);
        }
    }
}
