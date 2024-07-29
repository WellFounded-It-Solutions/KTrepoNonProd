package se.infomaker.frtutilities.ktx

import android.graphics.Color
import android.os.Build
import android.view.Window
import android.view.WindowManager
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import se.infomaker.iap.theme.ktx.darkenColor
import se.infomaker.iap.theme.ktx.isBrightColor

fun Window.applyStatusBarColor(original: Int) {
    var color = original
    if (Build.VERSION.SDK_INT >= 23) {
        val controller = WindowInsetsControllerCompat(this, decorView)
        controller.isAppearanceLightStatusBars = color.isBrightColor()
    }
    else {
        while (color.isBrightColor()) {
            color = color.darkenColor()
        }
    }

    addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    statusBarColor = color
}

fun Window.setStatusBarTranslucent() {
    addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    WindowCompat.setDecorFitsSystemWindows(this, false)
    statusBarColor = ColorUtils.setAlphaComponent(Color.BLACK, 127)
}