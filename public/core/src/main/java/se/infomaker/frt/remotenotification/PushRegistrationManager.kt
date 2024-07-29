package se.infomaker.frt.remotenotification

import kotlinx.coroutines.flow.Flow

interface PushRegistrationManager {

    val pushMeta: PushMeta?

    fun registrationChanges(): Flow<Unit>

    fun ensureRegistered()
}