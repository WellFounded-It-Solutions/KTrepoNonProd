package se.infomaker.iap.articleview.follow.author

import android.util.Xml
import com.google.gson.Gson
import se.infomaker.frtutilities.JSONUtil
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.Preprocessor
import se.infomaker.iap.articleview.follow.FollowPropertyObjectItemFactory
import se.infomaker.iap.articleview.follow.FollowPropertyUpdateListener
import se.infomaker.iap.articleview.follow.extensions.find
import se.infomaker.iap.articleview.follow.extensions.forEach
import se.infomaker.iap.articleview.follow.extensions.mapStringNotNull
import se.infomaker.livecontentmanager.parser.PropertyObject

class AuthorPreprocessor : Preprocessor {

    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure {
        val authorPreprocessorConfig = Gson().fromJson(config, AuthorPreprocessorConfig::class.java)
        val parser = Xml.newPullParser()
        val hierarchicalObjects = JSONUtil.optJSONArray(content.properties, authorPreprocessorConfig.propertyKey)
        JSONUtil.optJSONArray(content.properties, authorPreprocessorConfig.authorsRawKey)?.let { authorsRaw ->
            authorsRaw.mapStringNotNull { AuthorData.fromXml(it, parser) }.forEach { authorData ->
                val item = if (authorData.uuid == null || authorData.uuid == PropertyObject.NO_UUID) {
                    authorData.toItem(authorPreprocessorConfig)
                }
                else {
                    hierarchicalObjects?.find { it.optJSONArray("contentId")?.get(0) == authorData.uuid }?.let {
                        FollowPropertyObjectItemFactory.create(it, authorPreprocessorConfig.asFollowPropertyObjectPreprocessorConfig()).also { item ->
                            item.listeners.add(FollowPropertyUpdateListener(item))
                        }
                    }
                }
                item?.let {
                    content.body.items.add(it)
                }
            }
        } ?: run {
            hierarchicalObjects?.forEach {
                val item = FollowPropertyObjectItemFactory.create(it, authorPreprocessorConfig.asFollowPropertyObjectPreprocessorConfig())
                item.listeners.add(FollowPropertyUpdateListener(item))
                content.body.items.add(item)
            }
        }
        return content
    }
}