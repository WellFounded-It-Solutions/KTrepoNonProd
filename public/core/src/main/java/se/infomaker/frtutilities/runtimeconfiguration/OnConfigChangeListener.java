package se.infomaker.frtutilities.runtimeconfiguration;

import java.util.List;
import java.util.Set;

/**
 * Notifies when configuration changes
 */
public interface OnConfigChangeListener {

    /**
     * Called when a configuration change has been completed
     * @param updated list of config resources that has been added och changed
     * @param removed list of config resources that has been deleted
     *
     * @return resources handled
     */
    Set<String> onChange(List<String> updated, List<String> removed);
}
