package se.infomaker.livecontentmanager.query

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Creates a filter that creates a search question that subtracts a specified amount of time
 * @param amount Amount of time to subtract in filter
 * @param unit What time unit to subtract
 */
class TimeFilter(private val amount: Int, private val unit: TimeUnit, private val pubdateKey: String) : QueryFilter {
    /**
     * Creates a filter with a search question that subtracts a specified amount of days
     * @param amount How many days you want to subtract
     */
    constructor(amount: Int, pubdateKey: String) : this(amount, TimeUnit.DAYS, pubdateKey)

    /**
     * The time to subtract from
     */
    var time = Date()

    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SS'Z'", Locale.ENGLISH)

        init {
            dateFormat.timeZone = TimeZone.getTimeZone("Zulu")
        }
    }

    override fun identifier(): String {
        return "amount:$amount:unit:$unit"
    }

    override fun createStreamFilter(): JSONObject {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun calculateTime(): Calendar {
        val cal: Calendar = Calendar.getInstance()
        cal.time = time
        cal.add(unit.calendarUnit, -amount)
        return cal
    }

    override fun createSearchQuery(baseQuery: String): String {
        val lastActive = dateFormat.format(calculateTime().time)
        return "$pubdateKey:[$lastActive TO NOW] AND ($baseQuery)"
    }

    override fun toString(): String {
        return "Amount: $amount - Unit: $unit - Current time: $time - Calculated time: ${calculateTime().time}"
    }

    /**
     * The time unit to subtract, maps to java.util.Calendar time units
     */
    enum class TimeUnit(val calendarUnit: Int) {
        DAYS(Calendar.DAY_OF_MONTH),
        HOURS(Calendar.HOUR_OF_DAY),
        MINUTES(Calendar.MINUTE),
        SECONDS(Calendar.SECOND),
        MILLISECOND(Calendar.MILLISECOND)
    }
}