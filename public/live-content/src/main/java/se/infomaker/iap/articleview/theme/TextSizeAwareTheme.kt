package se.infomaker.iap.articleview.theme

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import se.infomaker.iap.theme.OnThemeUpdateListener
import se.infomaker.iap.theme.ProxyTheme
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.size.ThemeSize
import se.infomaker.iap.theme.style.text.ThemeTextStyle

class TextSizeAwareTheme(private val theme: Theme, lifecycle: Lifecycle, current: Observable<Float>) : ProxyTheme(theme), LifecycleObserver, UpdatableTheme {

    private val garbage = CompositeDisposable()
    private val listeners = mutableSetOf<OnThemeUpdateListener>()
    private var currentTextSizeMap: MutableMap<ThemeTextStyle, ThemeTextStyle>? = null

    private var multiplier = 1.0f
        set(value) {
            if (value != field) {
                field = value
                currentTextSizeMap = checkoutTextSizeMap(multiplier)
                listeners.forEach { it.onThemeUpdated() }
            }
        }

    init {
        garbage.add(current.subscribe {
            multiplier = it
        })

        lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun cleanup() {
        garbage.dispose()
    }

    override fun getText(name: String?, fallback: ThemeTextStyle?): ThemeTextStyle? {
        return multiplySize(super.getText(name, fallback))
    }

    override fun getText(names: MutableList<String>?, fallback: ThemeTextStyle?): ThemeTextStyle? {
        return multiplySize(super.getText(names, fallback))
    }

    override fun getText(fallback: ThemeTextStyle?, vararg names: String?): ThemeTextStyle? {
        return multiplySize(super.getText(fallback, *names))
    }

    private fun multiplySize(textStyle: ThemeTextStyle?): ThemeTextStyle? {

        val sizedTextStyles = currentTextSizeMap ?: return textStyle

        textStyle?.let {

            sizedTextStyles[it]?.let { cached ->
                return cached
            }

            val regularSize = it.getSize(theme)
            return it.buildUpon()
                    .setSize(ThemeSize(regularSize.size * multiplier))
                    .build().also { multiplied ->
                        sizedTextStyles[it] = multiplied
                    }
        }
        return null
    }

    override fun addOnUpdateListener(listener: OnThemeUpdateListener) {
        listeners.add(listener)
    }

    override fun removeOnUpdateListener(listener: OnThemeUpdateListener) {
        listeners.remove(listener)
    }

    companion object {
        private val MULTIPLIED_TEXT_SIZE_MAPS = mutableMapOf<Float, MutableMap<ThemeTextStyle, ThemeTextStyle>>()

        private fun checkoutTextSizeMap(multiplier: Float): MutableMap<ThemeTextStyle, ThemeTextStyle>? {
            if (multiplier == 1.0f) return null
            MULTIPLIED_TEXT_SIZE_MAPS[multiplier]?.let {
                return it
            }
            return mutableMapOf<ThemeTextStyle, ThemeTextStyle>().also {
                MULTIPLIED_TEXT_SIZE_MAPS[multiplier] = it
            }
        }
    }
}