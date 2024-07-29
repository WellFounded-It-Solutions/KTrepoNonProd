package se.infomaker.livecontentui.livecontentdetailview.frequency

import android.content.Context
import android.util.Log
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import se.infomaker.datastore.DatabaseManager
import se.infomaker.datastore.FrequencyRecord
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.livecontentmanager.parser.PropertyObject
import se.infomaker.livecontentui.config.FeatureConfiguration
import se.infomaker.livecontentui.config.MeteredAccess
import timber.log.Timber
import java.util.Date

class FrequencyManager(context: Context) {

    private var config:FrequencyManagerConfiguration = ConfigManager.getInstance(context).getConfig("global", FrequencyManagerConfiguration::class.java)
    private var accessedContent = mutableListOf<Frequency>()
    private var readArticle: FeatureConfiguration
    private var dbManager: DatabaseManager
    private val lastReset = BehaviorRelay.createDefault(Date())

    init {
        val configuration = config.featureConfiguration
        readArticle = configuration?.get("readArticle") ?: FeatureConfiguration(emptyList())
        readArticle.permissions?.map { permission ->
            permission.freeLimit?.unit = permission.freeLimit?.unit ?: "day"
            permission.freeLimit?.interval = permission.freeLimit?.interval ?: 1
        }

        dbManager = DatabaseManager()
        dbManager.getFrequencies(object: DatabaseCallback {
            override fun onSuccess() { }

            override fun onDataNotAvailable() {
                Timber.e("Unable to load previously read articles")
            }

            override fun onFrequencyLoaded(frequencyRecord: List<FrequencyRecord>) {
                Timber.d("Loaded ${frequencyRecord.size} previously read articles")

                frequencyRecord.forEach {
                    accessedContent.add(Frequency(it.uuid, it.permission, it.created, it.property, getMeteredAccessFor(it.permission, it.property)))
                }
            }
        })
    }

    fun getMeteredAccessFor(articlePermission:String, articleProperty: String?):MeteredAccess?
    {
        var access: MeteredAccess? = null
        readArticle.permissions?.forEach { permission ->
            access = MeteredAccess(permission.freeLimit?.limit ?: 0,
                    permission.freeLimit?.unit, permission.freeLimit?.interval)
            if (permission.permission == articlePermission) {
                articleProperty?.let {
                    if (permission.property == it) {
                        return MeteredAccess(permission.freeLimit?.limit ?: 0,
                                permission.freeLimit?.unit, permission.freeLimit?.interval)
                    }
                }
            }
        }
        return access
    }

    fun canReadFrequencyArticle(articleObservable: Observable<PropertyObject>): Observable<Boolean> {
        removeExpiredArticles()

        val permissions = config.featureConfiguration?.get("readArticle")?.permissions
        return Observable.combineLatest(listOf(articleObservable, lastReset)) { objects ->
            val article = objects[0] as PropertyObject
            permissions?.forEach { permission ->
                val id = article.optString("contentId") ?: article.id
                if (article.isMetered(permission.property)) {
                    if (!canRead(id)) {
                        return@combineLatest true
                    }
                }
            }
            return@combineLatest false
        }
    }

    private fun canRead(id: String): Boolean {
        val validArticle = accessedContent.filter { article -> article.uuid == id }
        if(validArticle.isNotEmpty()) {
            return false
        }
        return true
    }

    private fun hasExpired(index: Int): Boolean {
        if (index < accessedContent.size) {
            val article = accessedContent[index]
            var expired:Boolean? = false
            article.meteredAccess?.let { meteredAccess ->
                val map = mutableMapOf<String, Long>()
                map["minute"] = 1000 * 60L
                map["hour"] = 1000 * 60 * 60L
                map["day"] = 24 * 1000 * 60 * 60L
                map["week"] = 7 * 24 * 1000 * 60 * 60L
                map["month"] = 30 * 24 * 1000 * 60 * 60L

                val interval = meteredAccess.interval ?: 1
                val unit = meteredAccess.unit ?: "day"
                expired = when (unit.toLowerCase()) {
                    "minute" -> {
                        map["minute"]?.let {
                            Date().time > (interval * it) + article.created
                        }
                    }
                    "hour" -> {
                        map["hour"]?.let {
                            Date().time > (interval * it) + article.created
                        }
                    }
                    "day" -> {
                        map["day"]?.let {
                            Date().time > (interval * it) + article.created
                        }
                    }
                    "week" -> {
                        map["week"]?.let {
                            Date().time > (interval * it) + article.created
                        }
                    }
                    "month" -> {
                        map["month"]?.let {
                            Date().time > (interval * it) + article.created
                        }
                    }
                    else -> true
                }
            }
            return expired ?: true
        }
        return true
    }

    private fun removeExpiredArticles() {

        accessedContent.forEachIndexed { index, article ->

            if (hasExpired(index)) {

                dbManager.deleteFrequency(object : DatabaseCallback {

                    override fun onSuccess() {
                        accessedContent.remove(article)
                        lastReset.accept(Date())
                    }

                    override fun onDataNotAvailable() {
                        Timber.e("Error saving read article")
                    }

                    override fun onFrequencyLoaded(frequencyRecord: List<FrequencyRecord>) {
                        Timber.d("onFrequencyLoaded")
                    }
                }, article.uuid)
            }
        }
    }

    private fun PropertyObject.isMetered(property: String?) : Boolean {
        if (property == null) {
            return false
        }
        return optString(property)?.toBoolean() ?: false
    }

    fun registerArticleRead(article:PropertyObject) {

        readArticle.permissions?.forEach { permission ->

            val meteredAccess:MeteredAccess? = getMeteredAccessFor(permission.permission, permission.property)
            permission.property?.let { property ->

                val exists = article.optString(property)?.toBoolean() ?: false
                if (!exists) {
                    return
                }
            }

            meteredAccess?.let {
                val previousArticle = accessedContent.filter { frequency -> article.id == frequency.uuid }
                if (accessedContent.size < it.limit && previousArticle.isEmpty()) {

                    dbManager.insertFrequency(object : DatabaseCallback {
                        override fun onSuccess() {
                            accessedContent.add(Frequency(article.id, permission.permission, Date().time, permission.property, it))
                            lastReset.accept(Date())
                        }

                        override fun onDataNotAvailable() {
                            Timber.e("Error saving read article")
                        }

                        override fun onFrequencyLoaded(frequencyRecord: List<FrequencyRecord>) {
                            Timber.d("onFrequencyLoaded")
                        }

                    }, article.id, permission.permission, permission.property)
                }
            }
        }
    }
}