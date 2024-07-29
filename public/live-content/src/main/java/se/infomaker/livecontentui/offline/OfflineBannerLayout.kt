package se.infomaker.livecontentui.offline

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.updatePadding
import com.google.android.material.appbar.AppBarLayout
import com.navigaglobal.mobile.ktx.safeUpdateLayoutParams
import se.infomaker.frtutilities.DateUtil
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.size.ThemeSize
import se.infomaker.iap.theme.view.ThemeableLinearLayout
import com.navigaglobal.mobile.livecontent.R
import com.navigaglobal.mobile.livecontent.databinding.OfflineBannerLayoutBinding
import se.infomaker.frt.ui.view.extensions.safeRequestLayout
import se.infomaker.livecontentui.extensions.setPadding
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

open class OfflineBannerLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ThemeableLinearLayout(context, attrs), AppBarLayout.OnOffsetChangedListener {

    private val binding: OfflineBannerLayoutBinding

    private var originalBottomPadding = 0
    private var needsSubtitleMeasurement = false
    private var title: String? = null
    private var subtitle: String? = null
    private var attachedAppBarLayout: AppBarLayout? = null

    private var wrappingOfflineBannerSubtitleHeight = 0
        set(value) {
            if (value != field) {
                safeRequestLayout()
            }
            field = value
        }

    init {
        orientation = LinearLayoutCompat.VERTICAL
        View.inflate(context, R.layout.offline_banner_layout, this)
        binding = OfflineBannerLayoutBinding.bind(this)

        setThemeFallbackBackgroundColor("#202020")
        setThemeBackgroundColor("offlineBannerBackground")
    }

    override fun onSaveInstanceState(): Parcelable? {
        val state = Bundle()
        state.putParcelable(STATE_KEY_SUPER, super.onSaveInstanceState())
        state.putString(STATE_KEY_SUBTITLE, subtitle)
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        (state as? Bundle)?.let {
            super.onRestoreInstanceState(it.getParcelable(STATE_KEY_SUPER))
            it.getString(STATE_KEY_SUBTITLE)?.let { subtitle ->
                this.subtitle = subtitle
                binding.offlineBannerSubtitle?.text = subtitle
            }
        }
    }

    fun attach(appBarLayout: AppBarLayout?) {
        binding.offlineBannerSubtitle?.let { _ ->
            appBarLayout?.let {
                it.addOnOffsetChangedListener(this)
                attachedAppBarLayout = appBarLayout
            } ?: run {
                collapseOfflineSubtitle(0f)
            }
        }
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        collapseOfflineSubtitle(abs(verticalOffset.toFloat() / appBarLayout.totalScrollRange))
    }

    fun bindTitle(resourceManager: ResourceManager) {
        bindTitleResource(resourceManager, "offline_banner_title")
    }

    private fun bindTitleResource(resourceManager: ResourceManager, resourceName: String, arg: String? = null) {
        title = resourceManager.getString(resourceName, null, arg)
        binding.offlineBannerTitle.text = title
    }

    fun bindModel(model: OfflineBannerModel, resourceManager: ResourceManager) {

        val dateString = DateUtil.timeAgoSince(context, model.date).decapitalize()

        bindTitle(resourceManager)
        val subtitleResource = if (model.hasCachedContent) {
            "offline_banner_subtitle"
        }
        else {
            "offline_banner_no_result"
        }

        subtitle = resourceManager.getString(subtitleResource, null, dateString)
        binding.offlineBannerSubtitle?.let {
            if (subtitle != it.text) {
                it.text = subtitle
                needsSubtitleMeasurement = true
            }
        } ?: run {
            binding.offlineBannerTitle.text = resources.getString(R.string.offline_banner_merged_title_and_subtitle, title, subtitle)
        }
    }

    override fun apply(theme: Theme) {
        super.apply(theme)

        val horizontalPadding = theme.getSize("offlineBannerPaddingHorizontal", DEFAULT_PADDING_HORIZONTAL).sizePx.toInt()
        val verticalPadding = theme.getSize("offlineBannerPaddingVertical", DEFAULT_PADDING_VERTICAL).sizePx.toInt().also { originalBottomPadding = it }
        setPadding(horizontal = horizontalPadding, vertical = verticalPadding)

        needsSubtitleMeasurement = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (needsSubtitleMeasurement) {

            // TODO Do we need to check the LPs of the specific child? So far: No.
            val widthSpec = MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.AT_MOST)
            val heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            binding.offlineBannerSubtitle?.measure(widthSpec, heightSpec)
            wrappingOfflineBannerSubtitleHeight = binding.offlineBannerSubtitle?.measuredHeight ?: 0
        }
    }

    private fun collapseOfflineSubtitle(collapsed: Float) {

        binding.offlineBannerSubtitle?.let {
            if (it.text.isNotEmpty()) {

                val totalNewHeight = max(wrappingOfflineBannerSubtitleHeight - (wrappingOfflineBannerSubtitleHeight * collapsed).toInt(), 0)
                setSubtitleHeight(totalNewHeight)

                val subtitleAlpha = max(1 - collapsed * 1.5f, 0f)
                setSubtitleAlpha(subtitleAlpha)
            }
            else {
                setSubtitleHeight(0)
                setSubtitleAlpha(0f)
            }
        }
    }

    private fun setSubtitleHeight(subtitleHeight: Int) {
        val subtitleWithPaddingHeight = subtitleHeight + originalBottomPadding
        val newSubtitleHeight = min(subtitleWithPaddingHeight, wrappingOfflineBannerSubtitleHeight)
        binding.offlineBannerSubtitle?.let {
            if (it.layoutParams.height != newSubtitleHeight) {
                it.safeUpdateLayoutParams { height = newSubtitleHeight }
            }
        }

        val newBottomPadding = subtitleWithPaddingHeight - newSubtitleHeight
        updatePadding(bottom = newBottomPadding)
    }

    private fun setSubtitleAlpha(subtitleAlpha: Float) {
        binding.offlineBannerSubtitle?.let {
            it.alpha = subtitleAlpha
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        attachedAppBarLayout?.removeOnOffsetChangedListener(this)
    }

    companion object {
        private val STATE_KEY_SUPER = "${OfflineBannerLayout::class.java.canonicalName}.super.state"
        private val STATE_KEY_SUBTITLE = "${OfflineBannerLayout::class.java.canonicalName}.subtitle"

        private val DEFAULT_PADDING_HORIZONTAL = ThemeSize(12f)
        private val DEFAULT_PADDING_VERTICAL = ThemeSize(8f)
    }
}