package se.infomaker.livecontentmanager.query

import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TimeFilterTest {
    lateinit var filter: TimeFilter

    companion object {
        var TIME_TO_CHECK: Date
        init {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            TIME_TO_CHECK = dateFormat.parse("2016-10-04")
        }
    }

    @Before
    fun setUp() {
        filter = TimeFilter(30, "Pubdate")
        filter.time = TIME_TO_CHECK
    }

    @Test
    fun identifier() {
        Assert.assertEquals("amount:30:unit:DAYS", filter.identifier())
    }

    @Test
    fun createSearchQuery() {
        val cal: Calendar = Calendar.getInstance()
        cal.time = TIME_TO_CHECK
        cal.add(Calendar.DAY_OF_MONTH, -30)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SS'Z'", Locale.ENGLISH)
        dateFormat.timeZone = TimeZone.getTimeZone("Zulu")
        val lastActive = dateFormat.format(cal.time)

        Assert.assertEquals("Pubdate:[$lastActive TO NOW] AND (BQ)", filter.createSearchQuery("BQ"))
    }
}