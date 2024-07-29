package se.infomaker.iap.ui

import android.content.Context
import se.infomaker.frtutilities.AbstractInitContentProvider
import se.infomaker.iap.action.ActionManager
import se.infomaker.iap.ui.action.ResetPromotionsActionHandler

class UiSetup : AbstractInitContentProvider() {
    override fun init(context: Context) {
        ActionManager.register("reset-promotions", ResetPromotionsActionHandler())
    }
}