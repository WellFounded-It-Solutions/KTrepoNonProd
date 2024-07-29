package se.infomaker.livecontentui.section

import org.json.JSONObject
import se.infomaker.livecontentmanager.config.PresentationBehaviour
import se.infomaker.livecontentmanager.parser.PropertyObject
import se.infomaker.livecontentui.extensions.copy
import se.infomaker.livecontentui.extensions.optRelatedArticles
import se.infomaker.livecontentui.extensions.optSortedListObjects
import se.infomaker.livecontentui.extensions.patch
import se.infomaker.livecontentui.extensions.safePut
import se.infomaker.livecontentui.section.configuration.Orientation
import se.infomaker.livecontentui.section.configuration.SectionConfig
import se.infomaker.livecontentui.section.datasource.newspackage.ArticleSectionItem
import se.infomaker.livecontentui.section.datasource.newspackage.ErrorSectionItem
import se.infomaker.livecontentui.section.datasource.newspackage.HorizontalListSectionItem
import se.infomaker.livecontentui.section.datasource.newspackage.PackageSectionItem
import se.infomaker.livecontentui.section.supplementary.ExpandableSectionItem
import se.infomaker.livecontentui.section.supplementary.SupplementarySectionItemFactory
import se.infomaker.livecontentui.section.supplementary.SupplementarySectionItems

class SectionItemFactory(
    private val layoutResolver: LayoutResolver,
    private val behaviorResolver: BehaviorResolver,
    private val config: SectionConfig,
    private val supplementarySectionItemFactory: SupplementarySectionItemFactory
) {

    @JvmOverloads
    fun listFromPropertyObject(propertyObject: PropertyObject, sectionIdentifier: String, groupKey: String, context: JSONObject?, subListBehaviour: PresentationBehaviour? = null, orientation: Orientation = Orientation.VERTICAL, related: Boolean? = null) : List<SectionItem> {
        propertyObject.contentType?.let { contentType ->
            val templateKey = behaviorResolver.getTemplateKey(contentType)
            val template = layoutResolver.getValidTemplate(propertyObject, config.templatePrefix, templateKey)
            val templateReference = layoutResolver.getValidTemplateReference(propertyObject, config.templatePrefix, templateKey)
            val overlayThemeFiles = config.resolveThemeOverlayAsList(propertyObject)
            val dividerConfig = SectionItem.NO_DIVIDER_CONFIG
            return when (contentType) {
                "Article" -> {
                    val relatedContext = (context?.copy() ?: JSONObject()).apply { safePut("related", related == true) }
                    ArticleSectionItem(propertyObject, sectionIdentifier, groupKey, template, templateReference, overlayThemeFiles, dividerConfig, relatedContext).withRelatedArticles()
                }
                "Package" -> listOf(PackageSectionItem(propertyObject, sectionIdentifier, template, templateReference, overlayThemeFiles, dividerConfig, behaviorResolver.getBehavior(contentType), context))
                "List" -> {
                    if (orientation == Orientation.HORIZONTAL) {
                        return if (propertyObject.optSortedListObjects()?.size ?: 0 > 0) {
                            listOf(HorizontalListSectionItem(propertyObject, sectionIdentifier, overlayThemeFiles, template, templateReference, groupKey, dividerConfig, behaviorResolver.getBehavior(contentType), context));
                        } else {
                            emptyList()
                        }
                    } else {
                        propertyObject.optSortedListObjects()?.let { sorted ->
                            val supplementaryItems = supplementarySectionItemFactory.create(propertyObject, contentType)
                            val mutableSorted = sorted.toMutableList()
                            val first = mutableSorted.removeAt(0)
                            val listItems = createList(first, sectionIdentifier, groupKey, context, supplementaryItems, mutableSorted, config.sublistConfig?.behaviour ?: subListBehaviour)
                            return if(config.sublistConfig?.expandable == true) {
                                listOf(ExpandableSectionItem(config.sublistConfig, propertyObject.id, sectionIdentifier, listItems, context))
                            } else {
                                listItems
                            }
                        }
                    }
                    return emptyList()
                }
                else -> listOf(ErrorSectionItem(UnsupportedContentTypeError("contentType not supported: $contentType")))
            }
        }
        return listOf(ErrorSectionItem(UnsupportedContentTypeError("Could not resolve contentType.")))
    }

    private fun createList(first: PropertyObject, sectionIdentifier: String, groupKey: String, context: JSONObject?, supplementaryItems: SupplementarySectionItems?, mutableSorted: MutableList<PropertyObject>, subListBehaviour: PresentationBehaviour?): List<SectionItem>
    = listFromPropertyObject(first, sectionIdentifier, groupKey, context).toMutableList().apply {
        supplementaryItems?.header?.let { add(0, it) }
        (firstOrNull() as? PropertyObjectSectionItem)?.let { it.context = it.context?.copy()?.patch(SUBLIST_CONTEXT) }
        addAll(mutableSorted.flatMap { obj ->
            listFromPropertyObject(obj, sectionIdentifier, groupKey, context).mapIndexedNotNull { index, item ->
                if (index == 0) {
                    (item as? PropertyObjectSectionItem)?.let { it.context = it.context?.copy()?.patch(SUBLIST_CONTEXT) }
                }
                if (config.sublistConfig?.behaviour ?: subListBehaviour == PresentationBehaviour.RELATED) {
                    (item as? ArticleSectionItem)?.asRelatedArticle()
                } else {
                    item
                }
            }
        })
        supplementaryItems?.footer?.let { add(it) }
    }

    private fun ArticleSectionItem.withRelatedArticles(): List<SectionItem> {
        val articleSectionItems = mutableListOf(this)
        propertyObject.optRelatedArticles()?.forEach {
            val relatedArticle = it.asRelatedArticle(sectionIdentifier(), context)
            articleSectionItems.add(relatedArticle)
        }
        return articleSectionItems
    }

    private fun PropertyObject.asRelatedArticle(sectionIdentifier: String, context: JSONObject?): ArticleSectionItem {
        val templatePrefix =
            if (config.templatePrefix.isNullOrEmpty()) "related-" else "${config.templatePrefix}related-"
        val template = layoutResolver.getValidTemplate(this, templatePrefix)
        val templateReference = layoutResolver.getValidTemplateReference(this, templatePrefix)
        val relatedContext = context?.copy() ?: JSONObject()
        relatedContext.apply { safePut("related", true) }
        return ArticleSectionItem(this, sectionIdentifier, null, template, templateReference, null, SectionItem.NO_DIVIDER_CONFIG, relatedContext)
    }

    private fun ArticleSectionItem.asRelatedArticle() =
        propertyObject.asRelatedArticle(sectionIdentifier(), context)

    companion object {
        private val SUBLIST_CONTEXT = JSONObject().apply {
            put("sublist", true)
        }
    }
}

private class UnsupportedContentTypeError(message: String) : Error(message)