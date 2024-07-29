package se.infomaker.livecontentui.section;

import java.util.HashSet;
import java.util.Set;

/**
 * Notify about package updates
 */
public class SectionUpdateNotifier {
    public interface OnPackageUpdated {
        /**
         * Invoked when a package configured to notify all is updated as a result of a remote event
         */
        void packageUpdated();
    }

    private static final SectionUpdateNotifier instance = new SectionUpdateNotifier();
    private Set<OnPackageUpdated> listeners = new HashSet<>();

    private SectionUpdateNotifier() {
    }

    public static void addListener(OnPackageUpdated listener) {
        instance.add(listener);
    }
    private synchronized void add(OnPackageUpdated listener) {
        listeners.add(listener);
    }
    public static void removeListener(OnPackageUpdated listener) {
        instance.remove(listener);
    }
    private synchronized void remove(OnPackageUpdated listener) {
        listeners.remove(listener);
    }
    public static void updateAll() {
        instance.notifyUpdated();
    }

    private synchronized void notifyUpdated() {
        for (OnPackageUpdated listener : instance.listeners) {
            listener.packageUpdated();
        }
    }
}
