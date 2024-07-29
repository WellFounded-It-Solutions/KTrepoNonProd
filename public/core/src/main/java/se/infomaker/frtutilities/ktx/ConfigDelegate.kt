package se.infomaker.frtutilities.ktx

import android.app.Activity
import android.app.Application
import android.content.ContentProvider
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.ModuleInformation
import se.infomaker.frtutilities.runtimeconfiguration.OnConfigChangeListener
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

inline fun <reified C : Any> AndroidViewModel.config(
    onConfigChanged: OnConfigChangeListener? = null,
    noinline moduleInformationProducer: (() -> ModuleInformation?) = { null }
): ConfigDelegate<C> {
    return (getApplication() as Application).config(onConfigChanged, moduleInformationProducer)
}

inline fun <reified C : Any> Fragment.config(
    onConfigChanged: OnConfigChangeListener? = null,
    noinline moduleInformationProducer: (() -> ModuleInformation?) = {
        val moduleId = arguments?.getString("moduleId")
        val moduleName = arguments?.getString("moduleName")
        ModuleInformation(moduleId, name = moduleName)
    }
): ConfigDelegate<C> {
    return ConfigDelegate(C::class, fragment = this, onConfigChanged = onConfigChanged, moduleInformationProducer = moduleInformationProducer)
}

inline fun <reified C : Any> Activity.config(
    onConfigChanged: OnConfigChangeListener? = null,
    noinline moduleInformationProducer: (() -> ModuleInformation?) = {
        val moduleId = intent?.getStringExtra("moduleId")
        val moduleName = intent?.getStringExtra("moduleName")
        ModuleInformation(moduleId, name = moduleName)
    }
): ConfigDelegate<C> {
    return (this as Context).config(onConfigChanged, moduleInformationProducer)
}

inline fun <reified C : Any> ContentProvider.config(
    onConfigChanged: OnConfigChangeListener? = null,
    noinline moduleInformationProducer: (() -> ModuleInformation?) = { null }
): ConfigDelegate<C> {
    return context.config(onConfigChanged, moduleInformationProducer)
}

inline fun <reified C : Any> Context?.config(
    onConfigChanged: OnConfigChangeListener? = null,
    noinline moduleInformationProducer: (() -> ModuleInformation?) = { null }
): ConfigDelegate<C> {
    return ConfigDelegate(C::class, this, onConfigChanged = onConfigChanged, moduleInformationProducer = moduleInformationProducer)
}

inline fun <reified C : Any> config(
    onConfigChanged: OnConfigChangeListener? = null,
    noinline moduleInformationProducer: (() -> ModuleInformation?) = { null }
): ConfigDelegate<C> {
    return ConfigDelegate(C::class, onConfigChanged = onConfigChanged, moduleInformationProducer = moduleInformationProducer)
}

class ConfigDelegate<C : Any>(
    private val configClass: KClass<C>,
    private val _context: Context? = null,
    private val fragment: Fragment? = null,
    private val onConfigChanged: OnConfigChangeListener? = null,
    private val moduleInformationProducer: (() -> ModuleInformation?)? = { null }
) : ReadOnlyProperty<Any?, C>, LifecycleObserver, OnConfigChangeListener {

    private val context: Context?
        get() = _context ?: fragment?.context

    private val configManager: ConfigManager
        get() = context?.let { ConfigManager.getInstance(it) } ?: ConfigManager.getInstance()

    private val lifecycleOwner: LifecycleOwner?
        get() = fragment ?: context as? LifecycleOwner

    private var cached: C? = null

    init {
        lifecycleOwner?.lifecycle?.addObserver(this)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): C {
        val config = cached
        return if (config == null) {
            val moduleInformation = moduleInformationProducer?.invoke()
            configManager.getConfig(moduleInformation?.name, moduleInformation?.identifier, configClass.java).also {
                cached = it
            }
        }
        else {
            config
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onStart() {
        configManager.addOnConfigChangeListener(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onStop() {
        configManager.removeOnConfigChangeListener(this)
    }

    override fun onChange(updated: MutableList<String>, removed: MutableList<String>): MutableSet<String> {
        cached = null
        return onConfigChanged?.onChange(updated, removed) ?: mutableSetOf()
    }
}