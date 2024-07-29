package se.infomaker.streamviewer.editpage

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleObserver
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import se.infomaker.frtutilities.setImageResourceWithTint
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.view.ThemeableImageView
import se.infomaker.iap.theme.view.ThemeableTextView
import se.infomaker.storagemodule.Storage
import se.infomaker.storagemodule.model.Subscription
import se.infomaker.storagemodule.model.SubscriptionState
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.streamviewer.StatsHelper
import se.infomaker.streamviewer.SubscriptionAdapter
import se.infomaker.streamviewer.di.StreamNotificationSettingsHandlerFactory
import se.infomaker.streamviewer.extensions.getDrawableIdentifierOrFallback
import se.infomaker.streamviewer.stream.PushIconState
import se.infomaker.streamviewer.stream.StreamNotificationSettingsHandler


class EditPageAdapter(
    val activity: Activity,
    val recyclerView: RecyclerView,
    val moduleId: String,
    val theme: Theme,
    private val settingsHandlerFactory: StreamNotificationSettingsHandlerFactory
) : SubscriptionAdapter<EditPageViewHolder>(), LifecycleObserver {

    private val debug = false
    private val subscriptions = Storage.getSubscriptions()
    private val isRemoving = mutableSetOf<String?>()

    override fun updateSubscriptionOrdering(ordering: MutableList<Pair<String?, Int?>>) {
        Storage.reorderSubscriptions(ordering) {
            notifyDataSetChanged()
        }
    }

    private val itemTouchHelper = ItemTouchHelper(SubscriptionTouchCallback(activity, this, ResourceManager(activity, moduleId).getDrawableIdentifierOrFallback("action_delete", R.drawable.delete_subscription)) {
        delete(it)
    })

    init {
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun getManagedItem(position: Int): Subscription? = subscriptions[position]

    override fun onBindViewHolder(holder: EditPageViewHolder, position: Int) {
        subscriptions[position]?.let { subscription ->
            holder.subscription = subscription

            holder.title?.text = subscription.name
            holder.title?.setThemeKey("topic")

            val state = if (subscription.activatedOrPending) PushIconState.ENABLED else PushIconState.DISABLED
            val tintThemeKeys = if (subscription.activatedOrPending) listOf("notificationSelected","brandColor") else listOf("notificationUnselected","brandColor")

            holder.notification?.setThemeTintColor(tintThemeKeys)
            holder.notification?.setImageDrawable(getNotificationDrawable(holder.view.context, theme, state))

//            if (debug) {
//                holder.notification?.setImageResourceWithTint(StreamNotificationSettingsHandler.pushIcon(state), Color.BLACK)
//
//                subscription.state.let {
//                    when (it) {
//                        SubscriptionState.PENDING_ACTIVATION -> {
//                            holder.notification?.setImageResourceWithTint(StreamNotificationSettingsHandler.pushIcon(state), Color.RED)
//                        }
//                        SubscriptionState.PENDING_DEACTIVATION -> {
//                            holder.notification?.setImageResourceWithTint(StreamNotificationSettingsHandler.pushIcon(state), Color.RED)
//                        }
//                        else -> null
//                    }
//                }
//            }
            holder.notification?.setOnClickListener {


                val state = if (subscription.activatedOrPending)  PushIconState.DISABLED else PushIconState.ENABLED
                val tintThemeKeys = if (subscription.activatedOrPending)  listOf("notificationUnselected","brandColor") else  listOf("notificationSelected", "brandColor")

                holder.notification?.setThemeTintColor(tintThemeKeys)
                holder.notification?.setImageDrawable(getNotificationDrawable(holder.view.context, theme, state))


                if (isRemoving.contains(subscription.uuid)) {
                    return@setOnClickListener
                }


                Storage.getRealm().use {
                    it.executeTransaction {
                        subscription.pushActivated = subscription.pushActivated?.not()
                    }
                }


                settingsHandlerFactory.create(activity, moduleId, subscription)
                    .onSettingsChanged(recyclerView, null, EditPageActivity.STATISTICS_VIEW_NAME, null) {
                        activity.runOnUiThread {
                            notifyItemChanged(position)
                        }
                    }
            }
        }

        theme.apply(holder.view)

        holder.view.setOnLongClickListener {
            itemTouchHelper.startDrag(holder)
            true
        }
    }

    private fun delete(subscription: Subscription) {
        if (isRemoving.contains(subscription.uuid)) {
            return
        }

        val uuid = subscription.uuid
        isRemoving.add(uuid)
        val restoreSubscription = Storage.getPersistRealm().copyFromRealm(subscription)
        notifyItemRemoved(subscriptions.indexOf(subscription))
        Storage.delete(subscription)

        Snackbar.make(recyclerView, "Topic removed", Snackbar.LENGTH_LONG)
            .setAction(recyclerView.context.getString(R.string.undo)) {
                Storage.addOrUpdateSubscription(restoreSubscription) {
                    notifyItemRangeChanged(subscriptions.indexOf(it), 2)
                }
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(snackbar: Snackbar, @DismissEvent event: Int) {
                    if (event != DISMISS_EVENT_ACTION) {
                        settingsHandlerFactory.create(activity, moduleId, restoreSubscription).streamDeleted()
                        StatsHelper.logSubscriptionEvent(moduleId, StatsHelper.DELETE_STREAM_EVENT, EditPageActivity.STATISTICS_VIEW_NAME, restoreSubscription)
                    }
                    isRemoving.remove(uuid)
                }
            })
            .show()
    }

    private fun getNotificationDrawable(context: Context, theme: Theme, selected: PushIconState): Drawable? {
        val resource = if (selected == PushIconState.ENABLED) "notificationSelected" else "notificationUnselected"
        val image = theme.getImage(resource, null)
        if (image != null) {
            return image.getImage(context).mutate()
        }
        return ResourcesCompat.getDrawable(context.resources, StreamNotificationSettingsHandler.pushIcon(selected), null)?.mutate()
    }

    override fun getItemCount(): Int {
        return subscriptions.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditPageViewHolder = EditPageViewHolder(LayoutInflater.from(parent.context).inflate(viewType, parent, false))

    override fun getItemViewType(position: Int): Int = R.layout.edit_page_item
}

class EditPageViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    var subscription: Subscription? = null

    val title: ThemeableTextView? = view.findViewById(R.id.title)
    val notification: ThemeableImageView? = view.findViewById(R.id.notification)
}

private val Subscription.activatedOrPending: Boolean
    get() = state == SubscriptionState.ACTIVATED || state == SubscriptionState.PENDING_ACTIVATION