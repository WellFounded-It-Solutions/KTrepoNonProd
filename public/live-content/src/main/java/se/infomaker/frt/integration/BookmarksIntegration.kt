package se.infomaker.frt.integration

import android.content.Context
import se.infomaker.frt.moduleinterface.ModuleIconProvider
import se.infomaker.frt.moduleinterface.ModuleIntegration
import com.navigaglobal.mobile.livecontent.R

class BookmarksIntegration(context: Context, private val moduleIdentifier: String) : ModuleIntegration, ModuleIconProvider {
    override val moduleIcon = R.drawable.default_bookmarks_module_icon
    override fun getId() = moduleIdentifier
}