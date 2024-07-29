package se.infomaker.frt.moduleinterface;

import android.app.Activity;
import android.os.Bundle;

public interface RemoteNotificationHandlerInterface
{
    /**
     * Allows
     * @param notification bundle from gcm message
     * @return true if the remote notification was handled
     */
    boolean handleRemoteNotification(Activity context, Bundle config, Bundle notification);
}
