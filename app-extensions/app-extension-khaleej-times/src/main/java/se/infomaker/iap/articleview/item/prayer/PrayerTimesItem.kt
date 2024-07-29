package se.infomaker.iap.articleview.item.prayer

import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.prayer.RelativeTimeObjectFactory.InTheFuture
import se.infomaker.iap.articleview.item.prayer.RelativeTimeObjectFactory.InThePast
import se.infomaker.iap.articleview.item.prayer.RelativeTimeObjectFactory.JustStarting
import se.infomaker.iap.articleview.preprocessor.prayer.Location
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.GregorianCalendar
import java.util.Locale

data class PrayerTimesItem(
    val id: String,
    val locations: List<Location>? = null,
    val prayerTimes: List<PrayerTime>,
    val themeKeys: MutableList<String> = mutableListOf("prayer", "default")
) : Item(id) {
    override val matchingQuery = mapOf<String, String>()
    override val selectorType = "prayer"
    override val typeIdentifier = PrayerTimesItem::class.java

    private val prayerTimeHelper = PrayerTimeHelper()

    val ummalquraDate: String
        get() = prayerTimeHelper.formattedUmmalquraDate(System.currentTimeMillis())

    val georgianDate: String
        get() = prayerTimeHelper.formattedGregorianDate(System.currentTimeMillis())
}

data class PrayerTime(val date: LocalDateTime, val name: String) {
    var expanded = false
    var twelveHourTime: String = DateTimeFormatter.ofPattern("h:mm").format(date)
    private val prayerType: PrayerType = getPrayerType()
    lateinit var time:RelativeTimeObjectFactory.RelativeTimeObject
    lateinit var timeUntilPrayer: String

    init {
        updateTimeUntilPrayer()
    }

    fun updateTimeUntilPrayer() {
        time = RelativeTimeObjectFactory.getTimeObject(date)
        val hoursString = time.hours.pluraliseIfRequired("hours")
        val minutesString = time.minutes.pluraliseIfRequired("minutes")

        when {
            (time is InThePast) -> {
                expanded = false
                timeUntilPrayer = "${formattedPrayerName()} was ${formatTimeString(hoursString, minutesString)} ago"
            }
            (time is InTheFuture) -> {
                expanded = false
                timeUntilPrayer = "${formatTimeString(hoursString, minutesString)} until ${formattedPrayerName()}"
            }
            (time is JustStarting) -> {
                expanded = false
                timeUntilPrayer = "${formattedPrayerName()} starts in less than a minute"
            }
            else -> {
                expanded = true
                timeUntilPrayer = "${formattedPrayerName()} just started"
            }
        }
    }

    private fun formatTimeString(hours: String?, minutes: String?): String {
        return when {
            (!hours.isNullOrEmpty() && !minutes.isNullOrEmpty()) -> "$hours and $minutes"
            (hours.isNullOrEmpty() && !minutes.isNullOrEmpty()) -> minutes
            (!hours.isNullOrEmpty() && minutes.isNullOrEmpty()) -> hours
            else -> ""
        }
    }

    private fun Int.pluraliseIfRequired(noun: String): String? {
        if (this <= 0) {
            return null
        }
        return if (this > 1) {
            "$this $noun"
        } else {
            "$this $noun".dropLast(1)
        }
    }

    fun dateWithOffsetInMinutes(minutes: Long): LocalDateTime {
        return date.plusMinutes(minutes)
    }

    fun formattedPrayerName(): String {
        return name.capitalize()
    }

    fun prayerSolarPhase(): String {
        return when (prayerType) {
            PrayerType.FAJR -> "sun_quarter"
            PrayerType.SHURUQ -> "sun_half"
            PrayerType.DHUHR -> "sun_full"
            PrayerType.ASR -> "sun_half"
            PrayerType.MAGHRIB -> "sun_quarter"
            PrayerType.ISHA -> "moon"
            else -> "full_sun"
        }
    }

    private fun getPrayerType(): PrayerType {
        return when (name.toUpperCase(Locale.getDefault())) {
            PrayerType.FAJR.name -> PrayerType.FAJR
            PrayerType.SHURUQ.name -> PrayerType.SHURUQ
            PrayerType.SHUROQ.name -> PrayerType.SHURUQ
            PrayerType.DHUHR.name -> PrayerType.DHUHR
            PrayerType.ASR.name -> PrayerType.ASR
            PrayerType.MAGHRIB.name -> PrayerType.MAGHRIB
            PrayerType.MAGRIB.name -> PrayerType.MAGHRIB
            PrayerType.ISHA.name -> PrayerType.ISHA
            else -> PrayerType.UNKNOWN
        }
    }
}

class PrayerTimeHelper {
    private val ummalquraCalendar = UmmalquraCalendar()
    private val gregorianCalendar = GregorianCalendar()
    private val ummalquraDateFormatter = SimpleDateFormat("MMMM d", Locale.getDefault()).apply {
        calendar = ummalquraCalendar
    }
    private val gregorianDateFormatter = SimpleDateFormat("MMMM d", Locale.getDefault()).apply {
        calendar = gregorianCalendar
    }

    fun formattedUmmalquraDate(timeInMillis: Long): String {
        ummalquraCalendar.timeInMillis = timeInMillis
        return ummalquraDateFormatter.format(ummalquraCalendar.time)
    }

    fun formattedGregorianDate(timeInMillis: Long): String {
        gregorianCalendar.timeInMillis = timeInMillis
        return gregorianDateFormatter.format(gregorianCalendar.time)
    }
}

enum class PrayerType {
    FAJR, DHUHR, ASR, MAGHRIB, MAGRIB, ISHA, SHURUQ, SHUROQ, UNKNOWN
}