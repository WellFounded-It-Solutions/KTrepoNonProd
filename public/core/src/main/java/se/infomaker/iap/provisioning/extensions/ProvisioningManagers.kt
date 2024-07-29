package se.infomaker.iap.provisioning.extensions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import se.infomaker.iap.provisioning.ProvisioningManager

suspend fun ProvisioningManager.refreshProducts() = withContext(Dispatchers.IO) {
    val billingJob = launch { billingManager()?.checkCurrentProducts() }
    val loginJob = launch { loginManager()?.checkCurrentProducts() }
    joinAll(billingJob, loginJob)
}