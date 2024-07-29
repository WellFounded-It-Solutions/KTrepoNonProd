package se.infomaker.livecontentui.impressions

import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import se.infomaker.frtutilities.JSONUtil
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.livecontentmanager.parser.PropertyObject
import timber.log.Timber
import java.util.Timer
import kotlin.concurrent.timerTask

/**
 * Monitor the users read progress of an article
 * The assumption is that a user can read 300 words/minute
 * The user also has to at least scroll 90% of the article.
 * when BOTH conditions are true, onArticleRead is invoked.
 */
class ArticleReadMonitor(private val recyclerView: RecyclerView, private val contentObservable: Observable<ContentStructure>, private val onArticleRead: (PropertyObject) -> Unit): RecyclerView.OnScrollListener() {

    private var garbage = CompositeDisposable()
    private var viewTime = 0
        set(value) {
            field = value
            wordCount?.let {
                if (it > 0 && (it.toDouble()/300.0 * 60) < value) {
                    timerExpired = true
                    notifyIfDone()
                }
            }
        }
    private var targetPercentReached: Boolean = false
    private var hasNotified: Boolean = false
    private var resumed: Boolean = false
    private var article: PropertyObject? = null
    private var timer: Timer? = null
    private var wordCount: Int? = null
    private var timerExpired: Boolean = false
    private var maxScrollPercent = 0

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (!targetPercentReached) {
            updateMaxScrollPercent(recyclerView)
        }
    }

    private fun updateMaxScrollPercent(recyclerView: RecyclerView) {
        val offset = recyclerView.computeVerticalScrollOffset() + 1
        val extent = recyclerView.computeVerticalScrollExtent()
        val range = recyclerView.computeVerticalScrollRange()

        // Filter out bogus values
        if (offset == 1 && extent == 0 && range == 0) {
            return
        }

        val toScroll = Math.max(range - extent, 1)
        val percentage = Math.round(100.0f * offset / toScroll)
        maxScrollPercent = Math.max(percentage, maxScrollPercent)

        if (TARGET_PERCENT <= maxScrollPercent) {
            Timber.d("Scrolled enough")
            targetPercentReached = true
            notifyIfDone()
        }
    }

    private fun notifyIfDone() {
        if (targetPercentReached && timerExpired && !hasNotified) {
            hasNotified = true
            article?.let(onArticleRead)
            pause()
            Timber.d("Article read")
        }
    }

    /**
     * Call when the user is viewing the article, safe to call multiple times without a
     * call to {@link this#pause()} in between.
     */
    fun resume() {
        if (!hasNotified && !resumed) {
            /*
             * We need to make sure we get an initial reading even if we don't scroll
             */
            recyclerView.postDelayed({
                updateMaxScrollPercent(recyclerView)
            }, 50)

            timer = Timer().also {
                it.schedule(timerTask {
                    viewTime++
                }, 1000, 1000)
            }
            recyclerView.addOnScrollListener(this)
            garbage.add(contentObservable.subscribe { contentStructure ->
                if (!hasNotified) {
                    article = PropertyObject(contentStructure.properties, JSONUtil.getString(contentStructure.properties, "contentId"))
                    wordCount = contentStructure.body.items.map { it.wordCount() }.reduce { acc, i -> acc + i }.also {
                        val targetTime = Math.round(it.toDouble()/300.0 * 60)
                        Timber.d("Article target read time ${targetTime}s" )
                    }
                }
            })
        }
        resumed = true
    }

    /**
     * Call when the user stops viewing the article, safe to call without a call to
     * {@link this#resume()} first.
     */
    fun pause() {
        timer?.cancel()
        recyclerView.removeOnScrollListener(this)
        garbage.clear()
        resumed = false
    }

    companion object {
        const val TARGET_PERCENT = 90
    }
}