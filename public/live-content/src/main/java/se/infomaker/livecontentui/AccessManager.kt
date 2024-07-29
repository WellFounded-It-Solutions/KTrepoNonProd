package se.infomaker.livecontentui

import android.content.Context
import android.util.Log
import io.reactivex.Observable
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.iap.provisioning.ProvisioningManagerProvider
import se.infomaker.livecontentmanager.parser.PropertyObject
import se.infomaker.livecontentui.config.LiveContentUIConfig
import se.infomaker.livecontentui.livecontentdetailview.frequency.FrequencyManagerProvider

class AccessManager(context: Context, moduleId: String?) {
    private val config = ConfigManager.getInstance(context).getConfig(moduleId ?: "global", LiveContentUIConfig::class.java)
    private val permissions = Premium.getPermissionAsString(config)
    private val provisioningManager = ProvisioningManagerProvider.provide(context)
    private val frequencyManager = FrequencyManagerProvider.provide(context)

    fun isAllContentAccessible(): Boolean{
        return permissions.isEmpty()
    }

    fun observeAccess(content: Observable<PropertyObject>): Observable<Boolean> {
        if (permissions.isEmpty()) {
            return Observable.just(true)
        }

        return Observable.combineLatest(listOf(provisioningManager.canDisplayContentWithPermissions(permissions),
                frequencyManager.canReadFrequencyArticle(content),
                content)) { objects ->
            val canDisplay = objects[0] as Boolean
            val isFreeToRead = objects[1] as Boolean
            val article = objects[2] as PropertyObject

            var isPremiumArticle = false
            article.optString("isPremium")?.let {
                isPremiumArticle = it.toBoolean()
            }
            return@combineLatest canDisplay || !isPremiumArticle || isFreeToRead
        }
    }

    fun observeAccessAttributes(content: Observable<PropertyObject>): Observable<Map<String,Any>> {
        return observeAccess(content).map { hasAccess ->
            return@map mapOf("userHasAccess" to hasAccess)
        }
    }
}