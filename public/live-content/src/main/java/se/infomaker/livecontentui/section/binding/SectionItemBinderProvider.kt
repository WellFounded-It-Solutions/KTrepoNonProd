package se.infomaker.livecontentui.section.binding

import se.infomaker.livecontentui.livecontentrecyclerview.binder.PropertyBinder
import se.infomaker.livecontentui.section.SectionItem

private typealias Factory = (PropertyBinder, String?) -> SectionItemBinder

object SectionItemBinderProvider {
    private val factories = mutableMapOf<Class<out SectionItem>, Factory>()

    fun registerFactory(clazz: Class<out SectionItem>, factory: Factory) {
        factories[clazz] = factory
    }

    @JvmStatic
    fun all(propertyBinder: PropertyBinder, moduleId: String?): Map<Class<out SectionItem>, SectionItemBinder> {
        return factories.mapValues { it.value.invoke(propertyBinder, moduleId) }
    }
}