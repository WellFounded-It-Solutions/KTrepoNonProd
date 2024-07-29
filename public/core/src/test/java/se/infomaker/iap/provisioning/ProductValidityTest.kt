package se.infomaker.iap.provisioning

import org.junit.Assert
import org.junit.Test
import se.infomaker.iap.provisioning.backend.ProductValidity
import java.util.Date
import java.util.concurrent.TimeUnit

class ProductValidityTest {
    @Test
    fun testTimeExtension() {
        val expired = ProductValidity(name = "test", validTo = Date(0L))
        val valid = expired.extend(1, TimeUnit.HOURS)

        val now = Date()
        Assert.assertTrue(expired.validTo.before(now))
        Assert.assertTrue(valid.validTo.after(now))

        val almostAnHourFromNow = Date(now.time + TimeUnit.MINUTES.toMillis(59))
        Assert.assertTrue(expired.validTo.before(almostAnHourFromNow))
        Assert.assertTrue(valid.validTo.after(almostAnHourFromNow))

        val justOverAnHourFromNow = Date(now.time + TimeUnit.MINUTES.toMillis(61))
        Assert.assertTrue(expired.validTo.before(justOverAnHourFromNow))
        Assert.assertTrue(valid.validTo.before(justOverAnHourFromNow))
    }
}
