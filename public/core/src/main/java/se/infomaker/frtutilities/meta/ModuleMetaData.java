package se.infomaker.frtutilities.meta;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import se.infomaker.frtutilities.CollectionUtil;
import se.infomaker.frtutilities.ConfigManager;
import se.infomaker.frtutilities.MainMenuItem;

public class ModuleMetaData implements ValueProvider {

    private final Map<String, Object> meta;

    public ModuleMetaData(Map<String, Object> meta) {
        this.meta = Collections.unmodifiableMap(meta);
    }

    @Nullable
    @Override
    public List<String> getStrings(@NonNull String keyPath) {
        Object value = meta.get(keyPath);
        if (value instanceof List) {
            return CollectionUtil.convertToStrings((List) value);
        }
        return value != null ? CollectionUtil.asList(value.toString()) : null;
    }

    @Nullable
    @Override
    public String getString(@NonNull String keyPath) {
        Object o = meta.get(keyPath);
        if (o instanceof List && !((List) o).isEmpty()) {
            return (String) ((List) o).get(0);
        }
        if (o != null) {
            return o.toString();
        }
        return null;
    }

    @Nullable
    @Override
    public Observable<String> observeString(@NonNull String keyPath) {
        String value = getString(keyPath);
        if (value == null) {
            return Observable.never();
        }
        return Observable.just(value);
    }

    public static class Builder {
        private final Map<String, Object> meta = new HashMap<>();
        private String moduleId;

        @NonNull
        public Builder put(@NonNull String key, @NonNull String value) {
            Object current = meta.get(key);
            if (current instanceof List) {
                ((List) current).add(value);
            }
            else {
                ArrayList<Object> list = new ArrayList<>();
                list.add(value);
                meta.put(key, value);
            }
            return this;
        }

        @NonNull
        public Builder putAll(@NonNull Map<? extends String, ? extends String> values) {
            meta.putAll(values);
            return this;
        }

        @NonNull
        public Builder moduleId(@Nullable String moduleId) {
            this.moduleId = moduleId;
            return this;
        }

        public ModuleMetaData build(@NonNull Context context) {
            if (moduleId != null) {
                MainMenuItem menuItem = ConfigManager.getInstance(context).getMenuItem(moduleId);
                if (menuItem != null) {
                    meta.put("module.Id", menuItem.getId());
                    meta.put("module.Title", menuItem.getTitle());
                    meta.put("module.Name", menuItem.getModuleName());
                    meta.put("module.Color", menuItem.getColor());
                }
            }
            return new ModuleMetaData(meta);
        }
    }
}
