package se.infomaker.iap.articleview.preprocessor.select.apply

import com.google.gson.Gson
import org.json.JSONObject
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.ContentViewModel
import se.infomaker.iap.articleview.Preprocessor
import se.infomaker.iap.articleview.preprocessor.PreprocessorManager
import se.infomaker.iap.articleview.preprocessor.select.Selector

class ApplyPreprocessor : Preprocessor {
    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure {
        val config = Gson().fromJson(config, ApplyConfig::class.java)

        val indexes = Selector.getIndexes(content.body.items, config.select)
        val items = indexes.map { content.body.items[it] }.toMutableList()
        val contentStructure = ContentStructure(body = ContentViewModel(items), properties = JSONObject(content.properties.toString()))

        val processedItems = PreprocessorManager.preprocess(contentStructure, config.preprocessors, resourceProvider).body.items

        //Items added to the list
        if (processedItems.size > indexes.size) {
            (indexes.size until processedItems.size).map { processedItems[it] }.forEach {
                content.body.items.add(it)
                processedItems.remove(it)
            }
        } else if (processedItems.size < indexes.size) { //Items removed from the list
            var counter = 0
            (processedItems.size until indexes.size).forEach {
                content.body.items.removeAt(indexes[it] + counter--)
            }
        }

        processedItems.forEachIndexed { index, item ->
            content.body.items.removeAt(indexes[index])
            content.body.items.add(indexes[index], item)
        }

        return content
    }
}