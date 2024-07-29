package se.infomaker.iap.provisioning.credentials

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.atomic.AtomicInteger

object CredentialManager {
    private val providers = LinkedHashSet<CredentialProvider>()
    val liveCredentials = MutableLiveData<List<Credential>>()

    fun registerCredentialProvider(provider: CredentialProvider) {
        providers.add(provider)
    }

    fun removeCredentialProvider(provider: CredentialProvider) {
        providers.remove(provider)
    }

    fun forceRefresh(context: Context, onDone: () -> Unit){
        val countDown = AtomicInteger(providers.size)

        val out = LinkedHashSet<Credential>()

        providers.forEach {
            it.availableCredentials(context) { credentials ->

                out.addAll(credentials)

                val count = countDown.decrementAndGet()
                if (count == 0) {
                    liveCredentials.postValue(out.toList())
                    onDone.invoke()
                }
            }
        }
        if (countDown.get() == 0) {
            liveCredentials.postValue(out.toList())
            onDone.invoke()
        }
    }
}