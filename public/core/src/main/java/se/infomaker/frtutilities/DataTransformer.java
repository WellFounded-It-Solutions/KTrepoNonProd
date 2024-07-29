package se.infomaker.frtutilities;

import android.text.Html;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by magnusekstrom on 16/06/16.
 */

public class DataTransformer {
    private ConfigPropertyFinder configPropertyFinder;
    private Map<String, Transformer> transformers = new HashMap<>();

    public DataTransformer(ConfigPropertyFinder configPropertyFinder) {
        this.configPropertyFinder = configPropertyFinder;

        registerTransformer("formatReplace", new Transformer() {
            @Override
            public String transform(String value, ConfigPropertyFinder configPropertyFinder) {
                Map<String, String> formatReplaceMap = configPropertyFinder.getProperty(Map.class, "formatReplace");
                for (Map.Entry<String, String> entry : formatReplaceMap.entrySet()) {
                    value = value.replace(entry.getKey(), entry.getValue());
                }
                return value;
            }
        });

        registerTransformer("stripHtml", new Transformer() {
            @Override
            public String transform(String value, ConfigPropertyFinder configPropertyFinder) {
                return Html.fromHtml(value).toString();
            }
        });
    }

    public String transform(String value, List<String> transformers) {
        for (String transformer : transformers) {
            value = this.transformers.get(transformer).transform(value, this.configPropertyFinder);
        }

        return value;
    }

    public void registerTransformer(String name, Transformer transformer) {
        this.transformers.put(name, transformer);
    }

    public interface Transformer {
        String transform(String value, ConfigPropertyFinder configPropertyFinder);
    }
}
