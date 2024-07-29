package se.infomaker.frt.integration

import android.content.Context
import se.infomaker.frt.moduleinterface.ModuleIconProvider
import se.infomaker.frt.moduleinterface.ModuleIntegration
import com.navigaglobal.mobile.profile.R

class MyProfileIntegration(context: Context, private val moduleId: String) : ModuleIntegration, ModuleIconProvider {

    override val moduleIcon: Int
        get() = R.drawable.default_my_profile_icon

    override fun getId() = moduleId
}