package se.infomaker.iap.articleview.item

import se.infomaker.iap.articleview.extensions.ifragasatt.IfragasattItemViewFactory
import se.infomaker.iap.articleview.item.element.ElementItemViewFactory
import se.infomaker.iap.articleview.item.element.ElementListItemViewFactory
import se.infomaker.iap.articleview.item.embed.HtmlEmbedItemViewFactory
import se.infomaker.iap.articleview.item.fallback.FallbackItemViewFactory
import se.infomaker.iap.articleview.item.flowplayer.FlowPlayerItemViewFactory
import se.infomaker.iap.articleview.item.image.ImageGalleryItemViewFactory
import se.infomaker.iap.articleview.item.image.ImageItemViewFactory
import se.infomaker.iap.articleview.item.links.LinksItemViewFactory
import se.infomaker.iap.articleview.item.map.MapItemViewFactory
import se.infomaker.iap.articleview.item.mergedelement.MergedElementItemViewFactory
import se.infomaker.iap.articleview.item.screen9.Screen9ItemViewFactory
import se.infomaker.iap.articleview.item.table.TableItemViewFactory
import se.infomaker.iap.articleview.item.unsupported.UnsupportedItemViewFactory
import se.infomaker.iap.articleview.item.youplay.YouPlayItemViewFactory
import se.infomaker.iap.map.MapViewHolderFactory
import javax.inject.Inject
import javax.inject.Singleton

// TODO Use multibinding for all view factories
@Singleton
class DefaultItemViewFactoryProvider @Inject constructor(
    mapViewHolderFactory: MapViewHolderFactory
) : ItemViewFactoryProvider {
    val factories = mutableMapOf<Any, ItemViewFactory>()
    val fallback = UnsupportedItemViewFactory()

    init {
        registerViewFactory(TableItemViewFactory())
        registerViewFactory(MapItemViewFactory(mapViewHolderFactory))
        registerViewFactory(YouPlayItemViewFactory())
        registerViewFactory(FlowPlayerItemViewFactory())
        registerViewFactory(HtmlEmbedItemViewFactory())
        registerViewFactory(ImageGalleryItemViewFactory())
        registerViewFactory(ElementItemViewFactory())
        registerViewFactory(ImageItemViewFactory())
        registerViewFactory(UnsupportedItemViewFactory())
        registerViewFactory(FallbackItemViewFactory())
        registerViewFactory(LinksItemViewFactory())
        registerViewFactory(MergedElementItemViewFactory())
        registerViewFactory(IfragasattItemViewFactory())
        registerViewFactory(Screen9ItemViewFactory())
        registerViewFactory(ElementListItemViewFactory())
    }

    fun registerViewFactory(factory: ItemViewFactory) {
        factories[factory.typeIdentifier()] = factory
    }

    override fun viewFactoryForType(typeIdentifier: Any): ItemViewFactory {
        return factories[typeIdentifier] ?: fallback
    }

    override fun viewFactoryForItem(item: Item): ItemViewFactory {
        return viewFactoryForType(item.typeIdentifier)
    }
}