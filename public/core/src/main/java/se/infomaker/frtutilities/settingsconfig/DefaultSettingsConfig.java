package se.infomaker.frtutilities.settingsconfig;

import java.io.Serializable;

public class DefaultSettingsConfig implements Serializable {
    private static final long serialVersionUID = 569827046395637898L;
    private boolean allowPush = true;
    private int numberOfIssues = 7;
    private boolean allowDownload = true;
    private boolean vibrateOnNotification = true;
    private boolean soundOnNotification = true;
    private boolean prefetchingEnabled = false;

    public boolean getAllowPush() {
        return this.allowPush;
    }

    public int getNumberOfIssues() {
        return this.numberOfIssues;
    }

    public boolean getAllowDownload() {
        return this.allowDownload;
    }

    public boolean getVibrateOnNotification() {
        return vibrateOnNotification;
    }

    public boolean getSoundOnNotification() { return soundOnNotification; }

    public boolean isPrefetchingEnabled() { return prefetchingEnabled; }
}
