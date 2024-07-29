package se.infomaker.iap.articleview.preprocessor.divider

import com.google.gson.Gson
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.Preprocessor
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.author.DividerDecorator
import se.infomaker.iap.articleview.item.decorator.MarginDecorator
import se.infomaker.iap.articleview.preprocessor.select.Selector

class DividerPreprocessor : Preprocessor {
    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure {
        val dividerPreprocessorConfig = Gson().fromJson(config, DividerPreprocessorConfig::class.java)

        val indexes = Selector.getIndexes(content.body.items, dividerPreprocessorConfig.select)

        val itemGroups: List<List<Item>> =
                indexes.fold(mutableListOf<MutableList<Int>>(), { list, index ->
                    if (list.lastOrNull()?.lastOrNull() == index - 1) {
                        list.last().add(index)
                    } else {
                        list.add(mutableListOf(index))
                    }
                    return@fold list
                }).map {
                    it.map { content.body.items[it] }
                }

        itemGroups.forEach { indexGroup ->
            indexGroup.forEachIndexed { index, item ->
                var before = false
                var after = false
                val themeKey = dividerPreprocessorConfig.themeKey ?: "separator"

                //First item
                if (index == 0) {
                    //Add divider before first item
                    if (dividerPreprocessorConfig.getPlacement().contains(DividerPreprocessorConfig.Placement.before)) {
                        before = true
                        val separatorThemeKeyBase = listOf("${themeKey}Separator", "separator")
                        val marginKeys = separatorThemeKeyBase.suffixItems("Margin")
                        item.decorators.add(MarginDecorator(top = marginKeys))
                    }
                } else {
                    //Add dividers between all items
                    if (dividerPreprocessorConfig.getPlacement().contains(DividerPreprocessorConfig.Placement.between)) {
                        before = true
                    }
                }

                //Add divider to last item
                if (index == indexGroup.lastIndex) {
                    if (dividerPreprocessorConfig.getPlacement().contains(DividerPreprocessorConfig.Placement.after)) {
                        after = true
                        val separatorThemeKeyBase = listOf("${themeKey}Separator", "separator")
                        val marginKeys = separatorThemeKeyBase.suffixItems("Margin")
                        item.decorators.add(MarginDecorator(bottom = marginKeys))
                    }
                }

                item.decorators.add(DividerDecorator(
                        template = dividerPreprocessorConfig.template,
                        themeKey = themeKey,
                        before = before,
                        after = after))
            }
        }

        return content
    }

    private fun List<String?>.suffixItems(suffix: String): List<String> = this.filterNotNull().map { it + suffix }
}

/*
{
	"name": "divider",
	"config": {
		"themeKey": "authorSeparator", //default = separator
		"select": {
		},
		"template": "zebra", //optional
		"placement": "between|before" //between|around|(before|after), default = between
	}
}
 */