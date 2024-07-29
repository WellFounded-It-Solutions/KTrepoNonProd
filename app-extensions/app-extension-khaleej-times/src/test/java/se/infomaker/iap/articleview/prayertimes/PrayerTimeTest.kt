package se.infomaker.iap.articleview.prayertimes

import android.os.Build
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import se.infomaker.iap.articleview.item.prayer.PrayerTime
import java.time.LocalDateTime

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.KITKAT])
class PrayerTimeTest {

    @Test
    fun testTimeUntilPrayer() {
        val now = PrayerTime(LocalDateTime.now(), "FAJAR")
        Assert.assertEquals("FAJAR just started", now.updateTimeUntilPrayer())

        val oneMinAgo = PrayerTime(LocalDateTime.now().plusSeconds(-30), "FAJAR")
        Assert.assertEquals("FAJAR just started", oneMinAgo.updateTimeUntilPrayer())

        val sixtyThreeMinsAgo = PrayerTime(LocalDateTime.now().plusMinutes(-63), "FAJAR")
        Assert.assertEquals("FAJAR was 1 hour and 3 minutes ago", sixtyThreeMinsAgo.updateTimeUntilPrayer())

        val oneHourAgo = PrayerTime(LocalDateTime.now().plusMinutes(-60), "FAJAR")
        Assert.assertEquals("FAJAR was 1 hour ago", oneHourAgo.updateTimeUntilPrayer())

        val oneMinToGo = PrayerTime(LocalDateTime.now().plusSeconds(30), "FAJAR")
        Assert.assertEquals("FAJAR starts in less than a minute", oneMinToGo.updateTimeUntilPrayer())

        val sixtyThreeMinsToGo = PrayerTime(LocalDateTime.now().plusMinutes(63), "FAJAR")
        Assert.assertEquals("1 hour and 3 minutes until FAJAR", sixtyThreeMinsToGo.updateTimeUntilPrayer())

        val oneHourToGo = PrayerTime(LocalDateTime.now().plusMinutes(60), "FAJAR")
        Assert.assertEquals("1 hour until FAJAR", oneHourToGo.updateTimeUntilPrayer())
    }
}