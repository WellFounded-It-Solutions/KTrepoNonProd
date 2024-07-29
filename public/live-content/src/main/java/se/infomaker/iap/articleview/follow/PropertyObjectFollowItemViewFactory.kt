package se.infomaker.iap.articleview.follow

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleObserver
import com.google.android.material.snackbar.Snackbar
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.frtutilities.ktx.findActivity
import se.infomaker.frtutilities.ktx.requireActivity
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.ItemViewFactory
import se.infomaker.iap.articleview.util.UI.mapSubViews
import se.infomaker.iap.articleview.view.LifecycleProxy
import se.infomaker.iap.articleview.view.PropertyObjectItemViewFactory
import se.infomaker.iap.theme.Theme
import se.infomaker.livecontentmanager.parser.PropertyObject
import se.infomaker.storagemodule.Storage
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.streamviewer.StatsHelper
import se.infomaker.streamviewer.config.FollowConfig
import se.infomaker.streamviewer.di.StreamNotificationSettingsHandlerFactory
import timber.log.Timber
import java.util.UUID

class PropertyObjectFollowItemViewFactory @AssistedInject constructor(
    private val streamNotificationSettingsHandlerFactory: StreamNotificationSettingsHandlerFactory,
    private val configManager: ConfigManager,
    @Assisted private val template: String
) : ItemViewFactory {
    private val viewFactory = PropertyObjectItemViewFactory(template)

    override fun bindView(item: Item, view: View, moduleId: String) {
        (item as? FollowPropertyObjectItem)?.let { followItem ->
            viewFactory.bindView(view, moduleId, followItem.propertyObject)
            val follow = view.findViewById<FollowCompoundView>(R.id.follow)

            (item as? FollowPropertyObjectItem)?.let {
                val propertyObjectFollowable = item.propertyObject.id != PropertyObject.NO_UUID
                val allowFollow = propertyObjectFollowable && view.context.followFeatureEnabled()
                if (allowFollow) {
                    view.setOnClickListener(it.onClick)
                    follow.following = item.following

                    if (!item.following) {
                        addListener(follow, item, moduleId)
                    }
                }
                else {
                    follow.visibility = View.GONE
                }
            }

            (view as? LifecycleProxy)?.getLifecycle()?.let { lifecycle ->
                item.listeners.filterIsInstance(LifecycleObserver::class.java).forEach {
                    lifecycle.addObserver(it)
                }
            }
        }
    }

    private fun addListener(followView: FollowCompoundView, item: FollowPropertyObjectItem, moduleId: String) {
        followView.listener = {
            val topicUUID = UUID.randomUUID().toString()
            val values = mutableMapOf<String, String>()
            values["field"] = item.articleProperty
            values["value"] = item.value
            values["subscriptionName"] = item.title
            values["subscriptionId"] = topicUUID
            StatsHelper.logSubscriptionEvent(moduleId, StatsHelper.CREATE_STREAM_EVENT, StatsHelper.ARTICLE_VIEW, values as Map<String, Any>?)
            val config = configManager.getConfig(moduleId, FollowConfig::class.java)

            Storage.addOrUpdateSubscription(topicUUID, item.title, "match", values, config.enablePushOnSubscription) { subscription ->
                Timber.d("Created ${subscription.name}")

                val viewGroup = (followView.requireActivity().findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0) as ViewGroup
                val message = String.format(followView.context.getString(R.string.concept_added), subscription.name)
                val action = followView.context.getString(R.string.undo)
                Snackbar.make(viewGroup, message, Snackbar.LENGTH_LONG)
                        .setAction(action) {
                            if (subscription.isValid) {
                                Storage.delete(subscription)
                                StatsHelper.logSubscriptionEvent(moduleId, StatsHelper.DELETE_STREAM_EVENT, StatsHelper.ARTICLE_VIEW, values as Map<String, Any>?)
                            }
                        }
                        .addCallback(object : Snackbar.Callback() {
                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                if (event != DISMISS_EVENT_ACTION) {
                                    (followView.findActivity())?.let {
                                        val settingsHandler = streamNotificationSettingsHandlerFactory.create(it, moduleId, subscription)
                                        settingsHandler.initialActivate(followView.context)
                                    }
                                }
                            }
                        })
                        .show()
            }
        }
    }

    override fun createView(parent: ViewGroup, resourceManager: ResourceManager, theme: Theme): View {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.follow_group, parent, false)
        val contentFrame = view.findViewById<FrameLayout>(R.id.content_frame)
        contentFrame.addView(viewFactory.createView(contentFrame, resourceManager, theme))
        view.mapSubViews()
        return view
    }

    override fun themeView(view: View, item: Item, theme: Theme) {
        theme.apply(view)
        // TODO theme by view state?
    }

    override fun typeIdentifier(): Any = FollowPropertyObjectItem.createTemplateIdentifier(template)
}

private fun Context.followFeatureEnabled() =
    ConfigManager.getInstance(this).mainMenuConfig.mainMenuItems.any {
        it.moduleName == "Follow" || it.moduleName == "NearMe"
    }