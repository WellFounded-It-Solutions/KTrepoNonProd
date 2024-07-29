package se.infomaker.iap.articleview.item

/**
 * Provides [ItemViewFactory] for view creation/binding/themeing.
 */
interface ItemViewFactoryProvider {

    /**
     * Returns a factory for a type identifier
     */
    fun viewFactoryForType(typeIdentifier: Any): ItemViewFactory

    /**
     * Returns a factory that can create views for the given item
     */
    fun viewFactoryForItem(item: Item): ItemViewFactory
}