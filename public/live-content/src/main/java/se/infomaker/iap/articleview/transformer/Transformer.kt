package se.infomaker.iap.articleview.transformer

import org.json.JSONObject
import se.infomaker.iap.articleview.ContentStructure

/**
 * Transforms properties to content structure
 */
interface Transformer {

    /**
     * Transform a set of properties
     * @param properties a set of properties available when constructing the content Structure
     *
     * @return content structure containing body and properties
     */
    fun transform(properties: JSONObject) : ContentStructure
}