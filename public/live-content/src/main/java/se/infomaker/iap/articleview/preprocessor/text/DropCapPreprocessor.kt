package se.infomaker.iap.articleview.preprocessor.text

import android.text.Spannable
import android.text.style.CharacterStyle
import androidx.core.text.getSpans
import com.google.gson.Gson
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.Preprocessor
import se.infomaker.iap.articleview.item.element.ElementItem
import se.infomaker.iap.articleview.preprocessor.select.Selector
import java.util.Locale

class DropCapPreprocessor : Preprocessor {

    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure {
        val dropCapConfig = Gson().fromJson(config, DropCapPreprocessorConfig::class.java)
        return process(content, dropCapConfig)
    }

    private fun process(content: ContentStructure, config: DropCapPreprocessorConfig): ContentStructure {
        val indices = Selector.getIndexes(content.body.items, config.select)
        indices.forEach { index ->
            (content.body.items[index] as? ElementItem)?.text?.let { spannableStringBuilder ->
                if (spannableStringBuilder.isNotEmpty()) {
                    val firstChar = spannableStringBuilder.first()
                    if (firstChar.isLetterOrDigit()) {
                        // Remove the first char, it will be rendered by the DropCapSpan
                        val dropCap = firstChar.toString().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                        val dropCapSpans = spannableStringBuilder.getSpans<CharacterStyle>(0, 0).toList()
                        spannableStringBuilder.delete(0, 1)
                        spannableStringBuilder.setSpan(DropCapSpan(dropCap, dropCapSpans, config.themeKey), 0, spannableStringBuilder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            }
        }
        return content
    }
}