package se.infomaker.iap.provisioning.extensions

import se.infomaker.iap.provisioning.LoginManager
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun LoginManager.checkCurrentProducts() = suspendCoroutine<Unit> { continuation ->
    checkCurrentProducts({
        continuation.resume(Unit)
    }, {
        continuation.resumeWithException(it)
    })
}