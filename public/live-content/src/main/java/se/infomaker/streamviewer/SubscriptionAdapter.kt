package se.infomaker.streamviewer

import androidx.recyclerview.widget.RecyclerView
import se.infomaker.storagemodule.model.Subscription

abstract class SubscriptionAdapter<T : RecyclerView.ViewHolder?> : RecyclerView.Adapter<T>() {
    abstract fun getManagedItem(position: Int): Subscription?
    abstract fun updateSubscriptionOrdering(ordering: MutableList<Pair<String?, Int?>>)
}