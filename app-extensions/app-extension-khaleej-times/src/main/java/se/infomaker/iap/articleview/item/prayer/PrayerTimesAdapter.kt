package se.infomaker.iap.articleview.item.prayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.navigaglobal.mobile.extension.khaleejtimes.R
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.view.ThemeableConstraintLayout
import se.infomaker.iap.theme.view.ThemeableImageView
import se.infomaker.iap.theme.view.ThemeableLinearLayout
import se.infomaker.iap.theme.view.ThemeableTextView
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PrayerTimesAdapter(
    private val prayerTimes: List<PrayerTime>,
    private val theme: Theme?
) : RecyclerView.Adapter<PrayerTimesAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            LayoutInflater
                .from(viewGroup.context)
                .inflate(R.layout.prayer_time, viewGroup, false)
        )
    }

    private val internalPrayerTimes = mutableListOf<PrayerTime>()
    private val periodFormatter = DateTimeFormatter.ofPattern("a")

    init {
        internalPrayerTimes.addAll(prayerTimes)
    }

    private fun getPeriod(date: LocalDateTime): String = date.format(periodFormatter)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val prayerTime = internalPrayerTimes[position]

        bindView(holder, prayerTime)
        themeViewHolder(holder)

        /*holder.itemView.setOnClickListener {
            prayerTime.expanded = !prayerTime.expanded
            notifyItemChanged(position)
        }*/
    }

    private fun bindView(holder: ViewHolder, prayerTime: PrayerTime) {
        holder.name.text = prayerTime.formattedPrayerName()
        holder.time.text = prayerTime.twelveHourTime
        holder.period.text = getPeriod(prayerTime.date)
        holder.icon.themeKey = prayerTime.prayerSolarPhase()
        holder.subItem.visibility = if (prayerTime.expanded) View.VISIBLE else View.GONE
        holder.countDown.text = prayerTime.timeUntilPrayer
        if (prayerTime.expanded) { setSelected(holder) } else { setUnselected(holder) }
    }

    private fun setSelected(holder: ViewHolder) {
        holder.itemContainer.setThemeBackgroundColor("prayerTimeBackgroundSelected")
        holder.name.setThemeKey("prayerTimeNameSelected")
        holder.time.setThemeKey("prayerTimeClockSelected")
        holder.period.setThemeKey("prayerTimeAmPmSelected")
        holder.countDown.setThemeKey("prayerTimeUntilPrayerSelected")
    }

    private fun setUnselected(holder: ViewHolder) {
        holder.itemContainer.setThemeBackgroundColor("prayerTimeBackground")
        holder.name.setThemeKey("prayerTimeName")
        holder.time.setThemeKey("prayerTimeClock")
        holder.period.setThemeKey("prayerTimeAmPm")
        holder.countDown.setThemeKey("prayerTimeUntilPrayer")
    }

    private fun themeViewHolder(holder: ViewHolder) {
        theme?.apply {
            apply(holder.container)
            apply(holder.name)
            apply(holder.icon)
            apply(holder.time)
            apply(holder.period)
            apply(holder.subItem)
            apply(holder.countDown)
            apply(holder.itemContainer)
        }
    }

    fun updateTimeText() {
        internalPrayerTimes.forEachIndexed { index, prayerTime ->
            prayerTime.updateTimeUntilPrayer()
            notifyItemChanged(index)
        }
        internalPrayerTimes[getCurrentPrayerTime()].expanded = true
    }

    private fun getCurrentPrayerTime() =
        internalPrayerTimes.mapIndexed { index, value ->
            Pair(index, value.time.duration)
        }.firstOrNull {
            it.second > 0
        }?.first ?: 0

    fun applyTimeOffset(offset: Int) {
        internalPrayerTimes.clear()

        prayerTimes.map {
            internalPrayerTimes.add(it.copy(date = it.dateWithOffsetInMinutes(offset.toLong())))
        }
        internalPrayerTimes[getCurrentPrayerTime()].expanded = true
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = prayerTimes.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var container: ThemeableConstraintLayout = view.findViewById(R.id.prayer_time_container)
        var name: ThemeableTextView = view.findViewById(R.id.name)
        var time: ThemeableTextView = view.findViewById(R.id.time)
        var period: ThemeableTextView = view.findViewById(R.id.period)
        var icon: ThemeableImageView = view.findViewById(R.id.icon)
        val subItem: ThemeableLinearLayout = view.findViewById(R.id.sub_item)
        val countDown: ThemeableTextView = view.findViewById(R.id.countdown)
        val itemContainer: ThemeableLinearLayout = view.findViewById(R.id.item_container)
    }
}