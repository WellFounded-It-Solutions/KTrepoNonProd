package se.infomaker.iap.articleview.item.flowplayer

import android.graphics.Color
import androidx.appcompat.widget.AppCompatImageView
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.OneShotPreDrawListener
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.flowplayer.library.FlowplayerView
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.ItemViewFactory
import se.infomaker.iap.articleview.util.UI
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.color.ThemeColor
import se.infomaker.iap.theme.image.ThemeImage
import se.infomaker.iap.theme.view.ThemeableTextView
import se.infomaker.iap.theme.view.ThemeableUtil
import timber.log.Timber
import java.lang.IllegalArgumentException


class FlowPlayerItemViewFactory : ItemViewFactory {

    override fun typeIdentifier(): Any {
        return FlowPlayerItem::class.java
    }

    companion object {
        val LAYOUT_PARAMS_MATCH_WRAP = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun createView(parent: ViewGroup, resourceManager: ResourceManager, theme: Theme): View {
        return LinearLayout(parent.context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FlowPlayerItemViewFactory.LAYOUT_PARAMS_MATCH_WRAP

            addView(FlowplayerView(parent.context).apply {
                tag = "flowplayer"
                layoutParams = FlowPlayerItemViewFactory.LAYOUT_PARAMS_MATCH_WRAP

                this.findViewById<AppCompatImageView>(R.id.text)
            })

            addView(ThemeableTextView(parent.context).apply {
                tag = "description"
                layoutParams = FlowPlayerItemViewFactory.LAYOUT_PARAMS_MATCH_WRAP

            })
        }
    }

    override fun themeView(view: View, item: Item, theme: Theme) {

        view.findViewWithTag<FlowplayerView>("flowplayer")?.let {

            it.text.apply {
                var videoStringTheme = if (!it.isLive) "videoplayerDurationText" else "videoplayerLiveText"
                var videoBackgroundTheme = if (!it.isLive) "videoplayerDurationBackground" else "videoplayerLiveBackground"
                var videoPlayIconTheme = if (!it.isLive) "videoplayerIcon" else "videoplayerLiveIcon"

                var themeKeys = listOf(videoStringTheme)
                theme.getText(themeKeys, null) ?. let {
                    it.apply(theme, this)
                }

                if (theme.getColor(videoBackgroundTheme, null) == null) {
                    this.setBackgroundColor(Color.argb(128, 0, 0, 0))
                } else {
                    this.setBackgroundColor(ThemeableUtil.getThemeColor(theme, videoBackgroundTheme, ThemeColor.TRANSPARENT).get())
                }

                theme.getImage(videoPlayIconTheme, ThemeImage(R.drawable.image_gallery_icon))?.let {

                    it.getImage(this.context) ?. let {
                        val padding = UI.dp2px(8f).toInt()
                        setPadding(padding, padding, padding, padding)
                        setCompoundDrawablesWithIntrinsicBounds(it, null, null, null)
                        compoundDrawablePadding = padding
                    }
                }
            }
            view.findViewWithTag<ThemeableTextView>("description")?.apply {
                themeKeys = listOf("videoplayerTitle")
                if (theme.getText(themeKeys, null) == null) {
                    themeKeys = null
                    setTextColor(Color.WHITE)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                }
                apply(theme)
            }
        }
    }

    override fun bindView(item: Item, view: View, moduleId: String) {

        val video = item as FlowPlayerItem
        view.findViewWithTag<FlowplayerView>("flowplayer")?.apply {
            url = video.url
            thumbnailUrl = video.thumbnailUrl
            duration = video.duration
            isLive = video.live
            playerId = video.playerId
            autoplay = video.autoplay
            mute = video.mute
            start = video.start
            queryParameters = video.queryParameters

            val scaleImage = {

                layoutParams.height = try {
                    heightForWidth(width, item.aspectRatio)
                }catch (e: IllegalArgumentException){
                    Timber.d("Using fallback aspect ratio 16:9 instead of ${item.aspectRatio}")
                    heightForWidth(width, FlowPlayerItem.DEFAULT_ASPECT_RATIO)
                }
                this.invalidate()
            }

            OneShotPreDrawListener.add(this) {
                scaleImage.invoke()
            }
        }
        view.findViewWithTag<ThemeableTextView>("description")?.apply {
            this.text = video.title
        }
    }

    fun heightForWidth(width: Int, aspectRatio: String): Int
    {
        val parts = aspectRatio.split(":")
        var ratio: Double
        when (parts.size){
            1 -> {
                ratio = parts[0].toDouble()
            }
            2 -> {
                val widthPart  = parts[0].toDouble()
                val heightPart = parts[1].toDouble()
                ratio =  widthPart / heightPart
            }
            else -> {
                throw IllegalArgumentException("AspectRatio is invalid, aspectRatio=$aspectRatio")
            }
        }
        return (width / ratio).toInt()
    }
}