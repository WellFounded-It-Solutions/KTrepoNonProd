package se.infomaker.iap.action.display.module

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import se.infomaker.frt.moduleinterface.ModuleInterface
import se.infomaker.iap.action.module.InvalidModuleException
import se.infomaker.iap.action.module.Module
import timber.log.Timber

object ModuleFragmentFactory {
    @Throws(InvalidModuleException::class)
    fun createFragment(context: Context?, moduleName: String?, bundle: Bundle = Bundle()): androidx.fragment.app.Fragment {
        context ?: let { throw InvalidModuleException("Context cannot be null") }
        moduleName ?: let { throw InvalidModuleException("No module name") }

        return try {
            val className = Module.fullModuleFragmentClassName(moduleName)
            Class.forName(className)
            androidx.fragment.app.Fragment.instantiate(context, className).apply {
                arguments = bundle
                if (this@apply !is ModuleInterface) {
                    Timber.w("Module does not implement ModuleInterface.")
                }
            }
        } catch (e: ClassNotFoundException) {
            throw InvalidModuleException(e)
        }
    }
}