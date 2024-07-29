package se.infomaker.iap.ui.content;

import java.util.Map;

import se.infomaker.iap.ui.value.NoValueException;

public class MapContentWrapper implements Content {
    private final Map<String, ?> map;

    public MapContentWrapper(Map<String, ?> map) {
        this.map = map;
    }

    @Override
    public Object getValue(String key) throws NoValueException {
        Object o = map.get(key);
        if (o == null) {
            throw new NoValueException("No value for key: " + key);
        }
        return o;
    }

    @Override
    public Object optValue(String key) {
        return map.get(key);
    }
}
