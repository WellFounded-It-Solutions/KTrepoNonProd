package se.infomaker

import android.content.Context
import se.infomaker.iap.articleview.item.element.OnInterceptResult
import se.infomaker.iap.articleview.item.element.OnLinkClickInterceptor
import se.infomaker.livecontentui.common.di.GlobalLiveContentUiConfig
import se.infomaker.livecontentui.config.LiveContentUIConfig
import se.infomaker.livecontentui.livecontentdetailview.activity.ArticlePagerActivity
import se.infomaker.livecontentui.sharing.SharingManager
import javax.inject.Inject

class InternalLinkInterceptor @Inject constructor(
    @GlobalLiveContentUiConfig private val config: LiveContentUIConfig,
    private val sharingManager: SharingManager
) : OnLinkClickInterceptor {

    override fun intercept(context: Context, url: String, resultListener: OnInterceptResult) {
        config.sharing?.shareApiUrl?.run {
            if (!sharingManager.canHandleUrl(url)) {
                resultListener.onInterceptResult(false, url)
                return
            }
            sharingManager.getArticleUuid(url).subscribe({
                ArticlePagerActivity.openArticle(context, config.linkModuleId ?: "global", "", it.uuid)
                resultListener.onInterceptResult(true, url)
            }, {
                resultListener.onInterceptResult(false, url)
            })
            return
        }
        resultListener.onInterceptResult(false, url)
    }
}