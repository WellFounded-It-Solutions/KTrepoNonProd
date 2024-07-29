package se.infomaker.coremedia.slideshow

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.transition.Transition
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.OneShotPreDrawListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.navigaglobal.mobile.R
import se.infomaker.frt.statistics.StatisticsEvent
import se.infomaker.frt.statistics.StatisticsManager
import se.infomaker.iap.ui.theme.OverlayThemeProvider
import timber.log.Timber

class SlideshowActivity : AppCompatActivity() {
    companion object {
        const val ARG_IMAGES = "imagesMap"
        const val ARG_CURRENT_IMAGE = "currentImage"
        const val ARG_CAPTION_FONT = "captionFont"
        const val ARG_CAPTION_FONT_SIZE = "captionFontSize"

        //These are only for statistics
        const val ARG_MODULE_ID = "moduleId"
        const val ARG_MODULE_NAME = "moduleName"
        const val ARG_MODULE_TITLE = "moduleTitle"
        const val ARG_ARTICLE_HEADLINE = "articleHeadline"
        const val ARG_ARTICLE_UUID = "articleUuid"

        const val TEXT_ANIMATE_TIME = 100L
        const val SHARED_TRANSITION_TIME = 200L

        fun createIntent(context: Context, currentImage: String, images: List<ImageObject>, extras: Bundle? = null,
                         captionFont: String? = null, captionFontSize: Double? = null): Intent {
            return Intent(context, SlideshowActivity::class.java).apply {
                putExtra(ARG_CURRENT_IMAGE, currentImage)
                putExtra(ARG_IMAGES, Gson().toJson(images))
                putExtra(ARG_CAPTION_FONT, captionFont)
                putExtra(ARG_CAPTION_FONT_SIZE, captionFontSize)
                extras?.let { putExtras(it) }
            }
        }
    }

    private var images = listOf<ImageObject>()
    private var textVisible = true
    private var hasRotated = false
    private var currentImageIndex = 0
    private var startingPos: Int = 0
    private var pager: SlideshowViewPager? = null
    private var captionView: TextView? = null
    private var photographerView: TextView? = null
    private var currentPageIndicatorTextView: TextView? = null
    private var totalPageIndicatorTextView: TextView? = null
    private var photographerPrefix: String? = null

    private lateinit var pageIndicatorBackground: View
    private lateinit var textBackground: View



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.postponeEnterTransition(this)
        setContentView(R.layout.activity_slideshow)
        val rootView = findViewById<FrameLayout>(R.id.slideshowRoot)
        rootView?.run {
            setBackgroundColor(Color.TRANSPARENT)
        }
        captionView = findViewById(R.id.imageDescription)
        photographerView = findViewById(R.id.imagePhotographer)
        textBackground = findViewById<RelativeLayout>(R.id.textBackground)

        currentPageIndicatorTextView = findViewById(R.id.pageIndicatorCurrentTextView)
        totalPageIndicatorTextView = findViewById(R.id.pageIndicatorTotalTextView)
        pageIndicatorBackground = findViewById<RelativeLayout>(R.id.pageIndicatorBackground)

        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        showSystemUI()

        val args = intent.extras
        //Get images
        val type = object : TypeToken<List<ImageObject>>() {}
        images = Gson().fromJson(args?.getString(ARG_IMAGES), type.type)

        val currentImage = args?.getString(ARG_CURRENT_IMAGE)
        for ((index, value) in images.withIndex()) {
            if (currentImage != null && (value.url.containsUUID(currentImage)|| value.cropUrl.containsUUID(currentImage))) {
                startingPos = index
                currentImageIndex = index
            }
        }

        photographerPrefix = resources.getString(R.string.photographer)
        Timber.d("currentImage=$currentImage")
        //Setup ViewPager and Adapter
        pager = (findViewById<SlideshowViewPager>(R.id.pager)).apply {
            currentImage?.let {
                adapter = SlideshowPagerAdapter(supportFragmentManager, images, currentImage)
                currentItem = startingPos
            }
        }

        val moduleId = args?.getString(ARG_MODULE_ID)
        val moduleName = args?.getString(ARG_MODULE_NAME)
        val moduleTitle = args?.getString(ARG_MODULE_TITLE)
        val articleHeadline = args?.getString(ARG_ARTICLE_HEADLINE)
        val articleUuid = args?.getString(ARG_ARTICLE_UUID)

        OverlayThemeProvider.forModule(this, moduleId).theme.apply(rootView)

        //Nice animations when swiping between images
        pager?.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                StatisticsManager.getInstance().logEvent(StatisticsEvent.Builder()
                        .viewShow()
                        .moduleId(moduleId).moduleName(moduleName).moduleTitle(moduleTitle)
                        .attribute("articleHeadline", articleHeadline)
                        .attribute("articleUUID", articleUuid)
                        .attribute("imageUUID", Uri.parse(images[position].url).getQueryParameter("uuid"))
                        .viewName("imageViewer")
                        .build())
            }

            override fun onPageScrollStateChanged(state: Int) {
                when (state) {
                    androidx.viewpager.widget.ViewPager.SCROLL_STATE_IDLE -> {
                        pager?.let { pager ->
                            val (caption, photographers) = getImageData(pager.currentItem)
                            captionView?.text = caption
                            photographers?.let {
                                photographerView?.text = applyPrefixToPhotographers(photographers)
                                photographerView?.visibility = View.VISIBLE
                                photographerView?.animate()?.alpha(1.0f)
                            } ?: run {
                                photographerView?.visibility = View.GONE
                            }
                            captionView?.animate()?.alpha(1.0f)
                            updateImageCounter(pager.currentItem)
                            currentPageIndicatorTextView?.animate()?.alpha(1.0f)
                            totalPageIndicatorTextView?.animate()?.alpha(1.0f)
                        }
                    }
                    androidx.viewpager.widget.ViewPager.SCROLL_STATE_DRAGGING, androidx.viewpager.widget.ViewPager.SCROLL_STATE_SETTLING -> {
                        captionView?.animate()?.alpha(0.0f)
                        photographerView?.animate()?.alpha(0.0f)
                        currentPageIndicatorTextView?.animate()?.alpha(0.0f)
                        totalPageIndicatorTextView?.animate()?.alpha(0.0f)
                    }
                }
            }
        })

        //Set description text
        captionView?.setupStyling(args?.getString(ARG_CAPTION_FONT), args?.getDouble(ARG_CAPTION_FONT_SIZE))
        pager?.let { pager ->
            val (caption, photographers) = getImageData(pager.currentItem)
            captionView?.text = caption
            photographers?.let {
                photographerView?.text = applyPrefixToPhotographers(photographers)
                photographerView?.visibility = View.VISIBLE
            } ?: run {
                photographerView?.visibility = View.GONE
            }
            updateImageCounter(pager.currentItem)
        }

        if (savedInstanceState == null) {
            StatisticsManager.getInstance().logEvent(StatisticsEvent.Builder()
                    .viewShow()
                    .moduleId(moduleId).moduleName(moduleName).moduleTitle(moduleTitle)
                    .attribute("articleHeadline", articleHeadline)
                    .attribute("articleUUID", articleUuid)
                    .attribute("imageUUID", Uri.parse(currentImage).getQueryParameter("uuid"))
                    .viewName("imageViewer")
                    .build())
            captionView?.setUpTransitions()
        }
    }

    fun exitSlideShow(offset: Int) {
        if (hasRotated || currentImageIndex != startingPos) {
            return finish()
        }
        pager?.getCurrentFragment()?.run {
            onBeforeFinish(offset) {
                super.finishAfterTransition()
            }
        }
    }

    private fun applyPrefixToPhotographers(photographers: List<String?>?): String? {
        return if (!photographerPrefix.isNullOrEmpty())
            "$photographerPrefix ${photographers?.joinToString(", ")}"
        else
            photographers?.joinToString(", ")
    }

    private fun updateImageCounter(position: Int) {
        currentPageIndicatorTextView?.text = "${position + 1}"
        totalPageIndicatorTextView?.text = "/${images.size}"
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        hasRotated = true
        textVisible = savedInstanceState.getBoolean("textVisible", true)
        val previousPosition = savedInstanceState.getInt("currentImagePos", startingPos)
        val (caption, photographers) = getImageData(previousPosition)
        currentImageIndex = previousPosition
        captionView?.text = caption
        photographers?.let {
            photographerView?.text = applyPrefixToPhotographers(photographers)
            photographerView?.visibility = View.VISIBLE
        } ?: run {
            photographerView?.visibility = View.GONE
        }
        updateImageCounter(previousPosition)

        if (!textVisible) {
            hideSystemUI()
        }
        textBackground.performFade(textVisible, show = { showSystemUI() }, hide = { hideSystemUI() })
        pageIndicatorBackground.performFade(textVisible, show = { showSystemUI() }, hide = { hideSystemUI() })
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("textVisible", textVisible)

        pager?.let {
            outState.putInt("currentImagePos", it.currentItem)
        }
        super.onSaveInstanceState(outState)
    }

    fun TextView.setUpTransitions() {
        window.sharedElementExitTransition.duration = SHARED_TRANSITION_TIME
        window.sharedElementExitTransition.interpolator = AccelerateDecelerateInterpolator()
        window.sharedElementEnterTransition.duration = SHARED_TRANSITION_TIME
        window.sharedElementEnterTransition.interpolator = AccelerateDecelerateInterpolator()

        //Setup enter transitions
        OneShotPreDrawListener.add(this) {
            textBackground.apply {
                alpha = 0f
                translationY = height.toFloat()
            }
            window.sharedElementEnterTransition.addListener(object : TransitionListenerAdapter() {
                override fun onTransitionEnd(transition: Transition?) {
                    textBackground.animate()?.apply {
                        duration = TEXT_ANIMATE_TIME
                        alpha(1f)
                        translationY(0f)
                    }
                    pageIndicatorBackground.animate()?.apply {
                        duration = TEXT_ANIMATE_TIME
                        alpha(1f)
                        translationY(0f)
                    }
                }
            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun finishAfterTransition() {
        if (hasRotated || currentImageIndex != startingPos) {
            finish()
        } else {
            pager?.getCurrentFragment()?.onBeforeFinish(0) {
                super.finishAfterTransition()
            }
            if (textVisible) {
                val textBackground = textBackground
                textBackground.animate()?.apply {
                    duration = TEXT_ANIMATE_TIME
                    alpha(0f)
                    translationY(textBackground.height.toFloat())
                }
                pageIndicatorBackground.animate()?.apply {
                    duration = TEXT_ANIMATE_TIME
                    alpha(0f)
                    translationY(-pageIndicatorBackground.height.toFloat())
                }
            }
        }
    }

    fun getImageData(imagePos: Int): Pair<String?, List<String>?> {
        val description = images[imagePos].description?.trim()
        val photographers = if (images[imagePos].photographers.isNullOrEmpty()) null else images[imagePos].photographers
        return Pair(description, photographers)
    }

    /**
     * Setup styling for captionView with the selected font and font size
     */
    private fun TextView.setupStyling(captionFont: String?, captionFontSize: Double?) {
        //Font
        captionFont?.let {
            //Don't try to change the font if it does not exist in assets
            try {
                val typeFace = Typeface.createFromAsset(assets, it)
                this.typeface = typeFace
            } catch (ignore: Exception) {
                Timber.d("Could not load the font $it")
            }
        }

        //Font size
        if (captionFontSize != null && captionFontSize > 0f) {
            this.textSize = captionFontSize.toFloat()
        }
    }

    private fun hideSystemUI() {
        //Setup for full screen
        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        decorView.systemUiVisibility = uiOptions
    }

    private fun showSystemUI() {
        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        decorView.systemUiVisibility = uiOptions
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = (textBackground.background as ColorDrawable).color
    }

    /**
     * Called from ImageFragment when clicking on image, hides and shows the text (with background)
     * to get a more full screen feeling
     */
    fun onClick() {
        animateTextVisibility(!textVisible)
    }

    private fun animateTextVisibility(visible: Boolean, hideSystemUI: Boolean = true) {
        //Don't animate anything that is already in that state
        if (visible == textVisible) return
        textVisible = !textVisible

        listOf(Pair(textBackground, 1), Pair(pageIndicatorBackground, -1)).forEach { setup ->
            setup.first
                    .animate()
                    .withEndAction {
                        if (!textVisible && hideSystemUI) hideSystemUI()
                    }
                    .apply {
                        if (textVisible) {
                            alpha(1f)
                            translationY(0f)
                        } else {
                            alpha(0f)
                            translationY(setup.second * setup.first.height.toFloat())
                        }
                    }
        }
        if (textVisible) {
            showSystemUI()
        }
    }

    fun hideText() = animateTextVisibility(false)

    fun hideTextShowSystemUI() {
        animateTextVisibility(false, hideSystemUI = false)
        showSystemUI()
    }

    fun showText() = animateTextVisibility(true)
}

fun String?.containsUUID(uuid:String?):Boolean {
    if (uuid == null) return false
    return this?.contains(uuid) ?: false
}

private fun View.performFade(visibility: Boolean, show: () -> Unit, hide: () -> Unit) {
    OneShotPreDrawListener.add(this) {
        this.apply {
            if (visibility) {
                alpha = 1f
                translationY = 0f
                show.invoke()
            } else {
                alpha = 0f
                translationY = this.height.toFloat()
                hide.invoke()
            }
        }
    }
}