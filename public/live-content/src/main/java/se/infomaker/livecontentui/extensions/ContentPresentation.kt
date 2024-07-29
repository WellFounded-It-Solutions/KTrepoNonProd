package se.infomaker.livecontentui.extensions

import org.json.JSONObject
import se.infomaker.iap.articleview.presentation.match.matches
import se.infomaker.livecontentui.config.ContentPresentationConfig

internal fun ContentPresentationConfig.matchTeaser(
        properties: JSONObject,
        presentationContext: JSONObject
) = teasers?.let { teasers ->
    teasers.filter { teaser -> teaser.require?.let { properties.hasAll(it) } ?: true }
            .firstOrNull { it.provide().matches(properties, presentationContext) }
}

internal fun ContentPresentationConfig.matchThemes(
        properties: JSONObject,
        presentationContext: JSONObject
): List<String>? {
    val out = mutableListOf<String>()
    extraThemes?.filter { it.provide().matches(properties, presentationContext) }
            ?.map { it.themes }
            ?.flatten()
            ?.let {
                out.addAll(it)
            }

    themes?.firstOrNull { it.provide().matches(properties, presentationContext) }
            ?.let {
                out.addAll(it.themes)
            }

    return out.toSet().toList().ifEmpty { null }
}