package se.infomaker.iap.provisioning.backend

import java.util.Date
import java.util.concurrent.TimeUnit

data class ProductValidity(val name: String, val validTo: Date) {

    fun extend(duration: Long, timeUnit: TimeUnit): ProductValidity {
        return extend(timeUnit.toMillis(duration))
    }

    fun extend(extension: Long): ProductValidity {
        val now = Date().time
        return copy(name = name, validTo = Date(now + extension))
    }
}
