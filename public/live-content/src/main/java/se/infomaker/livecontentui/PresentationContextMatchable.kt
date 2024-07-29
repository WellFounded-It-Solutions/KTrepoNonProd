package se.infomaker.livecontentui

import se.infomaker.iap.articleview.presentation.match.MatchMap

interface PresentationContextMatchable {
    val presentationContextMatchMap: MatchMap?

    companion object {

        @JvmStatic
        fun makePresentationContextMatchMap(presentationContextKey: String?, presentationContextValues: String?): MatchMap? {
            if (presentationContextKey != null && presentationContextValues != null) {
                return mapOf("context.$presentationContextKey" to presentationContextValues.split(","))
            }
            return null
        }
    }
}