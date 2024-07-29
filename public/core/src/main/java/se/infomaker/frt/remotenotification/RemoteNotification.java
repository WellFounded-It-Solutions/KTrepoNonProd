package se.infomaker.frt.remotenotification;

import androidx.annotation.NonNull;

import java.util.Map;

/**
 * A remote notification received
 */
public class RemoteNotification {
    private final String sender;
    private final Map<String, String> data;

    public RemoteNotification(String sender, Map<String, String> data) {
        this.sender = sender;
        this.data = data;
    }

    /**
     * Sender of the message
     *
     * @return the origin of the message
     */
    public String getSender() {
        return sender;
    }

    /**
     * The message data
     *
     * @return the data in the notification
     */
    public Map<String, String> getData() {
        return data;
    }

    @NonNull
    @Override
    public String toString() {
        return "RemoteNotification(sender=" + sender + ", data=" + data.toString()
                + ")";
    }
}
