package se.infomaker.iap.provisioning.credentials

import android.app.Activity
import android.content.IntentSender
import android.os.Bundle
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.CredentialRequest
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import timber.log.Timber
import android.content.Intent




class SmartLockManager(val activity: Activity) : GoogleApiClient.ConnectionCallbacks {

    companion object {
        private const val SMART_LOCK_READ_RESOLUTION_REQUEST = 30216
        private const val SMART_LOCK_SAVE_RESOLUTION_REQUEST = 30217
    }
    private val credentialsApiClient: GoogleApiClient? = GoogleApiClient.Builder(activity)
            .addConnectionCallbacks(this)
            .addApi(Auth.CREDENTIALS_API)
            .build()
    private var readFromSmartLockCompletionListener: ((KeychainResponse?) -> Unit)? = null
    private var writeToSmartLockCompletionListener: ((Boolean) -> Unit)? = null
    private var onConnect : Pair<String, (KeychainResponse?) -> Unit>? = null

    override fun onConnected(p0: Bundle?) {
        Timber.d("Connected")
        onConnect?.let {
            getSmartLockCredentials(it.first, it.second)
        }
        onConnect = null
    }

    override fun onConnectionSuspended(p0: Int) {
        Timber.d("Suspended")
    }

    fun getSmartLockCredentials(domain: String, onCompletion: (KeychainResponse?) -> Unit) {
        // Only allow one request at a time, if one already exists we cancel it.
        rejectReadFromSmartLockCompletionListener()

        val credentialRequest = CredentialRequest.Builder()
                .setPasswordLoginSupported(true)
                .setAccountTypes(domain)
                .build()
        if (credentialsApiClient == null || !credentialsApiClient.isConnected) {
            onConnect = Pair(domain, onCompletion)
            onCompletion(null)
            return
        }
        readFromSmartLockCompletionListener = onCompletion
        Auth.CredentialsApi.request(credentialsApiClient, credentialRequest).setResultCallback { credentialRequestResult ->
            val credential = credentialRequestResult.credential
            if (credentialRequestResult.status.isSuccess && credential != null) {
                // See "Handle successful credential requests"
                onCredentialRetrieved(credential)
            } else {
                // See "Handle unsuccessful and incomplete credential requests"
                resolveResult(credentialRequestResult.status)
            }
        }
    }

    fun setSmartLockCredentials(username: String?, password: String?, onCompletion:(Boolean) -> Unit) {
        // Only allow one request at a time, if one already exists we cancel it.
        rejectWriteToSmartLockCompletionListener()
        val credential = Credential.Builder(username).setPassword(password).build()
        if (username == null || password == null || credentialsApiClient?.isConnected == false) {
            onCompletion(false)
            return
        }
        this.writeToSmartLockCompletionListener = onCompletion
        Auth.CredentialsApi.save(credentialsApiClient, credential).setResultCallback { status ->
            if (status.isSuccess) {
                completeWrite(true)
            } else {
                if (status.hasResolution()) {
                    // Try to resolve the save request. This will prompt the user if
                    // the credential is new.
                    try {
                        status.startResolutionForResult(activity, SMART_LOCK_SAVE_RESOLUTION_REQUEST)
                    } catch (e: IntentSender.SendIntentException) {
                        // Could not resolve the request
                        completeWrite(false)
                    }

                } else {
                    // Request has no resolution
                    completeWrite(false)
                }
            }
        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == SMART_LOCK_READ_RESOLUTION_REQUEST) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                data.getParcelableExtra<Credential>(Credential.EXTRA_KEY)?.let {
                    onCredentialRetrieved(it)
                }
            } else {
                rejectReadFromSmartLockCompletionListener()
            }
            return true
        } else if (requestCode == SMART_LOCK_SAVE_RESOLUTION_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if (writeToSmartLockCompletionListener != null) {
                    writeToSmartLockCompletionListener?.invoke(true)
                    writeToSmartLockCompletionListener = null
                }
            } else {
                rejectWriteToSmartLockCompletionListener()
            }
            return true
        }
        return false
    }

    private fun rejectReadFromSmartLockCompletionListener() {
        readFromSmartLockCompletionListener?.invoke(null)
        readFromSmartLockCompletionListener = null
    }

    private fun rejectWriteToSmartLockCompletionListener() {
        if (writeToSmartLockCompletionListener != null) {
            writeToSmartLockCompletionListener?.invoke(false)
            writeToSmartLockCompletionListener = null
        }
    }

    private fun completeWrite(success: Boolean = false) {
        writeToSmartLockCompletionListener?.invoke(success)
        writeToSmartLockCompletionListener = null
    }

    private fun onCredentialRetrieved(credential: Credential) {
        val accountType = credential.accountType
        if (accountType == null) {
            val username = credential.id
            val password = credential.password
            var keychainResponse: KeychainResponse? = null
            if (password != null) {
                keychainResponse = KeychainResponse(username, password, KeychainResponse.SMART_LOCK_LABEL)
            }
            readFromSmartLockCompletionListener?.let {
                readFromSmartLockCompletionListener = null
                it.invoke(keychainResponse)
            }
        }
    }

    private fun resolveResult(status: Status) {
        if (status.statusCode == CommonStatusCodes.RESOLUTION_REQUIRED) {
            // Prompt the user to choose a saved credential; do not show the hint
            // selector.
            try {
                status.startResolutionForResult(activity, SMART_LOCK_READ_RESOLUTION_REQUEST)
            } catch (e: IntentSender.SendIntentException) {
                rejectReadFromSmartLockCompletionListener()
            }

        } else {
            // The user must create an account or sign in manually.
            rejectReadFromSmartLockCompletionListener()
        }
    }

    fun onStart() {
        credentialsApiClient?.connect()
    }

    fun onStop() {
        credentialsApiClient?.disconnect()
    }
}