package se.infomaker.frtutilities

import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import se.infomaker.frtutilities.connectivity.Connectivity

class Setup : AbstractInitContentProvider() {

    override fun init(context: Context) {
        ProcessLifecycleOwner.get().lifecycle.addObserver(ForegroundDetector)
        Connectivity.init(context)
    }
}