package se.infomaker.iap.articleview.item.decorator

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.util.UI
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.color.ThemeColor


data class BorderDimensions(val left: Float = 0F, val top: Float = 0F, val right: Float = 0F, val bottom: Float = 0F)

data class BackgroundDrawableDecorator(val drawableId: Int, val background: DecoratorColors, val borderColor: DecoratorColors, val borderDimensions: BorderDimensions) : ItemDecorator {

    override fun decorate(item: Item, itemView: View, theme: Theme) {
        ResourcesCompat.getDrawable(itemView.context.resources, drawableId, null)?.let { it ->
            val layerDrawable = it as? LayerDrawable
            val bgColor = background.getColor(theme, ThemeColor.TRANSPARENT)
            if (bgColor != ThemeColor.TRANSPARENT) {
                layerDrawable?.let {
                    it.findDrawableByLayerId(R.id.decorator_border)?.overrideColor(borderColor.getColor(theme, ThemeColor.TRANSPARENT).get())
                    it.findDrawableByLayerId(R.id.decorator_bg)?.overrideColor(bgColor.get())

                    repeat(it.numberOfLayers) { index ->
                        if (it.getId(index) == R.id.decorator_bg) {
                            it.setLayerInset(index,
                                    UI.dp2px(borderDimensions.left).toInt(),
                                    UI.dp2px(borderDimensions.top).toInt(),
                                    UI.dp2px(borderDimensions.right).toInt(),
                                    UI.dp2px(borderDimensions.bottom).toInt())
                            return@repeat
                        }
                    }
                }
                itemView.background = layerDrawable
            }
        }
    }
}

fun Drawable.overrideColor(@ColorInt colorInt: Int) {
    when (this) {
        is GradientDrawable -> setColor(colorInt)
        is ShapeDrawable -> paint.color = colorInt
        is ColorDrawable -> color = colorInt
    }
}