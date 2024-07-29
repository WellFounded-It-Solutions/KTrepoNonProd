package se.infomaker.livecontentui.section

import se.infomaker.livecontentui.config.ContentTypeTemplateConfig

/**
 * Indicates that the a [SectionItem] wraps other SectionItems to present them in a
 * non standard way.
 */
interface SectionItemWrapper {

    /**
     * Config used to override default moduleId used when running sub item click actions.
     *
     * Can also provide its own actions to be performed when a certain sub item is clicked,
     * which will call the [se.infomaker.frt.moduleinterface.action.GlobalActionHandler].
     */
    val config: ContentTypeTemplateConfig.Config?
}