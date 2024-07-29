package se.infomaker.livecontentui.prefetch

import android.content.Context

interface BackgroundPrefetcher {
    fun fetchData(context: Context)
}