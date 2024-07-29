package se.infomaker.iap.provisioning.extensions

import se.infomaker.iap.provisioning.billing.BillingManager
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


suspend fun BillingManager.checkCurrentProducts() = suspendCoroutine<Unit>{ continuation ->
    checkCurrentProducts({
        continuation.resume(Unit)
    }, {
        continuation.resumeWithException(it)
    })
}