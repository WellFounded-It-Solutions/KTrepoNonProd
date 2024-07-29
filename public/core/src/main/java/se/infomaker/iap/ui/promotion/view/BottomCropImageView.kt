package se.infomaker.iap.ui.promotion.view

import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class BottomCropImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    init {
        scaleType = ScaleType.MATRIX
    }

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        
        val viewWidth = (measuredWidth - paddingLeft - paddingRight).toFloat()
        val viewHeight = (measuredHeight - paddingTop - paddingBottom).toFloat()
        val drawableWidth = (drawable?.intrinsicWidth ?: 0).toFloat()
        val drawableHeight = (drawable?.intrinsicHeight ?: 0).toFloat()

        val scale = if (drawableWidth * viewHeight > drawableHeight * viewWidth) {
            viewHeight / drawableHeight
        }
        else {
            viewWidth / drawableWidth
        }

        val drawableRect = RectF(0f, drawableHeight - viewHeight / scale, drawableWidth, drawableHeight)
        val viewRect = RectF(0f, 0f, viewWidth, viewHeight)

        imageMatrix = imageMatrix.apply { setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.FILL) }

        return super.setFrame(l, t, r, b)
    }
}