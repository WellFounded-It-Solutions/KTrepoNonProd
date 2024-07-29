package se.infomaker.coremedia.slideshow

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.RectEvaluator
import android.annotation.SuppressLint
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.transition.Transition
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.core.app.ActivityCompat
import androidx.core.view.OneShotPreDrawListener
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.navigaglobal.mobile.R
import io.reactivex.disposables.CompositeDisposable
import se.infomaker.coremedia.slideshow.imagezoom.ImageViewTouch
import se.infomaker.coremedia.slideshow.imagezoom.ImageViewTouchBase
import timber.log.Timber
import kotlin.math.roundToInt

class ImageFragment : Fragment() {

    private var description: String? = null
    private var url: String? = null
    private var placeholder: PlaceholderImage? = null
    private var imageView: ImageViewTouch? = null
    private var slideshowBackground: View? = null
    private var placeholderImageView: ImageView? = null
    private var placeholderWrapper: View? = null
    private var imageSize: Point? = null
    private var isStartImage: Boolean? = null
    private val activity: SlideshowActivity? by lazy { getActivity() as? SlideshowActivity }
    private var disposable = CompositeDisposable()

    override fun onDestroyView() {
        super.onDestroyView()
        disposable.clear()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            url = it.getString(ARG_URL)
            placeholder = it.getParcelable(ARG_PLACEHOLDER_IMAGE)
            description = it.getString(ARG_DESCRIPTION)
            imageSize = it.getParcelable(ARG_IMAGE_SIZE)
            isStartImage = it.getBoolean(ARG_IS_START_IMAGE)
        }
    }

    fun isZoomedOut(): Boolean = imageView?.let { it.scale <= 1 } ?: true

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val fragmentView = inflater.inflate(R.layout.fragment_image, container, false)

        imageView = fragmentView.findViewById(R.id.testImageView)
        imageView?.displayType = ImageViewTouchBase.DisplayType.FIT_TO_SCREEN
        slideshowBackground = fragmentView.findViewById(R.id.slideshowBackground)
        placeholderImageView = fragmentView.findViewById(R.id.placeholderImage)
        placeholderWrapper = fragmentView.findViewById(R.id.placeholderWrapper)
        val progressBar = fragmentView.findViewById<ProgressBar>(R.id.progressBar)

        val gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                imageView?.performClick()
                activity?.onClick()
                return super.onSingleTapConfirmed(e)
            }
        })

        imageView?.alphaRelay?.run {
            disposable.add(subscribe { alpha ->
                slideshowBackground?.alpha = alpha
                if (alpha < 0.97f) {
                    activity?.hideTextShowSystemUI()
                } else {
                    activity?.showText()
                }
            })
        }

        imageView?.exitRelay?.run {
            disposable.add(subscribe { y ->
                val originalY = placeholderWrapper?.y
                placeholderWrapper?.y = -y.toFloat() + (originalY ?: 0f)
                imageView?.y = -y.toFloat()
                activity?.exitSlideShow(-y)
            })
        }
        //Setting if we are zoomed in on image or not
        imageView?.setOnTouchListener { _, motionEvent ->
            gestureDetector.onTouchEvent(motionEvent)
        }

        if (savedInstanceState == null) {
            imageView?.addScaleListener { scale ->
                if (scale > 1.0f) {
                    activity?.hideText()
                } else {
                    if (slideshowBackground?.alpha ?: 1f > 0.97f)
                        activity?.showText()
                }
            }

            //Load image async
            if (hasPlaceholder()) {
                placeholderImageView?.loadImageIntoView(placeholder?.url) { placeholderSuccess ->
                    scalePlaceholder(fragmentView, placeholder, imageSize) {
                        progressBar.visibility = View.GONE

                        if (isStartImage == true) {
                            activity?.let {
                                ActivityCompat.startPostponedEnterTransition(it)
                            }
                        }
                        imageView?.visibility = View.INVISIBLE
                        activity?.window?.sharedElementEnterTransition?.addListener(object : TransitionListenerAdapter() {
                            override fun onTransitionEnd(transition: Transition?) {
                                hidePlaceholderImageViewSecondTime(fragmentView)
                            }
                        })
                    }

                    imageView?.loadImageIntoView(url, executeWhenReady = {
                        hidePlaceholderImageViewSecondTime(fragmentView)
                    }, executeOnLoadFailed = {
                        if (placeholderSuccess) {
                            placeholderImageView?.drawable?.let {
                                imageView?.setImageDrawable(it)
                            }
                        }
                        hidePlaceholderImageViewSecondTime(fragmentView)
                    })
                }
            } else {
                placeholderWrapper?.visibility = View.GONE
                val executeWhenDone = {
                    if (isStartImage == true) {
                        activity?.let {
                            ActivityCompat.startPostponedEnterTransition(it)
                            progressBar.visibility = View.GONE
                        }
                    }
                }
                imageView?.loadImageIntoView(url, executeWhenDone, executeWhenDone)
            }
        } else {
            OneShotPreDrawListener.add(fragmentView) {
                placeholderImageView?.loadImageIntoView(placeholder?.url) {
                    scalePlaceholder(fragmentView, placeholder, imageSize)
                    placeholderWrapper?.visibility = View.GONE
                }
            }
            progressBar.visibility = View.GONE
            imageView?.loadImageIntoView(url)
        }
        progressBar.visibility = View.GONE
        return fragmentView
    }

    fun onBeforeFinish(offset: Int, runWhenComplete: (() -> Unit)? = null) {
        val imageView = imageView
        if (hasPlaceholder() && imageView != null) {
            imageView.zoomTo(1f, 50L)
            placeholderWrapper?.visibility = View.VISIBLE
            val from = Rect(0, 0, imageView.width, imageView.height)
            val to = placeholderImageView?.let { placeholderImageView ->
                val location = IntArray(2)
                placeholderImageView.getLocationInWindow(location)
                Rect(location[0],
                        (location[1] - offset),
                        location[0] + placeholderImageView.width,
                        location[1] + placeholderImageView.height - offset)
            } ?: kotlin.run {
                imageView.visibility = View.GONE
                runWhenComplete?.invoke()
                return
            }

            imageView.rectReveal(from, to, 50L) {
                imageView.visibility = View.GONE
                runWhenComplete?.invoke()
            }
        } else {
            imageView?.zoomTo(1f, 50L)
            runWhenComplete?.invoke()
        }
    }

    private fun View.rectReveal(fromRect: Rect, toRect: Rect, duration: Long = REVEAL_TIME, runWhenComplete: (() -> Unit)? = null) {
        ObjectAnimator.ofObject(this, "clipBounds", RectEvaluator(), fromRect, toRect).apply {
            this.duration = duration
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    runWhenComplete?.invoke()
                }
            })
        }.start()
    }

    private var hideCount = 0

    /**
     * This method hides placeholder image the second time this method is called
     */
    private fun hidePlaceholderImageViewSecondTime(fragmentView: View) {
        hideCount++
        if (hideCount == 2) {
            val to = Rect(0, 0, fragmentView.width, fragmentView.height)

            val from = placeholderImageView?.let { placeholderImageView ->
                val location = IntArray(2)
                placeholderImageView.getLocationInWindow(location)
                Rect(location[0],
                        location[1],
                        location[0] + placeholderImageView.width,
                        location[1] + placeholderImageView.height)
            } ?: Rect()

            imageView?.let { imageView ->
                imageView.visibility = View.VISIBLE
                imageView.rectReveal(from, to) {
                    Timber.d("Placeholder is now $placeholderImageView")
                }
                placeholderWrapper?.visibility = View.GONE
            }
        }
    }

    private fun scalePlaceholder(frame: View?, placeholder: PlaceholderImage?, originalImageSize: Point?, runWhenComplete: (() -> Unit)? = null) {
        val placeholderWrapper = placeholderWrapper

        if (placeholderWrapper == null || placeholder == null || originalImageSize == null || frame == null || frame.width <= 0 || frame.height <= 0) {
            return
        }
        val wrapperRatio = frame.width / frame.height.toFloat()
        val imageRatio = originalImageSize.x / originalImageSize.y.toFloat()

        val wrapperLParams = placeholderWrapper.layoutParams as FrameLayout.LayoutParams
        val ratio = if (imageRatio >= wrapperRatio) {
            //The image width will match parent
            frame.width / originalImageSize.x.toFloat()
        } else {
            //The image height will match parent
            frame.height / originalImageSize.y.toFloat()
        }

        val scaledHeight = (originalImageSize.y * ratio).roundToInt()
        val scaledWidth = (originalImageSize.x * ratio).roundToInt()
        wrapperLParams.height = scaledHeight
        wrapperLParams.width = scaledWidth
        wrapperLParams.gravity = Gravity.CENTER
        placeholderWrapper.layoutParams = wrapperLParams

        //X position of guides
        val guideXStart = placeholderWrapper.findViewById<Guideline>(R.id.placeholderXStart)
        val guideXEnd = placeholderWrapper.findViewById<Guideline>(R.id.placeholderXEnd)
        val paramsXStart = guideXStart.layoutParams as ConstraintLayout.LayoutParams
        val paramsXEnd = guideXEnd.layoutParams as ConstraintLayout.LayoutParams
        paramsXStart.guidePercent = placeholder.crop?.left ?: 0f
        paramsXEnd.guidePercent = placeholder.crop?.right ?: 1f
        guideXStart.layoutParams = paramsXStart
        guideXEnd.layoutParams = paramsXEnd

        //Y position of guides
        val guideYStart = placeholderWrapper.findViewById<Guideline>(R.id.placeholderYStart)
        val guideYEnd = placeholderWrapper.findViewById<Guideline>(R.id.placeholderYEnd)
        val paramsYStart = guideYStart.layoutParams as ConstraintLayout.LayoutParams
        val paramsYEnd = guideYEnd.layoutParams as ConstraintLayout.LayoutParams
        paramsYStart.guidePercent = placeholder.crop?.top ?: 0f
        paramsYEnd.guidePercent = placeholder.crop?.bottom ?: 1f
        guideYStart.layoutParams = paramsYStart
        guideYEnd.layoutParams = paramsYEnd

        placeholderImageView?.let {
            OneShotPreDrawListener.add(it) {
                runWhenComplete?.invoke()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (url != null) {
            if (hasPlaceholder()) {
                placeholderImageView?.transitionName = url
            } else {
                imageView?.transitionName = url
            }
        }
    }

    private fun hasPlaceholder(): Boolean {
        return placeholder != null
    }

    companion object {
        const val ARG_DESCRIPTION = "image"
        const val ARG_URL = "url"
        const val ARG_PLACEHOLDER_IMAGE = "placeholderImage"
        const val ARG_IMAGE_SIZE = "imageSize"
        private const val REVEAL_TIME = 200L

        private const val ARG_IS_START_IMAGE = "startImage"

        fun newInstance(imageObject: ImageObject, isStartImage: Boolean): ImageFragment {
            return ImageFragment().apply {
                arguments = Bundle().apply {
                    this.putString(ARG_DESCRIPTION, imageObject.description)
                    this.putString(ARG_URL, imageObject.cropUrl ?: imageObject.url)
                    this.putParcelable(ARG_PLACEHOLDER_IMAGE, imageObject.placeholderImage)
                    this.putParcelable(ARG_IMAGE_SIZE, imageObject.size)
                    this.putBoolean(ARG_IS_START_IMAGE, isStartImage)
                }
            }
        }
    }
}

private fun ImageView.loadImageIntoView(url: String?, executeWhenDone: ((success: Boolean) -> Unit)? = null) {
    Glide.with(this.context).load(url).error(R.drawable.error_placeholder).listener(object : RequestListener<Drawable> {
        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
            executeWhenDone?.invoke(false)
            return false
        }

        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
            executeWhenDone?.invoke(true)
            return false
        }
    }).into(this)
}

private fun ImageViewTouch.loadImageIntoView(url: String?, executeWhenReady: (() -> Unit)? = null, executeOnLoadFailed: (() -> Unit)? = null) {
    val builder = Glide.with(this.context).load(url).error(R.drawable.error_placeholder)
    builder.into(object : SimpleTarget<Drawable>() {
        override fun onLoadFailed(errorDrawable: Drawable?) {
            setImageDrawable(errorDrawable)
            executeOnLoadFailed?.invoke()
        }

        override fun onResourceReady(resource: Drawable, transition: com.bumptech.glide.request.transition.Transition<in Drawable>?) {
            setImageDrawable(resource, null, 1.0F, 4.0F)
            executeWhenReady?.invoke()
        }
    })
}
