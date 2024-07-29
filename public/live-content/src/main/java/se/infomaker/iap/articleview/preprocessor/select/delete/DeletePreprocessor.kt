package se.infomaker.iap.articleview.preprocessor.select.delete

import com.google.gson.Gson
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.Preprocessor
import se.infomaker.iap.articleview.preprocessor.select.Selector

class DeletePreprocessor : Preprocessor {

    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure {
        val deletePreprocessorConfig = Gson().fromJson(config, DeletePreprocessorConfig::class.java)
        val indicesToDelete = Selector.getIndexes(content.body.items, deletePreprocessorConfig.select)
        content.body.items.removeAll(indicesToDelete.flatMap {
            content.body.items.filterIndexed { index, _ ->  index == it }
        })
        return content
    }
}