package se.infomaker.iap.provisioning

import se.infomaker.iap.provisioning.backend.LoginType

/**
 * A component that can provide a [LoginType]. Can be useful when a snapshot of
 * a last login type that was synced is needed, as an initial value before the real
 * login type is fetched from the backend.
 */
interface LoginTypeProvider {

    /**
     * The current login type.
     */
    fun getLoginType(): LoginType
}
