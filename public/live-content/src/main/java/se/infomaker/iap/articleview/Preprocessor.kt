package se.infomaker.iap.articleview

import se.infomaker.frtutilities.ResourceProvider

/**
 * Preprocess the content structure
 */
interface Preprocessor {

    /**
     * Process content. The preprocessor may only modify the content during the call
     *
     * @param content the content to process
     * @param config a string representation of the config for the preprocessor
     * @return a processed version of the content
     */
    fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider) : ContentStructure
}