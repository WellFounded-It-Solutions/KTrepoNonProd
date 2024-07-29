package se.infomaker.livecontentui.offline

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.OneShotPreDrawListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.transition.TransitionManager
import com.google.android.material.appbar.AppBarLayout
import com.navigaglobal.mobile.ktx.findAncestorOfType
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.frtutilities.connectivity.Connectivity
import se.infomaker.frtutilities.connectivity.hasInternetConnection
import se.infomaker.livecontentui.extensions.cloneChild
import se.infomaker.livecontentui.livecontentrecyclerview.view.ContentInsets
import se.infomaker.livecontentui.livecontentrecyclerview.view.LiveBinding
import se.infomaker.livecontentui.livecontentrecyclerview.view.OnContentInsetsChangedListener
import timber.log.Timber

class OfflineBannerCoordinator(private val bannerLayout: OfflineBannerLayout, private val resourceManager: ResourceManager) : LifecycleObserver {

    private val contentInsetsListeners = mutableSetOf<OnContentInsetsChangedListener>()
    private val bannerParent by lazy { bannerLayout.parent as? ConstraintLayout ?: throw IllegalArgumentException("Offline banner must be contained in a ConstraintLayout.") }
    private val onlineConstraints = ConstraintSet().also { it.cloneChild(bannerParent, bannerLayout) }
    private val offlineConstraints = ConstraintSet().also {
        it.clone(onlineConstraints)
        it.constrainHeight(bannerLayout.id, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private var connectionDisposable: Disposable? = null
    private var lastContentInsets: ContentInsets? = null
    private var liveBinding: LiveBinding? = null

    init {
        OneShotPreDrawListener.add(bannerLayout) {
            bannerLayout.findAncestorOfType(AppBarLayout::class.java).let {
                bannerLayout.attach(it)
            }
        }

        if (!bannerLayout.context.hasInternetConnection()) {
            applyOfflineConstraints(false)
        }
        else {
            applyOnlineConstraints(false)
        }

        bannerLayout.bindTitle(resourceManager)

        bannerLayout.addOnLayoutChangeListener { _, _, top, _, bottom, _, oldTop, _, oldBottom ->
            if (top != oldTop || bottom != oldBottom) {
                val contentInsets = if (top == 0 && bottom == 0) {
                    ContentInsets()
                }
                else {
                    ContentInsets(top = bannerParent.y.toInt() + top + bottom)
                }
                contentInsetsListeners.forEach { listener -> listener.onContentInsetsChanged(contentInsets) }
                lastContentInsets = contentInsets
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onStart() {
        connectionDisposable = Connectivity.observable()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { e -> Timber.e(e) }
                .subscribe {
                    if (it == true) {
                        applyOnlineConstraints()
                        Timber.d("Detected internet connection, will hide offline banner.")
                    }
                    else {
                        applyOfflineConstraints()
                        Timber.d("Internet connection lost, will show offline banner.")
                    }
                }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onStop() {
        liveBinding?.recycle()
        connectionDisposable?.dispose()
    }

    fun bind(model: OfflineBannerModel) {
        bindNow(model)

        liveBinding?.recycle()
        liveBinding = LiveBinding.add(bannerLayout, { bindNow(model) }, 30)
        if (bannerLayout.isAttachedToWindow) {
            liveBinding?.start()
        }
    }

    private fun bindNow(model: OfflineBannerModel) {
        bannerLayout.bindModel(model, resourceManager)
    }

    private fun applyOnlineConstraints(transition: Boolean = true) {
        if (transition) {
            TransitionManager.beginDelayedTransition(bannerParent)
        }
        onlineConstraints.applyTo(bannerParent)
    }

    private fun applyOfflineConstraints(transition: Boolean = true) {
        if (transition) {
            TransitionManager.beginDelayedTransition(bannerParent)
        }
        offlineConstraints.applyTo(bannerParent)
    }

    fun addOnContentInsetsChangedListener(listener: OnContentInsetsChangedListener) {
        contentInsetsListeners.add(listener)
        lastContentInsets?.let {
            listener.onContentInsetsChanged(it)
        }
    }

    fun removeOnContentInsetsChangedListener(listener: OnContentInsetsChangedListener) {
        contentInsetsListeners.remove(listener)
    }
}