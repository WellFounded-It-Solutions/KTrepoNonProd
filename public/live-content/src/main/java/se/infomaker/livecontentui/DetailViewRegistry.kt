package se.infomaker.livecontentui

import se.infomaker.livecontentui.livecontentdetailview.pageadapters.DetailFragmentFactory

object DetailViewRegistry {
    private val registry = mutableMapOf<String, DetailFragmentFactory>()

    fun put(name:String, factory: DetailFragmentFactory) {
        registry[name] = factory
    }

    fun get(name: String) : DetailFragmentFactory? {
        return registry[name]
    }
}