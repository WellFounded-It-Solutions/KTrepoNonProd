package se.infomaker.iap

import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import se.infomaker.frt.remotenotification.RemoteNotificationManager
import se.infomaker.frtutilities.AbstractInitContentProvider
import se.infomaker.iap.action.ActionManager
import se.infomaker.iap.action.DialogAction
import se.infomaker.iap.action.stack.PopToRootActionHandler
import se.infomaker.iap.action.SequenceAction
import se.infomaker.iap.action.SpringboardRestartAction
import se.infomaker.iap.action.SwitchAction
import se.infomaker.iap.action.display.DisplayManager
import se.infomaker.iap.action.display.flow.FlowFragment
import se.infomaker.iap.action.display.module.ModuleFragmentFactory
import se.infomaker.iap.action.display.tabs.TabsFactory
import se.infomaker.iap.action.display.toBundle
import se.infomaker.iap.action.http.HttpAction
import se.infomaker.iap.action.module.ModuleActionHandler
import se.infomaker.iap.action.open.OpenActionHandler
import se.infomaker.iap.action.value.ValueAction
import se.infomaker.iap.update.notification.UpdateInterceptor


class BaseSetup : AbstractInitContentProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BaseSetupEntryPoint {
        fun httpAction(): HttpAction
        fun updateInterceptor(): UpdateInterceptor
    }

    override fun init(context: Context) {
        val entryPoint = context.baseSetupEntryPoint

        ActionManager.register("http-action", entryPoint.httpAction())
        ActionManager.register("switch", SwitchAction)
        ActionManager.register("open", OpenActionHandler)
        ActionManager.register("sequence", SequenceAction)
        ActionManager.register("show-dialog", DialogAction)
        ActionManager.register("set-values", ValueAction)
        ActionManager.register("open-module", ModuleActionHandler)
        ActionManager.register("register-statistics", StatisticsActionHandler)
        ActionManager.register("pop-to-root", PopToRootActionHandler)
        ValueAction.load(context)

        DisplayManager.register("display-tabs") { operation ->
            TabsFactory.createFragment(context, operation)
        }
        DisplayManager.register("display-flow") { operation ->
            FlowFragment().apply {
                arguments = operation.toBundle()
            }
        }
        DisplayManager.register("display-module") { operation ->
            ModuleFragmentFactory.createFragment(context, operation.getParameter("moduleName"), operation.parametersAsBundle())
        }
        ActionManager.registerGroupHandler("display", DisplayManager)

        ActionManager.register("springboard-restart", SpringboardRestartAction)

        val updateInterceptor = entryPoint.updateInterceptor()
        RemoteNotificationManager.registerInterceptor(updateInterceptor)
    }
}

private val Context.baseSetupEntryPoint
    get() = EntryPointAccessors.fromApplication(this, BaseSetup.BaseSetupEntryPoint::class.java)