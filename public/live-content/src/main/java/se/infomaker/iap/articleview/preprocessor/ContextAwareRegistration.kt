package se.infomaker.iap.articleview.preprocessor

import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import se.infomaker.frtutilities.AbstractInitContentProvider
import se.infomaker.iap.articleview.extensions.ifragasatt.IfragasattPreprocessor
import se.infomaker.iap.articleview.preprocessor.date.DatePreprocessor

class ContextAwareRegistration: AbstractInitContentProvider() {
    override fun init(context: Context) {
        val entryPoint = EntryPointAccessors.fromApplication(context, ArticleViewEntryPoint::class.java)
        PreprocessorManager.registerPreprocessor("ifragasatt", entryPoint.ifragasattPreprocessor())
        PreprocessorManager.registerPreprocessor("date", DatePreprocessor(context.applicationContext))
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ArticleViewEntryPoint {
        fun ifragasattPreprocessor(): IfragasattPreprocessor
    }
}