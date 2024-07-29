package com.navigaglobal.mobile.ad.huaweiadskit.view

import android.content.Context
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.huawei.hms.ads.banner.BannerView
import se.infomaker.library.Destroyable

internal class AdWrapperView(context: Context) : FrameLayout(context), LifecycleObserver, Destroyable {

    var view: BannerView? = null
        set(value) {
            value?.let { addView(it) }
            field = value
        }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun resume() {
        view?.resume()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun pause() {
        view?.pause()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    override fun destroy() {
        view?.adListener = null
        view?.destroy()
        view = null
    }
}