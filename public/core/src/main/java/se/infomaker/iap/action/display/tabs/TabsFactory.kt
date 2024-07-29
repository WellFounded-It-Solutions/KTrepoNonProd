package se.infomaker.iap.action.display.tabs

import android.content.Context
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import se.infomaker.iap.action.Operation

object TabsFactory {
    fun createFragment(context: Context?, operation: Operation): androidx.fragment.app.Fragment {
        return TabsFragment.newInstance(Gson().fromJson(operation.parameters.toString(), TabsConfig::class.java))
    }
}