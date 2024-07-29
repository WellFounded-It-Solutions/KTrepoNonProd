package se.infomaker.iap.articleview.item.prayer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.Spinner
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.view.Themeable
import se.infomaker.iap.theme.view.ThemeableRecyclerView
import se.infomaker.iap.theme.view.ThemeableTextView
import java.util.concurrent.TimeUnit
import androidx.core.view.setPadding
import com.google.android.material.textview.MaterialTextView
import com.navigaglobal.mobile.extension.khaleejtimes.R


class PrayerTimesView(context: Context) : FrameLayout(context), Themeable {

    private val recyclerView: ThemeableRecyclerView
    private val todayGeorgian: ThemeableTextView
    private val todayUmmalqura: ThemeableTextView
    private var prayerTimesAdapter: PrayerTimesAdapter? = null
    private val compositeDisposable = CompositeDisposable()
    private val spinner: Spinner
    private val sharedPreferences = context.getSharedPreferences(PRAYER_TIME_PREFS, Context.MODE_PRIVATE)
    var theme: Theme? = null

    companion object{
        const val PRAYER_TIME_PREFS = "prayer_times_prefs"
        const val SELECTED_OFFSET = "selected_offset"

    }

    init {
        LayoutInflater.from(context).inflate(R.layout.prayer_times, this)
        todayGeorgian = findViewById(R.id.todayGregorian)
        todayUmmalqura = findViewById(R.id.todayUmmalqura)
        spinner = findViewById(R.id.location_menu)
        recyclerView = findViewById(R.id.prayer_times)
        recyclerView.layoutManager = LinearLayoutManager(context)
        (recyclerView.itemAnimator as SimpleItemAnimator?)?.supportsChangeAnimations = false
    }

    fun bind(item: PrayerTimesItem) {
        todayGeorgian.text = item.georgianDate
        todayUmmalqura.text = item.ummalquraDate
        prayerTimesAdapter = PrayerTimesAdapter(item.prayerTimes, theme)

        item.locations?.let { locations ->
            locations
                .toMutableList()
                .map { location -> location.name }
                .also {
                    spinner.adapter = ArrayAdapter(
                        spinner.context,
                        R.layout.location_drop_down_menu,
                        it
                    )

                    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            sharedPreferences.edit {
                                putInt(SELECTED_OFFSET, position)
                            }
                            prayerTimesAdapter?.applyTimeOffset(locations[position].offsetInMinutes)
                            (view as? MaterialTextView)?.setPadding(0)
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
                }
            sharedPreferences.getInt(SELECTED_OFFSET, 0).let {
                prayerTimesAdapter?.applyTimeOffset(item.locations[it].offsetInMinutes)
                spinner.setSelection(it)
            }
        }
        recyclerView.adapter = prayerTimesAdapter
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        compositeDisposable.add(
            Observable
                .interval(5, 5, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe {
                    prayerTimesAdapter?.updateTimeText()
                }
        )
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        compositeDisposable.clear()
    }

    override fun apply(theme: Theme) {
        theme.apply(recyclerView)
        theme.apply(todayGeorgian)
        theme.apply(todayUmmalqura)
    }
}