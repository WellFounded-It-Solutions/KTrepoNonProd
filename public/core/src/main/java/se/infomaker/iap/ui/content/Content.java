package se.infomaker.iap.ui.content;

import se.infomaker.iap.ui.value.NoValueException;

public interface Content {
    /**
     * Gets value for key from content
     * @param key to get value for
     * @return the value object from the content
     * @throws NoValueException if no value is present
     */
    Object getValue(String key) throws NoValueException;

    /**
     * Gets value from content for key
     * @param key to get value for
     * @return value or null if no value is present
     */
    Object optValue(String key);
}
