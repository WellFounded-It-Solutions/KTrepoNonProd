package se.infomaker.iap.articleview.view

import android.content.Context
import android.util.AttributeSet
import androidx.lifecycle.Lifecycle
import se.infomaker.iap.theme.view.ThemeableFrameLayout

class LifecycleProxyFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ThemeableFrameLayout(context, attrs, defStyleAttr), LifecycleProxy {

    private var lifecycle: Lifecycle? = null

    override fun getLifecycle() = lifecycle

    override fun setLifecycle(lifecycle: Lifecycle?) {
        this.lifecycle = lifecycle
    }
}