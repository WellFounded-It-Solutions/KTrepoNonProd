package se.infomaker.iap.khaleej

import android.content.Context
import com.navigaglobal.mobile.ad.vuukle.VuukleAdProvider
import com.navigaglobal.mobile.consent.ConsentManagerProvider
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import io.reactivex.disposables.Disposable
import se.infomaker.frt.statistics.StatisticsManager
import se.infomaker.frtutilities.AbstractInitContentProvider
import se.infomaker.frtutilities.ForegroundDetector
import se.infomaker.iap.SpringBoardManager
import se.infomaker.iap.articleview.item.DefaultItemViewFactoryProvider
import se.infomaker.iap.articleview.item.prayer.PrayerTimesViewFactory
import se.infomaker.iap.articleview.preprocessor.CustomerContentSubTypePreprocessor
import se.infomaker.iap.articleview.preprocessor.PreprocessorManager
import se.infomaker.iap.articleview.preprocessor.prayer.PrayerTimesPreprocessor
import se.infomaker.library.AdViewFactory

class Setup : AbstractInitContentProvider() {

    private lateinit var garbage: Disposable

    override fun init(context: Context) {
        val entryPoint = EntryPointAccessors.fromApplication(context, KhaleejTimesEntryPoint::class.java)
        val defaultItemViewFactoryProvider = entryPoint.defaultItemViewFactoryProvider()
        PreprocessorManager.registerPreprocessor("prayer", PrayerTimesPreprocessor())
        PreprocessorManager.registerPreprocessor("customerContentSubType", CustomerContentSubTypePreprocessor())
        defaultItemViewFactoryProvider.registerViewFactory(PrayerTimesViewFactory())

        // ConsentManagerProvider.registerFactory(TealiumConsentManagerFactory) // Removed Tealium-specific code

        // StatisticsManager.getInstance().registerInterceptor(TealiumEventInterceptor()) // Removed Tealium-specific code

        val router = KhaleejTimesRouter(SpringBoardManager.router)
        SpringBoardManager.router = router

        garbage = ForegroundDetector.observable().subscribe {
            // Removed Tealium-specific location tracking code
        }

        AdViewFactory.registerAdProvider("Vuukle", VuukleAdProvider)
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface KhaleejTimesEntryPoint {
        fun defaultItemViewFactoryProvider(): DefaultItemViewFactoryProvider
    }
}
