package se.infomaker.iap.articleview.preprocessor.select.move

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonParseException
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.preprocessor.select.Selector

class Insert {
    companion object {
        fun registerInsertTypeAdapter(builder: GsonBuilder = GsonBuilder()): GsonBuilder {
            builder.registerTypeAdapter(InsertConfig::class.java, JsonDeserializer { json, _, context ->
                when (json.asJsonObject["method"].asString) {
                    "index" -> context.deserialize<InsertIndexConfig>(json, InsertIndexConfig::class.java)
                    "relative" -> context.deserialize<InsertRelativeConfig>(json, InsertRelativeConfig::class.java)
                    else -> throw JsonParseException("Could not deserialize InsertConfig to a specific class")
                }
            })
            return builder
        }

        fun MutableList<Item>.getInsertPosition(indexes: List<Int>, insertConfig: InsertConfig): Int {
            var toPosition = 0
            when (insertConfig) {
                is InsertIndexConfig -> toPosition = insertConfig.position
                is InsertRelativeConfig -> {
                    val subset = Selector.getIndexes(this, insertConfig.select)
                    if (subset.isEmpty()) {
                        insertConfig.fallback?.let { fallback ->
                            return getInsertPosition(indexes, fallback)
                        }
                        if (indexes.size == 1) {
                            return indexes[0] // Could not find an insertion point and there's no fallback defined, return item's original location.
                        }
                    }

                    toPosition = if (insertConfig.position == "after") {
                        (subset.lastOrNull() ?: -1) + 1
                    } else {
                        subset.firstOrNull() ?: -1
                    }
                    toPosition - indexes.filter { toPosition > it }.size
                }
                else -> throw ClassNotFoundException("I don't understand your InsertConfig class type")
            }
            return toPosition
        }
    }
}
