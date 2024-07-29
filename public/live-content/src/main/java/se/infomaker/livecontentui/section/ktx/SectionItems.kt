@file:JvmName("SectionItemUtils")
package se.infomaker.livecontentui.section.ktx

import io.reactivex.Observable
import se.infomaker.livecontentui.AccessManager
import se.infomaker.livecontentui.StatsHelper
import se.infomaker.livecontentui.config.SharingConfig
import se.infomaker.livecontentui.section.ContentPresentationAware
import se.infomaker.livecontentui.section.PropertyObjectSectionItem
import se.infomaker.livecontentui.section.SectionItem
import se.infomaker.livecontentui.section.datasource.newspackage.ArticleSectionItem
import se.infomaker.livecontentui.sharing.SharingManager


internal val SectionItem.isRelated: Boolean
    get() = when(this) {
        is ContentPresentationAware -> context?.optBoolean("related") == true
        else -> false
    }

internal val SectionItem.externalLink: String?
    get() = when(this) {
        is PropertyObjectSectionItem -> propertyObject.externalLink
        else -> null
    }

internal val SectionItem.hasExternalLink: Boolean
    get() = externalLink != null

internal fun ArticleSectionItem.registerShown(accessManager: AccessManager, sharingConfig: SharingConfig?, sharingManager: SharingManager, moduleId: String?) {
    val sharingUrl = sharingConfig?.let { config ->
        config.shareApiUrl?.let {
            sharingManager.getSharingUrl(id)
        }
    }
    StatsHelper.logArticleShowStatsEvent(propertyObject,
        moduleId,
        null,
        accessManager.observeAccessAttributes(Observable.just(propertyObject)).firstOrError(),
        sharingUrl?.firstOrError())
}