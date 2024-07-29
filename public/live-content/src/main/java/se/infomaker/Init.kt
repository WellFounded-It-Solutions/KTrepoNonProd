package se.infomaker

import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import io.reactivex.disposables.Disposable
import se.infomaker.frt.deeplink.ArticleUriTargetResolver
import se.infomaker.frt.moduleinterface.deeplink.DeepLinkUrlManager
import se.infomaker.frtutilities.AbstractInitContentProvider
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.ForegroundDetector
import se.infomaker.frtutilities.ktx.config
import se.infomaker.iap.articleview.item.element.OnLinkClickManager
import se.infomaker.iap.articleview.item.links.LinksHandlerManager
import se.infomaker.iap.theme.ThemeInjector
import se.infomaker.livecontentui.bookmark.BookmarkDownloader
import se.infomaker.livecontentui.config.LiveContentUIConfig
import timber.log.Timber

class Init : AbstractInitContentProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface LcuiEntryPoint {
        fun internalLinkInterceptor(): InternalLinkInterceptor
        fun articleUriTargetResolver(): ArticleUriTargetResolver
    }

    private var garbage: Disposable? = null

    override fun init(context: Context) {
        val entryPoint = EntryPointAccessors.fromApplication(context, LcuiEntryPoint::class.java)
        OnLinkClickManager.add(entryPoint.internalLinkInterceptor())
        LinksHandlerManager.registerHandler(ArticleLinkHandler(), "x-im/article")
        DeepLinkUrlManager.register(entryPoint.articleUriTargetResolver())
        garbage = ForegroundDetector.observable()
            .subscribe({ BookmarkDownloader(context).shouldAutoDownload = it }, Timber::e)

        context.assets.open("livecontentui_theme.json").bufferedReader().use {
            ThemeInjector.getInstance().inject(it.readText())
        }
    }
}