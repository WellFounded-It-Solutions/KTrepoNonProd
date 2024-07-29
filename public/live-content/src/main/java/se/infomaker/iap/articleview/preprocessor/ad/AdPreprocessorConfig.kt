package se.infomaker.iap.articleview.preprocessor.ad

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import se.infomaker.iap.articleview.preprocessor.select.SelectorConfig


class AdPreprocessorConfig {
    lateinit var providerConfiguration: List<JsonObject>
    lateinit var provider: String
    var insertStrategy: InsertionStrategy? = null
    val amount: Int? = null
}

data class InsertionStrategy(var strategy: Strategy = Strategy.END, var config: JsonObject?)

data class AdIntervalConfiguration(val select: SelectorConfig?, val alwaysInsert: Boolean, val interval: Int, val start: Int? = null, val max: Int? = null)

enum class Strategy {
    @SerializedName("end")
    END,
    @SerializedName("interval")
    INTERVAL;
}


