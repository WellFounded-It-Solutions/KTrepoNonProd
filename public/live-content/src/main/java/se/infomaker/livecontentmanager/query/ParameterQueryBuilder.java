package se.infomaker.livecontentmanager.query;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ParameterQueryBuilder {
    private Map<String, String> params = new HashMap<>();
    private Set<String> properties = new HashSet<>();

    public void set(String key, String value) {
        params.put(key, value);
    }

    public void addProperty(String property) {
        properties.add(property);
    }
}
