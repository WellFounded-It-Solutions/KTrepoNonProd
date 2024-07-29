package se.infomaker.livecontentui

import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.frtutilities.ktx.config
import se.infomaker.frtutilities.ktx.moduleInfo
import se.infomaker.frtutilities.ktx.requireActivity
import se.infomaker.livecontentui.bookmark.BookmarkFeatureFlag
import se.infomaker.livecontentui.config.LiveContentUIConfig


open class MenuActivity : AppCompatActivity() {

    private val moduleInfo by moduleInfo()
    private val config: LiveContentUIConfig by config { moduleInfo }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.base_article_menu, menu)

        config.sharing?.let {
            if (!it.shareApiUrl.isNullOrBlank()) {
                menuInflater.inflate(R.menu.sharing_menu, menu)
                menu.findItem(R.id.menu_item_share)?.isVisible = false
            }
        }

        if (BookmarkFeatureFlag.isEnabled(requireActivity())){
            menuInflater.inflate(R.menu.bookmark_menu, menu)
            menu.findItem(R.id.menu_item_bookmark)?.isVisible = false
        }

        return true
    }
}