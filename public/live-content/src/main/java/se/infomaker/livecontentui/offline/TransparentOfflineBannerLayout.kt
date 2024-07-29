package se.infomaker.livecontentui.offline

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import androidx.annotation.IntRange
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.ColorUtils
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.color.ThemeColor
import com.navigaglobal.mobile.livecontent.R

class TransparentOfflineBannerLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : OfflineBannerLayout(context, attrs) {

    private var currentAlpha = 0
    private var theme: Theme? = null

    override fun apply(theme: Theme) {
        super.apply(theme)
        this.theme = theme

        background = AppCompatResources.getDrawable(context, R.drawable.translucent_app_bar_offline_banner_background)
        updateBackground(currentAlpha)
    }

    fun updateBackground(@IntRange(from = 0x0, to = 0xFF) alpha: Int, animate: Boolean = false) {
        val backgroundColor = theme?.let {
            getBackgroundColor(theme, ThemeColor.BLACK).get()
        } ?: run {
            Color.BLACK
        }
        if (animate) {
            val animator = ValueAnimator.ofInt(currentAlpha, alpha)
            animator.addUpdateListener {
                updateBackgroundColor(ColorUtils.setAlphaComponent(backgroundColor, it.animatedValue as Int))
            }
            animator.duration = 300
            animator.start()
        }
        else {
            updateBackgroundColor(ColorUtils.setAlphaComponent(backgroundColor, alpha))
        }
        currentAlpha = alpha
    }

    private fun updateBackgroundColor(backgroundColor: Int) {
        ((background as? LayerDrawable)?.findDrawableByLayerId(R.id.background_color) as? ColorDrawable)?.let {
            it.mutate()
            it.color = backgroundColor
        }
    }
}