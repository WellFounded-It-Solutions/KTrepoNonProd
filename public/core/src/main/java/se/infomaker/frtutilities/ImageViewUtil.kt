package se.infomaker.frtutilities

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.widget.ImageView
import androidx.core.graphics.drawable.DrawableCompat

fun ImageView.setTint(tintColor: Int) {
    if (android.os.Build.VERSION.SDK_INT >= 21) {
        imageTintList = ColorStateList.valueOf(tintColor)
    } else {
        val wrappedDrawable = DrawableCompat.wrap(drawable).mutate()
        wrappedDrawable.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
        setImageDrawable(wrappedDrawable)
    }
}

fun ImageView.setImageResourceWithTint(imageResource: Int, tintColor: Int) {
    this.setImageResource(imageResource)
    setTint(tintColor)
}