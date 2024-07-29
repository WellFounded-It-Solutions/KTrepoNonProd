package se.infomaker.iap.provisioning.credentials

data class KeychainResponse(val username: String?, val password: String?, val packageName: String?, var isUnableLogin: Boolean = false) {
    companion object {
        val SMART_LOCK_LABEL = "SMART_LOCK"
    }
}
