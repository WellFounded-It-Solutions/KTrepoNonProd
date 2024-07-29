package se.infomaker.livecontentui.livecontentrecyclerview.fragment

import io.reactivex.Observable
import se.infomaker.frtutilities.meta.ValueProvider
import se.infomaker.livecontentmanager.parser.PropertyObject

class PropertyObjectValueProvider(val propertyObject: PropertyObject) : ValueProvider {
    override fun observeString(keyPath: String): Observable<String>? {
        val string = propertyObject.optString(keyPath)
        return if (string != null) Observable.just(string) else null
    }

    override fun getStrings(keyPath: String): List<String>? = propertyObject.optStringList(keyPath)
    override fun getString(keyPath: String): String? = propertyObject.optString(keyPath)
}
