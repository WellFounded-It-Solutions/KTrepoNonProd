package se.infomaker.iap.theme.ktx

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import se.infomaker.iap.theme.OnThemeUpdateListener
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.ThemeManager
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun Fragment.theme(
    onThemeUpdated: OnThemeUpdateListener? = null,
    moduleIdProducer: (() -> String?) = { arguments?.getString("moduleId") }
): ThemeDelegate {
    return ThemeDelegate(fragment = this, listener = onThemeUpdated, moduleIdProducer = moduleIdProducer)
}

fun Activity.theme(
    onThemeUpdated: OnThemeUpdateListener? = null,
    moduleIdProducer: (() -> String?) = { intent?.getStringExtra("moduleId") }
): ThemeDelegate {
    return ThemeDelegate(this, listener = onThemeUpdated, moduleIdProducer = moduleIdProducer)
}

fun Context.theme(
    onThemeUpdated: OnThemeUpdateListener? = null,
    moduleIdProducer: (() -> String?)? = { null }
): ThemeDelegate {
    return ThemeDelegate(this, listener = onThemeUpdated, moduleIdProducer = moduleIdProducer)
}

class ThemeDelegate(
    private val _context: Context? = null,
    private val fragment: Fragment? = null,
    private val listener: OnThemeUpdateListener? = null,
    private val moduleIdProducer: (() -> String?)? = { null }
) : ReadOnlyProperty<Any?, Theme>, LifecycleObserver, OnThemeUpdateListener {

    private val context: Context?
        get() = _context ?: fragment?.context

    private val requiredContext: Context
        get() = _context ?: fragment?.requireContext() ?: throw IllegalStateException("No available context to initialize ThemeManager.")

    private val lifecycleOwner: LifecycleOwner?
        get() = fragment ?: context as? LifecycleOwner

    private var cached: Theme? = null

    init {
        if (listener != null) {
            lifecycleOwner?.lifecycle?.addObserver(this)
        }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Theme {
        val theme = cached
        return if (theme == null) {
            val moduleId = moduleIdProducer?.invoke()
            ThemeManager.getInstance(requiredContext).getModuleTheme(moduleId).also {
                cached = it
            }
        }
        else {
            theme
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onStart() {
        context?.let { ThemeManager.getInstance(it).addOnUpdateListener(this) }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onStop() {
        context?.let { ThemeManager.getInstance(it).removeOnUpdateListener(this) }
    }

    override fun onThemeUpdated() {
        cached = null
        listener?.onThemeUpdated()
    }
}