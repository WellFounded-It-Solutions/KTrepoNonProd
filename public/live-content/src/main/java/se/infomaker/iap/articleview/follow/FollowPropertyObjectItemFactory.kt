package se.infomaker.iap.articleview.follow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import org.json.JSONObject
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.livecontentmanager.parser.PropertyObject
import se.infomaker.livecontentmanager.query.FilterHelper
import se.infomaker.livecontentmanager.query.MatchFilter
import se.infomaker.livecontentmanager.query.QueryFilter
import se.infomaker.storagemodule.Storage
import se.infomaker.streamviewer.FollowRecyclerViewActivity
import se.infomaker.streamviewer.action.SubscriptionDescription
import se.infomaker.streamviewer.stream.SubscriptionUtil
import java.util.ArrayList
import java.util.UUID

object FollowPropertyObjectItemFactory {

    fun create(properties: JSONObject, config: FollowPropertyObjectPreprocessorConfig): FollowPropertyObjectItem {
        val id = properties.optJSONArray("contentId")?.optString(0, null)
            ?: properties.optString("contentId", "generated" + UUID.randomUUID().toString())
        val propertyObject = PropertyObject(properties, id)
        val title = propertyObject.optString(config.titleKey) ?: "Unknown"
        val value = propertyObject.optString(config.keyPath) ?: ""
        val onClick = if (config.canOpenDirectly == true) View.OnClickListener { openMatchSubscription(it.context, title, config.articleProperty, value)  } else null

        return FollowPropertyObjectItem(propertyObject, config.template, config.selectorType ?: "followPropertyObject", config.articleProperty, value, title).also {
            it.onClick = onClick
        }
    }

    private fun openMatchSubscription(context: Context, title: String, field: String, value: String) {
        val moduleId = resolveModuleId()

        val intent = Intent(context, FollowRecyclerViewActivity::class.java)
        val bundle = Bundle()
        bundle.putString("moduleId", moduleId)
        if (SubscriptionUtil.hasMatchSubscription(field, value)) {
            Storage.getMatchSubscription(value, field)?.let {
                bundle.putString("subscriptionUUID", it.uuid)
            }

        } else {
            val description = SubscriptionDescription.Builder()
                    .setName(title)
                    .setType("match")
                    .putParameter("name", title)
                    .putParameter("field", field)
                    .putParameter("moduleId", moduleId)
                    .putParameter("value", value).create()
            bundle.putSerializable(FollowRecyclerViewActivity.SUBSCRIPTION_DESCRIPTION, description)
        }

        bundle.putString("title", title)

        val filters = ArrayList<QueryFilter>()
        filters.add(MatchFilter(field, value))
        FilterHelper.put(intent, filters)
        intent.putExtras(bundle)
        context.startActivity(intent)
    }

    private fun resolveModuleId(): String {
        val followItems = ConfigManager.getInstance().mainMenuConfig.mainMenuItems.filter { it.moduleName == "NearMe" || it.moduleName == "Follow" }.toList()
        return if (followItems.isNotEmpty()) {
            followItems.first().id
        } else {
            "shared"
        }
    }
}