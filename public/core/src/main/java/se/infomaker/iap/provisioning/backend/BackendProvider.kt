package se.infomaker.iap.provisioning.backend

object BackendProvider {
    var backends = mutableMapOf<String, Backend>()

    fun get(appId: String, region: String, useLocalEmulators: Boolean) : Backend {
        backends[appId]?.let {
            return it
        }
        return create(appId, region, useLocalEmulators).also { backends[appId] = it }
    }

    private fun create(appId: String, region: String, useLocalEmulators: Boolean) : Backend {
        return Backend(appId, region, useLocalEmulators)
    }
}