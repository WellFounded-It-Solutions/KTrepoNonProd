package se.infomaker.iap.articleview.item


class LocalItemViewFactoryProvider(
    private val factories: Map<Any, ItemViewFactory>,
    private val fallbackProvider: ItemViewFactoryProvider
): ItemViewFactoryProvider {

    override fun viewFactoryForType(typeIdentifier: Any): ItemViewFactory {
        return factories[typeIdentifier] ?: fallbackProvider.viewFactoryForType(typeIdentifier)
    }

    override fun viewFactoryForItem(item: Item): ItemViewFactory {
        return viewFactoryForType(item.typeIdentifier)
    }
}