package se.infomaker.iap.provisioning.action

import android.content.Context
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import se.infomaker.frtutilities.ktx.findActivity
import se.infomaker.frtutilities.ktx.requireActivity
import se.infomaker.iap.action.ObservableActionHandler
import se.infomaker.iap.action.Operation
import se.infomaker.iap.action.Result
import se.infomaker.iap.provisioning.LoginManager
import se.infomaker.iap.provisioning.LoginStatus
import se.infomaker.iap.provisioning.LoginTypeProvider
import se.infomaker.iap.provisioning.ProvisioningManagerProvider
import se.infomaker.iap.provisioning.backend.LoginType
import se.infomaker.iap.provisioning.ui.restartApp

class LogoutActionHandler(private val loginManager: LoginManager) : ObservableActionHandler {

    override fun canPerform(context: Context, operation: Operation): Boolean {
        return loginManager.getLoginStatus() == LoginStatus.LOGGED_IN && (loginManager as? LoginTypeProvider)?.getLoginType() != LoginType.TEMPORARILY_DISABLED
    }

    override fun observeCanPerform(context: Context, operation: Operation): Observable<Boolean> {
        return Observable.combineLatest(loginManager.loginStatus(), loginManager.loginType(), BiFunction { status, type ->
            status == LoginStatus.LOGGED_IN && type != LoginType.TEMPORARILY_DISABLED
        })
    }

    override fun perform(context: Context, operation: Operation, onResult: (Result) -> Unit) {
        loginManager.logout(context.requireActivity()) {
            if (ProvisioningManagerProvider.provide(context).hasAppStartPaywall()) {
                ProvisioningManagerProvider.provide(context).checkPermissionToPassPaywall({ canPass ->
                    if (!canPass) {
                        (context.findActivity())?.restartApp()
                    }
                    onResult.invoke(Result(success = true))
                }, {
                    onResult.invoke(Result(success = true))
                })
            }
            else {
                onResult.invoke(Result(success = true))
            }
        }
    }
}
