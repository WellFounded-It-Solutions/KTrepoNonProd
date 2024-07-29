package se.infomaker.streamviewer.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.content.res.AppCompatResources
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.view.ThemeableCardView
import se.infomaker.iap.theme.view.ThemeableImageView
import se.infomaker.storagemodule.Storage
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.streamviewer.StatsHelper
import se.infomaker.streamviewer.config.FollowConfig
import timber.log.Timber
import java.util.UUID

class ListHeaderFollowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ThemeableCardView(context, attrs, defStyleAttr) {

    private var garbage: Disposable? = null
    private var theme: Theme? = null
    private var followIconImageView: ThemeableImageView? = null

    var moduleId: String? = null

    private var isFollowing = false
        set(value) {
            field = value
            setIconDrawable()
        }

    var concept: ConceptRecord? = null
        set(value) {
            field = value
            if (isAttachedToWindow) {
                subscribeToConcept()
            }
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.list_header_follow_view, this)
    }

    private fun setIconDrawable() {
        theme?.let {
            val themeKey = if (isFollowing) "topicSelected" else "topicUnselected"
            val image = it.getImage(themeKey, null)
            val drawable = if (image != null) {
                image.getImage(context).mutate()
            }
            else {
                val defaultResource = if (isFollowing) R.drawable.heart else R.drawable.heart_outline
                AppCompatResources.getDrawable(context, defaultResource)?.mutate()
            }
            followIconImageView?.setImageDrawable(drawable)
            followIconImageView?.setThemeTintColor(themeKey)
            it.apply(followIconImageView)
        }
    }

    override fun apply(theme: Theme) {
        super.apply(theme)
        this.theme = theme
        setIconDrawable()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setOnClickListener {
            concept?.let { record ->
                toggleFollowing(moduleId, record)
            }
        }
        followIconImageView = findViewById(R.id.follow_icon)
        setIconDrawable()
    }

    private fun toggleFollowing(moduleId: String?, concept: ConceptRecord) {
        val subscription = Storage.getMatchSubscription(concept.uuid, concept.articleProperty)
        if (subscription != null) {
            StatsHelper.logSubscriptionEvent(moduleId, StatsHelper.DELETE_STREAM_EVENT, null, subscription)
            Storage.delete(subscription)
        }
        else {
            concept.save(moduleId)
        }
    }

    private fun clearGarbage() {
        garbage?.dispose()
        garbage = null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        subscribeToConcept()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        clearGarbage()
    }

    private fun subscribeToConcept() {
        clearGarbage()
        concept?.let { record ->
            garbage = Storage.getSubscriptions().asChangesetObservable()
                .map {
                    it.collection.filterNotNull().mapNotNull { subscription ->
                        subscription.getValue("value")
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    isFollowing = it.contains(record.uuid)
                }
        }
    }
}

data class ConceptRecord(val uuid: String, val articleProperty: String, val title: String) {

    fun save(moduleId: String?) {
        val config = ConfigManager.getInstance().getConfig(moduleId, FollowConfig::class.java)
        val orderIndex = Storage.nextOrderIndex()
        val values = mutableMapOf<String, String>()
        val topicUUID = UUID.randomUUID().toString()
        values["field"] = articleProperty
        values["value"] = uuid
        values["subscriptionName"] = title
        values["subscriptionId"] = topicUUID
        StatsHelper.logSubscriptionEvent(moduleId, StatsHelper.CREATE_STREAM_EVENT, null, values as Map<String, Any>?)
        Storage.addOrUpdateSubscription(topicUUID, title, "match", values, orderIndex, config?.enablePushOnSubscription ?: true) { subscription ->
            Timber.d("Created ${subscription.name}")
        }
    }
}