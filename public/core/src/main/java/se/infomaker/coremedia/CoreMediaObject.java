package se.infomaker.coremedia;

import java.util.Map;

/**
 * Created by Magnus Ekström on 24/02/16.
 */
public class CoreMediaObject {
    private String mName;
    private Map<String, Object> mAttributes;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public Map<String, Object> getAttributes() {
        return mAttributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        mAttributes = attributes;
    }
}
