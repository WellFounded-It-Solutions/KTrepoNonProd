package se.infomaker.streamviewer

import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import se.infomaker.frtutilities.AbstractInitContentProvider
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.ForegroundDetector
import se.infomaker.frtutilities.connectivity.Connectivity
import se.infomaker.livecontentui.section.binding.ListHeaderFollowSectionItemBinder
import se.infomaker.livecontentui.section.binding.SectionItemBinderProvider
import se.infomaker.livecontentui.section.supplementary.FactoryKey
import se.infomaker.livecontentui.section.supplementary.ListHeaderFollowSectionItem
import se.infomaker.livecontentui.section.supplementary.SupplementarySectionItemFactoryProvider
import se.infomaker.livecontentui.section.supplementary.SupplementarySectionItemType
import se.infomaker.streamviewer.config.FollowConfig
import se.infomaker.streamviewer.di.SubscriptionManagerFactory
import se.infomaker.streamviewer.extensions.getModuleId

class Init : AbstractInitContentProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface InitEntryPoint {
        fun configManager(): ConfigManager
        fun subscriptionManagerFactory(): SubscriptionManagerFactory
    }

    private val garbage = CompositeDisposable()

    override fun init(context: Context) {
        val entryPoint = EntryPointAccessors.fromApplication(context, InitEntryPoint::class.java)
        val followModuleId = context.getModuleId("Follow")
            ?: context.getModuleId("NearMe")
        followModuleId?.let { moduleId ->
            val config = entryPoint.configManager().getConfig(moduleId, FollowConfig::class.java)
            val subscriptionManager = entryPoint.subscriptionManagerFactory().create(config)

            garbage.add(Observable.combineLatest(Connectivity.observable().distinctUntilChanged(),
                ForegroundDetector.observable(),
                { connected: Boolean, inForeground: Boolean ->
                    connected && inForeground
                })
                .filter { it }
                .subscribe {
                    subscriptionManager.processPendingSubscriptions(context)
                })
        }
        registerSupplementarySectionItemFactories()
        registerSectionItemBinderFactories()
    }

    private fun registerSectionItemBinderFactories() {
        SectionItemBinderProvider.registerFactory(ListHeaderFollowSectionItem::class.java) { propertyBinder, moduleId ->
            ListHeaderFollowSectionItemBinder(propertyBinder, moduleId)
        }
    }

    private fun registerSupplementarySectionItemFactories() {
        val listHeaderKey = FactoryKey("List", SupplementarySectionItemType.HEADER, "Concept")
        SupplementarySectionItemFactoryProvider.register(listHeaderKey) { concept, liveContentConfig ->
            liveContentConfig.conceptTypeUuidsMap?.let { mapping ->
                (mapping[concept.conceptType] ?: mapping["default"])?.let { articleProperty ->
                    ListHeaderFollowSectionItem(concept, articleProperty)
                }
            }
        }
    }
}