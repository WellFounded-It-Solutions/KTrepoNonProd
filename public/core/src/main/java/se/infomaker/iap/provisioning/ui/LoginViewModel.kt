package se.infomaker.iap.provisioning.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import se.infomaker.iap.provisioning.LoginStatus
import se.infomaker.iap.provisioning.ProvisioningManager
import se.infomaker.iap.provisioning.ProvisioningManagerProvider

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val provisioningManager: ProvisioningManager = ProvisioningManagerProvider.provide(application)

    private val isLoading = MutableLiveData<Boolean>()

    fun isLoading() : LiveData<Boolean> = isLoading

    fun isLoggedIn() : Boolean {
        return provisioningManager.loginManager()?.getLoginStatus() == LoginStatus.LOGGED_IN
    }

    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        isLoading.value = true
        provisioningManager.loginManager()?.login(email, password, {
            isLoading.value = false
            onSuccess()
        },{
            isLoading.value = false
            onError(it)
        })
    }
}
