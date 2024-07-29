package se.infomaker.streamviewer.tabs

import android.os.Bundle
import android.util.SparseArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import se.infomaker.frt.statistics.StatisticsEvent
import se.infomaker.frt.statistics.StatisticsManager
import se.infomaker.livecontentui.livecontentrecyclerview.fragment.LiveContentRecyclerViewFragment
import se.infomaker.storagemodule.Storage
import se.infomaker.streamviewer.FollowRecyclerViewActivity
import se.infomaker.streamviewer.config.FollowConfig
import se.infomaker.streamviewer.stream.SubscriptionUtil
import timber.log.Timber
import java.lang.ref.WeakReference

class TabsPagerAdapter(fragmentManager: FragmentManager, val moduleId: String, val config: FollowConfig, onChange: () -> Unit) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private var subscriptions = Storage.getSubscriptions()
    private var fragments: SparseArray<WeakReference<Fragment>> = SparseArray()
    private var pending: Int? = null

    init {
        subscriptions.addChangeListener { _ ->
            try {
                notifyDataSetChanged()
                onChange.invoke()
            } catch (e: Exception){
                Timber.e(e)
            }
        }
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun getItem(position: Int): Fragment {
        val fragment = LiveContentRecyclerViewFragment()
        fragments.put(position, WeakReference(fragment))
        subscriptions[position]?.let { subscription ->
            val bundle = Bundle()
            bundle.putSerializable("moduleId", moduleId)
            bundle.putBoolean(LiveContentRecyclerViewFragment.MANUAL_STATISTICS, true)
            if (pending == position) {
                bundle.putBoolean(LiveContentRecyclerViewFragment.REGISTER_INITAL_STATISTICS, true)
                pending = null
            }
            bundle.putString("title", subscription.name)
            bundle.putString("subscriptionUUID", subscription.uuid)

            Timber.e("TabsPagerAdapter: ModuleId: %s title: %s subscriptionUUID: %s", moduleId, subscription.name, subscription.uuid)

            val statsExtras = Bundle()
            SubscriptionUtil.putStatistics(subscription, statsExtras)
            bundle.putBundle(FollowRecyclerViewActivity.STATS_EXTRAS_KEY, statsExtras)
            val filter = SubscriptionUtil.createFilter(subscription, config)
            bundle.putSerializable("queryFilters", ArrayList(listOf(filter)))
            fragment.arguments = bundle
        }

        return fragment
    }

    override fun getCount(): Int {
        return subscriptions.size
    }

    override fun getPageTitle(position: Int): String? {
        return subscriptions[position]?.name
    }

    fun getSubscriptionIdAtPosition(position: Int): String? = subscriptions[position]?.uuid

    fun fragmentAtIndex(position: Int): Fragment? {
        return fragments[position]?.get()
    }

    fun getSubscriptionIndex(uuid: String) = subscriptions.indexOfFirstOrNull { subscription -> subscription.uuid == uuid }

    fun registerStatsEvent(position: Int) {
        val fragment = fragmentAtIndex(position) as? LiveContentRecyclerViewFragment
        if (fragment != null) {
            fragment.registerStatsEvent()
        } else {
            pending = position
        }

        Timber.e("TabsPagerAdapter: ModuleId: %s ", moduleId)

        if (count == 0) {
            StatisticsManager.getInstance().logEvent(StatisticsEvent.Builder()
                    .viewShow()
                    .moduleId(moduleId)
                    .viewName("empty").build())
        }
    }
}

private inline fun <T> List<T>.indexOfFirstOrNull(predicate: (T) -> Boolean): Int? {
    var index = 0
    for (item in this) {
        if (predicate(item))
            return index
        index++
    }
    return null
}