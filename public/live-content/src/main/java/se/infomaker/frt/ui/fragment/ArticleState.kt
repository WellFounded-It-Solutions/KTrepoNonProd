package se.infomaker.frt.ui.fragment

import java.util.Date

data class ArticleState(val isLoading: Boolean, val hasError: Boolean, val articles: List<ArticleRecord>, val updateInfo: UpdateInfo) {
    companion object {
        @JvmStatic
        val INITIAL = ArticleState(isLoading = true, hasError = false, articles = emptyList(), updateInfo = emptyUpdateInfo())
    }
}

data class UpdateInfo(val lastUpdated: Date? = null, val lastUpdateAttempt: Date? = null)

private fun emptyUpdateInfo() = UpdateInfo()