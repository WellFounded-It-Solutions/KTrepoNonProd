package se.infomaker.livecontentui.bookmark

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.children
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import se.infomaker.datastore.Bookmark
import se.infomaker.datastore.DatabaseSingleton
import se.infomaker.iap.provisioning.ui.dp2px
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.image.ThemeImage
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.livecontentui.PropertyObjectItemProvider

class BookmarkOverlayItemDecoration(recyclerView: RecyclerView, theme: Theme, lifecycleOwner: LifecycleOwner) : RecyclerView.ItemDecoration() {

    private val overlay: Drawable
    private var bookmarks = emptyList<Bookmark>()

    init {
        val overlayIcon = theme.getImage("bookmarkOverlay", ThemeImage(R.drawable.ic_bookmark)).getImage(recyclerView.context).mutate().also { it.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP) }
        val overlayBackground = AppCompatResources.getDrawable(recyclerView.context, R.drawable.bookmark_overlay_background)
        overlay = LayerDrawable(arrayOf(overlayBackground, overlayIcon))

        DatabaseSingleton.getDatabaseInstance().bookmarkDao().all().observe(lifecycleOwner, Observer {
            bookmarks = it
            recyclerView.adapter?.notifyDataSetChanged()
        })
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        (parent.adapter as? PropertyObjectItemProvider)?.let {
            parent.children.forEach { child ->
                val position = parent.getChildAdapterPosition(child)
                if (position != RecyclerView.NO_POSITION) {
                    it.getPropertyObjectForPosition(position)?.let { childContent ->
                        if (bookmarks.map { it.uuid }.contains(childContent.id)) {
                            val top = child.top + MARGIN
                            val right = child.right - MARGIN
                            val left = right - SIZE
                            val bottom = top + SIZE
                            overlay.setBounds(left, top, right, bottom)
                            overlay.draw(c)
                        }
                    }
                }
            }
        }
    }

    companion object {
        private val SIZE = 28.dp2px()
        private val MARGIN = 4.dp2px()
    }
}