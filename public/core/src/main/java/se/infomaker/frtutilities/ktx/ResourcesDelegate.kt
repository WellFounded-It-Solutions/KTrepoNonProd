package se.infomaker.frtutilities.ktx

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import se.infomaker.frtutilities.ResourceManager
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun Fragment.resources(
    moduleIdProducer: () -> String? = { arguments?.getString("moduleId") }
): ResourcesDelegate {
    return ResourcesDelegate(fragment = this, moduleIdProducer = moduleIdProducer)
}

fun Activity.resources(
    moduleIdProducer: () -> String? = { intent?.getStringExtra("moduleId") }
): ResourcesDelegate {
    return (this as Context).resources(moduleIdProducer)
}

fun Context.resources(
    moduleIdProducer: () -> String? = { null }
): ResourcesDelegate {
    return ResourcesDelegate(this, moduleIdProducer = moduleIdProducer)
}

class ResourcesDelegate(
    private val context: Context? = null,
    private val fragment: Fragment? = null,
    private val moduleIdProducer: () -> String?
) : ReadOnlyProperty<Any?, ResourceManager> {
    private var cached: ResourceManager? = null
    private val currentContext: Context
        get() = context ?: fragment?.requireContext() ?: throw IllegalStateException("No available context to initialize theme.")

    override fun getValue(thisRef: Any?, property: KProperty<*>): ResourceManager {
        val resources = cached
        return if (resources == null) {
            val moduleId = moduleIdProducer()
            ResourceManager(currentContext, moduleId).also {
                cached = it
            }
        }
        else {
            resources
        }
    }
}