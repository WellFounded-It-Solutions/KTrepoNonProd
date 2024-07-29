package se.infomaker.frt.ui.fragment

import android.os.Bundle

data class TabConfig(val id: String, val module: String, val title: String, val parent: String, val defaultSelected: Boolean?) {
    fun bundle(): Bundle {
        return Bundle().also {
            it.putString("moduleId", id)
            it.putString("moduleName", module)
            it.putString("parent", parent)
            it.putString("title", title)
            it.putString("moduleTitle", title)
        }
    }
}
