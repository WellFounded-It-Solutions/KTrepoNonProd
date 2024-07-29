package se.infomaker.frtutilities.runtimeconfiguration;

public interface RuntimeConfigManager {
    void registerOnChangeListener(OnConfigChangeListener listener);

    void removeOnChangeListener(OnConfigChangeListener listener);

    ResourceStatus status(String key);

    String get(String key);
}
