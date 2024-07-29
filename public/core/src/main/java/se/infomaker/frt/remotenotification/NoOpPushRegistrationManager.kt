package se.infomaker.frt.remotenotification

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

object NoOpPushRegistrationManager : PushRegistrationManager {
    override val pushMeta: PushMeta? = null

    override fun registrationChanges(): Flow<Unit> = emptyFlow()

    override fun ensureRegistered() {
        // no-op
    }
}