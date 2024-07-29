package se.infomaker.iap.articleview.item.youplay

import android.content.res.Resources
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.drawable.DrawableCompat
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


class YouPlayItemViewFactory : ItemViewFactory {

    override fun typeIdentifier(): Any {
        return YouPlayItem::class.java
    }

    companion object {
        val LAYOUT_PARAMS_MATCH_WRAP = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val PADDING_MAP = mapOf(
                Pair(ThemeableTextView::class.java, Pair("textPaddingHorizontal", "textPaddingVertical")))
        val DEFAULT_THEME_SIZE = ThemeSize(0f)
        const val DIVIDER = "  "
        const val TITLE_THEME_KEY = "videoplayerTitle"
        const val DESCRIPTION_THEME_KEY = "videoplayerDescription"
    }

    override fun createView(parent: ViewGroup, resourceManager: ResourceManager, theme: Theme): View {
        return LinearLayout(parent.context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = YouPlayItemViewFactory.LAYOUT_PARAMS_MATCH_WRAP

            addView(YouplayView(parent.context).apply {
                tag = "youplay"
                layoutParams = YouPlayItemViewFactory.LAYOUT_PARAMS_MATCH_WRAP

                this.findViewById<AppCompatImageView>(R.id.text)
            })

            addView(ThemeableTextView(parent.context).apply {
                tag = "description"
                layoutParams = YouPlayItemViewFactory.LAYOUT_PARAMS_MATCH_WRAP
            })
        }
    }

    override fun themeView(view: View, item: Item, theme: Theme) {
        view.findViewWithTag<YouplayView>("youplay")?.let {
            it.text.apply {
                val videoStringTheme = if (!it.isLive) "videoplayerDurationText" else "videoplayerLiveText"
                val videoBackgroundTheme = if (!it.isLive) "videoplayerDurationBackground" else "videoplayerLiveBackground"
                val videoPlayIconTheme = if (!it.isLive) "videoplayerIcon" else "videoplayerLiveIcon"

                theme.getText(listOf(videoStringTheme), null)?.apply(theme, this)
                setBackgroundColor(theme.getColor(videoBackgroundTheme, null)?.get() ?: Color.argb(128, 0, 0, 0))
                theme.getImage(videoPlayIconTheme, ThemeImage(R.drawable.play))?.let { themeImage ->
                    val padding = UI.dp2px(8f).toInt()
                    setPadding(padding, padding, padding, padding)
                    themeImage.getImage(context)?.let { drawable ->
                        DrawableCompat.setTint(drawable, theme.getColor(videoStringTheme, null)?.get() ?: ThemeColor.WHITE.get())
                        setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
                    }
                    compoundDrawablePadding = padding
                }
            }
        }
    }

    override fun bindView(item: Item, view: View, moduleId: String) {

        val video = item as YouPlayItem
        view.findViewWithTag<YouplayView>("youplay")?.apply {
            this.moduleId = moduleId
            url = video.url
            thumbnailUrl = video.thumbnailUrl
            duration = video.duration
            embedCode = video.embedCode
            isLive = video.live
            autoplay = video.autoplay
            baseUrl = video.baseUrl

            val scaleImage = {

                layoutParams.height = try {
                    heightForWidth(width, item.aspectRatio)
                }catch (e: IllegalArgumentException){
                    Timber.d("Using fallback aspect ratio 16:9 instead of ${item.aspectRatio}")
                    heightForWidth(width, YouPlayItem.DEFAULT_ASPECT_RATIO)
                }
                this.invalidate()
            }

            OneShotPreDrawListener.add(this) {
                scaleImage.invoke()
            }
        }

        val builder = SpannableStringBuilder()
        val textView = view.findViewWithTag<ThemeableTextView>("description")?.apply {
            for (text in item.textToShow) {
                when (text) {
                    "all" -> {
                        builder.append(item.title).append(DIVIDER).append(item.description)
                    }
                    "title" -> {
                        builder.append(item.title)
                    }
                    "description" -> {
                        builder.append(item.description)
                    }
                    "none" -> {
                        builder.clear()
                    }
                }

                if (item.textToShow.lastIndexOf(text) != item.textToShow.size -1) {
                    builder.append(DIVIDER)
                }
            }
            if (item.textToShow.contains("all"))

            builder.append(item.description)
        }

        with(view) {
            setTag(R.id.youplay_description,
                { theme: Theme, item:Item ->
                    item as YouPlayItem
                    var count = 0
                    for (text in item.textToShow) {
                        when (text) {
                            "all" -> {
                                builder.addThemeSpan(theme.getThemeSpan(TITLE_THEME_KEY), count, item.title.length)
                                count += item.title.length
                                count += DIVIDER.length
                                builder.addThemeSpan(theme.getThemeSpan(DESCRIPTION_THEME_KEY), count, count + item.description.length)
                                count += item.description.length
                            }
                            "title" -> {
                                builder.addThemeSpan(theme.getThemeSpan(TITLE_THEME_KEY),count, item.title.length)
                                count += item.title.length + DIVIDER.length
                            }
                            "description" -> {
                                builder.addThemeSpan(theme.getThemeSpan(DESCRIPTION_THEME_KEY), count, count + item.description.length)
                                count += item.description.length
                            }
                        }
                        if (item.textToShow.lastIndexOf(text) != item.textToShow.size -1) {
                            count += DIVIDER.length
                        }
                    }

                    textView?.text = builder
                    val textPadding = PADDING_MAP[ThemeableTextView::class.java]
                    val horizontalPadding = UI.dp2px(theme.getSize(textPadding?.first, DEFAULT_THEME_SIZE).size).toInt()
                    val verticalPadding = UI.dp2px(theme.getSize(textPadding?.second, DEFAULT_THEME_SIZE).size).toInt()
                    textView?.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
                }
        )
        }
    }

    private fun Theme.getThemeSpan(themeKey: String): ThemeableSpan {
        return ThemeableSpan(this, listOf(themeKey))
    }
    
    private fun SpannableStringBuilder.addThemeSpan(span: ThemeableSpan, start: Int, end: Int) {
        setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
    }

    private fun heightForWidth(width: Int, aspectRatio: String): Int
    {
        val parts = aspectRatio.split(":")
        val ratio: Double = when (parts.size){
            1 -> {
                parts[0].toDouble()
            }
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
}