package se.infomaker.iap.standardcomponents

import android.content.Context
import se.infomaker.iap.theme.ThemeInjector
import se.infomaker.frtutilities.AbstractInitContentProvider

class Setup : AbstractInitContentProvider() {
    override fun init(context: Context) {
        context.assets.open("standard_theme.json").bufferedReader().use {
            ThemeInjector.getInstance().inject(it.readText())
        }
    }
}
