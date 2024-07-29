package se.infomaker.frtutilities.ktx

import android.app.Activity
import androidx.fragment.app.Fragment
import se.infomaker.frtutilities.ModuleInformation
import se.infomaker.frtutilities.ModuleInformationManager
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun Fragment.moduleInfo(
    moduleIdProducer: () -> String? = { arguments?.getString("moduleId") },
    moduleTitleProducer: () -> String? = { arguments?.getString("moduleTitle") },
    moduleNameProducer: () -> String? = { arguments?.getString("moduleName") },
    modulePromotionProducer: () -> String? = { arguments?.getString("modulePromotion") }
): ModuleInformationDelegate {
    return ModuleInformationDelegate(moduleIdProducer, moduleTitleProducer, moduleNameProducer, modulePromotionProducer)
}

fun Activity.moduleInfo(
    moduleIdProducer: () -> String? = { intent?.getStringExtra("moduleId") },
    moduleTitleProducer: () -> String? = { intent?.getStringExtra("moduleTitle") },
    moduleNameProducer: () -> String? = { intent?.getStringExtra("moduleName") },
    modulePromotionProducer: () -> String? = { intent?.getStringExtra("modulePromotion") }
): ModuleInformationDelegate {
    return ModuleInformationDelegate(moduleIdProducer, moduleTitleProducer, moduleNameProducer, modulePromotionProducer)
}

fun moduleInfo(
    moduleIdProducer: () -> String? = { null },
    moduleTitleProducer: () -> String? = { null },
    moduleNameProducer: () -> String? = { null },
    modulePromotionProducer: () -> String? = { null }
): ModuleInformationDelegate {
    return ModuleInformationDelegate(moduleIdProducer, moduleTitleProducer, moduleNameProducer, modulePromotionProducer)
}

class ModuleInformationDelegate(
    private val moduleIdProducer: () -> String?,
    private val moduleTitleProducer: () -> String?,
    private val moduleNameProducer: () -> String?,
    private val modulePromotionProducer: () -> String?
) : ReadOnlyProperty<Any?, ModuleInformation> {
    private var cached: ModuleInformation? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): ModuleInformation {
        val moduleInformation = cached
        return if (moduleInformation == null) {
            val moduleId = moduleIdProducer.invoke()
            val moduleTitle = moduleTitleProducer.invoke() ?: ModuleInformationManager.getInstance().getModuleTitle(moduleId)
            val moduleName = moduleNameProducer.invoke() ?: ModuleInformationManager.getInstance().getModuleName(moduleId)
            val modulePromotion = modulePromotionProducer.invoke()
            ModuleInformation(moduleId, moduleTitle, moduleName, modulePromotion).also {
                cached = it
            }
        }
        else {
            moduleInformation
        }
    }
}