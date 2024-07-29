package se.infomaker.iap.ui.value;

import se.infomaker.iap.ui.content.Content;
import timber.log.Timber;

/**
 * Extracts value from multiple layers of content using a key-path
 */
public class KeyPathValueExtractor implements ValueExtractor {
    private final String[] keyPath;

    public KeyPathValueExtractor(String keyPath) {
        this.keyPath = keyPath.split("\\.");
    }

    @Override
    public String getValue(Content content) {
        Content node = content;
        for (int i = 0; i < keyPath.length - 1; i++) {
            try {
                Object o = content.getValue(keyPath[i]);
                if (!(o instanceof Content)) {
                    Timber.d("Unexpected node type: %s", o);
                    return null;
                }
                node = (Content) o;
            } catch (NoValueException e) {
                return null;
            }
        }
        try {
            Object o = node.getValue(keyPath[keyPath.length - 1]);
            return o instanceof String ? (String) o : o.toString();
        } catch (NoValueException e) {
            Timber.d(e, "Could not extract value");
            return null;
        }
    }
}
