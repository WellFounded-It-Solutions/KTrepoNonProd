package se.infomaker.iap.articleview.follow.author

import com.google.gson.annotations.SerializedName
import se.infomaker.iap.articleview.follow.FollowPropertyObjectPreprocessorConfig

data class AuthorPreprocessorConfig(
        @SerializedName("authorsRawKey") private val _authorsRawKey: String?,
        val propertyKey: String,
        val template: String,
        @SerializedName("selectorType") private val _selectorType: String?,
        val articleProperty: String,
        val titleKey: String,
        val keyPath: String,
        val canOpenDirectly: Boolean?
) {

    val authorsRawKey: String
        get() = _authorsRawKey ?: "authorsRaw"

    val selectorType: String
        get() = _selectorType ?: "author"

    fun asFollowPropertyObjectPreprocessorConfig(): FollowPropertyObjectPreprocessorConfig {
        return FollowPropertyObjectPreprocessorConfig(selectorType, propertyKey, template, articleProperty, titleKey, keyPath, canOpenDirectly)
    }
}