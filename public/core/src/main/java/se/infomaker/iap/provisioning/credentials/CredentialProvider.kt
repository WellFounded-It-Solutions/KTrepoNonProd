package se.infomaker.iap.provisioning.credentials

import android.content.Context

interface CredentialProvider {
    /**
     * Returns a list of available credentials,
     */
    fun availableCredentials(context: Context, onResult: (List<Credential>) -> Unit)
}
