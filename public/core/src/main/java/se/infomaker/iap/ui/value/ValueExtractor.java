package se.infomaker.iap.ui.value;

import se.infomaker.iap.ui.content.Content;

/**
 * Extracts a value from content
 */
public interface ValueExtractor {

    /**
     * Extract value from provided context
     * @param content to extract value from
     * @return Extracted value or null if none could be extracted
     */
    String getValue(Content content);
}
