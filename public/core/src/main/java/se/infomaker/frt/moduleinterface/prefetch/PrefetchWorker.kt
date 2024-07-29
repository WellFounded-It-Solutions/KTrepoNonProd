package se.infomaker.frt.moduleinterface.prefetch

import android.content.Context

interface PrefetchWorker {

    fun prefetch(context: Context): Boolean

}