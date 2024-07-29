package se.infomaker.frtutilities;

import java.util.Map;

public class ShortCodeObject {
    private String name;
    private Map<String, Object> attributes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[").append(name);
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            builder.append(" ").append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");
        }
        builder.append("]");
        return builder.toString();
    }
}
