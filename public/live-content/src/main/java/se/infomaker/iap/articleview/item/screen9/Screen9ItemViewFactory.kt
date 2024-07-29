package se.infomaker.iap.articleview.item.screen9

import android.content.res.Resources
import android.graphics.Color
import androidx.appcompat.widget.AppCompatImageView
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.OneShotPreDrawListener
import se.infomaker.frtutilities.ResourceManager
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.ItemViewFactory
import se.infomaker.iap.articleview.util.UI
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.color.ThemeColor
import se.infomaker.iap.theme.image.ThemeImage
import se.infomaker.iap.theme.size.ThemeSize
import se.infomaker.iap.theme.span.ThemeableSpan
import se.infomaker.iap.theme.view.ThemeableTextView
import se.infomaker.iap.theme.view.ThemeableUtil
import timber.log.Timber
import java.lang.IllegalArgumentException

class Screen9ItemViewFactory : ItemViewFactory {

    override fun typeIdentifier(): Any {
        return Screen9Item::class.java
    }

    override fun createView(parent: ViewGroup, resourceManager: ResourceManager, theme: Theme): View {
        return LinearLayout(parent.context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LAYOUT_PARAMS_MATCH_WRAP

            addView(Screen9View(parent.context).apply {
                tag = "screen9"
                this.findViewById<AppCompatImageView>(R.id.text)
            })

            addView(ThemeableTextView(parent.context).apply {
                tag = "text"
                layoutParams = LAYOUT_PARAMS_MATCH_WRAP
            })
        }
    }

    override fun themeView(view: View, item: Item, theme: Theme) {

        view.findViewWithTag<Screen9View>("screen9")?.let {
            it.text.apply {
                val videoStringTheme = if (!it.isLive) "videoplayerDurationText" else "videoplayerLiveText"
                val videoBackgroundTheme = if (!it.isLive) "videoplayerDurationBackground" else "videoplayerLiveBackground"
                val videoPlayIconTheme = if (!it.isLive) "videoplayerIcon" else "videoplayerLiveIcon"

                val themeKeys = listOf(videoStringTheme)
                theme.getText(themeKeys, null)?.apply(theme, this)

                if (theme.getColor(videoBackgroundTheme, null) == null) {
                    this.setBackgroundColor(Color.argb(128, 0, 0, 0))
                } else {
                    this.setBackgroundColor(ThemeableUtil.getThemeColor(theme, videoBackgroundTheme, ThemeColor.TRANSPARENT).get())
                }

                theme.getImage(videoPlayIconTheme, ThemeImage(R.drawable.image_gallery_icon))?.let { image ->
                    image.getImage(this.context)?.let { drawable ->
                        val padding = UI.dp2px(8f).toInt()
                        setPadding(padding, padding, padding, padding)
                        setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
                        compoundDrawablePadding = padding
                    }
                }
            }
        }

        val function = view.getTag(R.id.screen9) as? (Theme, Item) -> Unit
        function?.invoke(theme, item)
    }

    override fun bindView(item: Item, view: View, moduleId: String) {
        val video = item as Screen9Item
        view.findViewWithTag<Screen9View>("screen9")?.apply {
            url = video.videoUrl
            thumbnailUrl = video.thumbnailUrl
            duration = Screen9Item.formatDuration(video.duration)

            val scaleImage = {
                layoutParams.height = try {
                    heightForWidth(width, item.aspectRatio)
                } catch (e: IllegalArgumentException){
                    Timber.d("Using fallback aspect ratio 16:9 instead of ${item.aspectRatio}")
                    heightForWidth(width, Screen9Item.DEFAULT_ASPECT_RATIO)
                }
                this.invalidate()
            }

            OneShotPreDrawListener.add(this) {
                scaleImage.invoke()
            }
        }

        val builder = SpannableStringBuilder()

        val textView = view.findViewWithTag<ThemeableTextView>("text")?.apply {
            builder.clear()
            builder.append(video.title)
            builder.append(DIVIDER)
            builder.append(video.description)
        }

        val tag = { theme: Theme, screen9Item: Item ->
            (screen9Item as? Screen9Item)?.let {
                var count = 0
                it.title?.let { title ->
                    val span = theme.getThemeSpan("videoplayerTitle")
                    builder.addThemeSpan(span, count, title.length)
                    count = title.length + DIVIDER.length // Add count for spacer
                }

                it.description?.let { description ->
                    val span = theme.getThemeSpan("videoplayerDescription")
                    builder.addThemeSpan(span, count, description.length)
                }
                textView?.text = builder
            }
            val textPadding = PADDING_MAP[ThemeableTextView::class.java]
            val horizontalPadding = UI.dp2px(theme.getSize(textPadding?.first, DEFAULT_THEME_SIZE).size).toInt()
            val verticalPadding = UI.dp2px(theme.getSize(textPadding?.second, DEFAULT_THEME_SIZE).size).toInt()
            textView?.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
        }

        view.setTag(R.id.screen9, tag)
    }

    private fun Theme.getThemeSpan(themeKey: String): ThemeableSpan {
        return ThemeableSpan(this, listOf(themeKey))
    }

    private fun SpannableStringBuilder.addThemeSpan(span: ThemeableSpan, start: Int, length: Int) {
        val end = start + length
        setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
    }

    fun heightForWidth(width: Int, aspectRatio: String): Int {
        val parts = aspectRatio.split(":")
        val ratio: Double = when (parts.size) {
            1 -> { parts[0].toDouble() }
            2 -> {
                val widthPart  = parts[0].toDouble()
                val heightPart = parts[1].toDouble()
                widthPart / heightPart
            }
            else -> {
                throw IllegalArgumentException("AspectRatio is invalid, aspectRatio=$aspectRatio")
            }
        }
        return (width / ratio).toInt()
    }

    companion object {
        val PADDING_MAP = mapOf(
                Pair(ThemeableTextView::class.java, Pair("textPaddingHorizontal", "textPaddingVertical")))
        val LAYOUT_PARAMS_MATCH_WRAP = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val DEFAULT_THEME_SIZE = ThemeSize(0f)
        const val DIVIDER = "  "
    }
}