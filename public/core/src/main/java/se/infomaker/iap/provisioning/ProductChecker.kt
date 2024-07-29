package se.infomaker.iap.provisioning

import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import se.infomaker.iap.provisioning.billing.BillingManager
import se.infomaker.iap.provisioning.firebase.containsAny
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Check both product providers ([LoginManager] and [BillingManager]) for products,
 * if they both exist.
 */
class ProductChecker(
        private val loginManager: LoginManager,
        private val billingManager: BillingManager?
) {
    suspend fun checkFor(requiredProducts: List<String>, onResult: (Boolean) -> Unit, onError: (Throwable) -> Unit) = coroutineScope {

        var result = false
        var exception: Exception? = null
        val jobs = mutableListOf<Job>()

        billingManager?.let {
            try {
                jobs.add(launch { result = result || it.suspendCheckCurrentProductsContains(requiredProducts) })
            }
            catch (e: Exception) {
                exception = e
            }
        }

        try {
            jobs.add(launch { result = result || loginManager.suspendCheckCurrentProductsContains(requiredProducts) })
        }
        catch (e: Exception) {
            exception = e
        }

        jobs.joinAll()

        val safeException = exception
        when {
            result -> onResult.invoke(result)
            safeException != null -> onError.invoke(safeException)
            else -> onResult.invoke(result)
        }
    }
}

private suspend fun LoginManager.suspendCheckCurrentProductsContains(requiredProducts: List<String>): Boolean = suspendCoroutine { continuation ->
    checkCurrentProducts({ loginManagerResult ->
        Timber.d("Got result from loginmanager ${loginManagerResult.size}")
        continuation.resume(loginManagerResult.map { it.name }.containsAny(requiredProducts))
    }, { error ->
        continuation.resumeWithException(error)
    })
}

private suspend fun BillingManager.suspendCheckCurrentProductsContains(requiredProducts: List<String>): Boolean = suspendCoroutine { continuation ->
    checkCurrentProducts({ billingManagerResult ->
        Timber.d("Got result from billingmanager ${billingManagerResult.size}")
        continuation.resume(billingManagerResult.containsAny(requiredProducts))
    }, {
        val functionsException = it as? FirebaseFunctionsException
        if (functionsException != null) {
            continuation.resume(false)
        }
        else {
            continuation.resumeWithException(it)
        }
    })
}