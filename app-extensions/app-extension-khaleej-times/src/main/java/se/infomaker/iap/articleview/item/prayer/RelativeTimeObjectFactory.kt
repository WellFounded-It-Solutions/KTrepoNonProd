package se.infomaker.iap.articleview.item.prayer

import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

class RelativeTimeObjectFactory {

    companion object {

        private fun getDuration(from: LocalDateTime): Duration {
            val prayerTime = LocalTime.of(from.hour, from.minute, from.second)
            val now = LocalTime.now()
            return Duration.between(now, prayerTime)
        }

        private fun calculateReadable(duration: Long): List<Int> {
            val hours = duration / SECONDS_PER_HOUR
            val minutes = duration % SECONDS_PER_HOUR / SECONDS_PER_MINUTE
            val secs = duration % SECONDS_PER_MINUTE
            return listOf(hours.toInt(), minutes.toInt(), secs.toInt())
        }

        fun getTimeObject(from: LocalDateTime): RelativeTimeObject {
            val duration = getDuration(from)

            val readableTime = calculateReadable(duration.abs().seconds)
            val hours = readableTime[0]
            val minutes = readableTime[1]
            val secs = readableTime[2]

            return when {
                hours == 0 && minutes == 0 && (duration.isNegative && secs < 59 || duration.isZero) -> JustStarted(
                    hours,
                    minutes,
                    secs,
                    duration.seconds
                )
                hours == 0 && minutes == 0 && (!duration.isNegative && secs <= 59) -> JustStarting(
                    hours,
                    minutes,
                    secs,
                    duration.seconds
                )
                (hours != 0 || minutes != 0) && duration.isNegative -> {

                    if (secs == 0) {
                        return InThePast(hours, minutes, secs, duration.seconds)
                    }

                    val localDuration = duration.minusSeconds(secs.toLong())
                    if (secs > 30) {
                        val updatedDuration = localDuration.plusMinutes(1)
                        val updated = calculateReadable(updatedDuration.abs().seconds)
                        return InThePast(
                            updated[0],
                            updated[1],
                            updated[2],
                            updatedDuration.seconds
                        )
                    }
                    return InThePast(hours, minutes, secs, duration.seconds)
                }
                (hours != 0 || minutes != 0) && !duration.isNegative -> {
                    if (secs == 0) {
                        return InTheFuture(hours, minutes, secs, duration.seconds)
                    }
                    val localDuration = duration.minusSeconds(secs.toLong())
                    if (secs > 30) {
                        val updatedDuration = localDuration.plusMinutes(1)
                        val updated = calculateReadable(updatedDuration.abs().seconds)
                        return InTheFuture(
                            updated[0],
                            updated[1],
                            updated[2],
                            updatedDuration.seconds
                        )
                    }
                    InTheFuture(hours, minutes, secs, duration.seconds)
                }
                else -> InTheFuture(hours, minutes, secs, duration.seconds)
            }
        }

        private const val MINUTES_PER_HOUR = 60
        private const val SECONDS_PER_MINUTE = 60
        private const val SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR
    }

    interface RelativeTimeObject {
        val hours: Int
        val minutes: Int
        val secs: Int
        val duration: Long
    }

    data class InThePast(
        override val hours: Int,
        override val minutes: Int,
        override val secs: Int, override val duration: Long
    ) : RelativeTimeObject

    data class InTheFuture(
        override val hours: Int,
        override val minutes: Int,
        override val secs: Int, override val duration: Long
    ) : RelativeTimeObject

    data class JustStarting(
        override val hours: Int,
        override val minutes: Int,
        override val secs: Int, override val duration: Long
    ) : RelativeTimeObject

    data class JustStarted(
        override val hours: Int,
        override val minutes: Int,
        override val secs: Int, override val duration: Long
    ) : RelativeTimeObject
}