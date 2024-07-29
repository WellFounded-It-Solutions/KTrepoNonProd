package se.infomaker.iap.action.module

import android.content.Context
import android.content.Intent
import android.os.Bundle
import timber.log.Timber

object Module {

    const val MODULE_ID = "moduleId"
    private const val MODULE_FRAGMENT_PACKAGE_NAME = "se.infomaker.frt.ui.fragment"
    private const val MODULE_FRAGMENT_SUFFIX = "Fragment"

    @Throws(InvalidModuleException::class)
    fun open(context: Context, moduleName: String, arguments: Bundle) {
        if (!isValid(moduleName)) {
            throw InvalidModuleException("$moduleName does not exist")
        }

        Timber.e("Module.kt ModuleName: %s, Arguments: %s", moduleName, arguments)

        val intent = Intent(context, ModuleActivity::class.java)
        intent.putExtra(ModuleActivity.MODULE_NAME, moduleName)
        intent.putExtras(arguments)
        context.startActivity(intent)
    }

    fun isValid(moduleName: String): Boolean {
        val className = fullModuleFragmentClassName(moduleName)
        return try {
            Class.forName(className)
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    fun fullModuleFragmentClassName(moduleName: String): String {
        return "$MODULE_FRAGMENT_PACKAGE_NAME.$moduleName$MODULE_FRAGMENT_SUFFIX"
    }
}
