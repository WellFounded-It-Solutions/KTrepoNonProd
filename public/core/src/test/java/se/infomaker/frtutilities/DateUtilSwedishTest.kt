package se.infomaker.frtutilities

import android.os.Build
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.Date
import java.util.concurrent.TimeUnit

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class DateUtilSwedishTest(private val diff: Long, private val expected: String, private val now: Long) {

    companion object {
        const val JAN_2021 = 1609492500000L // Fri Jan 01 10:15:00 CET 2021
        const val JAN_2020 = 1577870100000L // Wed Jan 01 10:15:00 CET 2020
        val JAN_2020_NIGHT = (1577870100000L + TimeUnit.HOURS.toMillis(12))
        const val NOV_2018 = 1542621021397L // Mon Nov 19 10:50:21 CET 2018
        const val MARCH_2020 = 1583054100000L // Sun Mar 01 10:15:00 CET 2020
        const val MARCH_2020_LEAP_TEST = 1583831700000L //Tue Mar 10 10:15:00 CET 2020

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "Diff {0}, Expected {1}")
        fun data() = listOf(
                arrayOf(TimeUnit.SECONDS.toMillis(1), "Nu", JAN_2020),
                arrayOf(TimeUnit.MINUTES.toMillis(1), "En minut sedan", JAN_2020),
                arrayOf(TimeUnit.MINUTES.toMillis(5), "5 minuter sedan", JAN_2020),
                arrayOf(TimeUnit.MINUTES.toMillis(15), "15 minuter sedan", JAN_2020),
                arrayOf(TimeUnit.HOURS.toMillis(2), "Idag 08:15", JAN_2020),
                arrayOf(TimeUnit.HOURS.toMillis(2), "Idag 20:15", JAN_2020_NIGHT),
                arrayOf(TimeUnit.DAYS.toMillis(1), "Tisdag 10:15", JAN_2020),
                arrayOf(TimeUnit.DAYS.toMillis(6), "2019-12-26 10:15", JAN_2020),
                arrayOf(TimeUnit.DAYS.toMillis(7), "2019-12-25 10:15", JAN_2020),
                arrayOf(TimeUnit.DAYS.toMillis(1),  "Torsdag 10:15", JAN_2021),
                arrayOf(TimeUnit.DAYS.toMillis(7),  "2020-12-25 10:15", JAN_2021),
                arrayOf(TimeUnit.DAYS.toMillis(2),  "Lördag 10:50", NOV_2018),
                arrayOf(TimeUnit.DAYS.toMillis(6),  "Tisdag 10:50", NOV_2018),
                arrayOf(TimeUnit.DAYS.toMillis(7),  "2018-11-12 10:50", NOV_2018),
                arrayOf(TimeUnit.DAYS.toMillis(1),  "Lördag 10:15", MARCH_2020),
                arrayOf(TimeUnit.DAYS.toMillis(10),  "2020-02-29 10:15", MARCH_2020_LEAP_TEST)
        )
    }

    @Config(qualifiers = "sv")
    @Test
    fun testTimeAgoSince() {
        Assert.assertEquals(expected, DateUtil.timeAgoSince(RuntimeEnvironment.application, now, Date(now - diff)))
    }
}