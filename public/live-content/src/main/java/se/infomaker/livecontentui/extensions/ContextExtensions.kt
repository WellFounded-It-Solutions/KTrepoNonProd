package se.infomaker.livecontentui.extensions

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager

internal fun Context.findFragmentManager(): FragmentManager? {
    return when (this) {
        is AppCompatActivity -> supportFragmentManager
        is ContextWrapper -> baseContext.findFragmentManager()
        else -> null
    }
}