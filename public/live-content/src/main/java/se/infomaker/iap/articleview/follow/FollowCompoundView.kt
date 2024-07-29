package se.infomaker.iap.articleview.follow

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.view.Themeable
import se.infomaker.iap.theme.view.ThemeableImageView
import se.infomaker.iap.theme.view.ThemeableTextView
import com.navigaglobal.mobile.livecontent.R

class FollowCompoundView : LinearLayout, Themeable {
    private var canFollow: Boolean = true
        set(value) {
            if (field == value) {
                return
            }
            field = value
            visibility = if (field) View.VISIBLE else View.INVISIBLE
        }

    private val garbage = CompositeDisposable()
    var listener: ((isActive: Boolean) -> Unit)? = null

    constructor (context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initializeView(context)
    }

    var following = false
        set(value) {
            field = value
            invalidate()
        }

    private var imageView: ThemeableImageView? = null
    private var textView: ThemeableTextView? = null

    var theme: Theme? = null
        set(value) {
            field = value
            invalidate()
        }

    private var resourceManager: ResourceManager? = null

    private fun initializeView(context: Context) {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.follow_compound_view, this)
        theme?.let {
            apply(it)
        }
        // TODO Module awareness
        resourceManager = ResourceManager(context, null)
    }

    override fun apply(theme: Theme) {
        this.theme = theme
        textView?.apply(theme)
        imageView?.apply(theme)
        invalidate()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setOnClickListener {
            if (canFollow && !following) {
                following = !following
                listener?.invoke(following)
            }
        }
        imageView = findViewById(R.id.followImage)
        textView = findViewById(R.id.followText)

        invalidate()
    }

    override fun invalidate() {
        val image = theme?.getImage((if (following) "following" else "follow"), null)?.getImage(context)
        if (image != null) {
            imageView?.visibility = View.VISIBLE
            imageView?.setImageDrawable(image)
        }
        else {
            imageView?.visibility = View.GONE
        }
        textView?.apply {
            val resourceKey = if (following) "following" else "follow"
            text = resourceManager?.getString(resourceKey, null)
            setThemeKey(if (following) "followButtonActive" else "followButtonInactive")
            theme?.let { theme ->
                apply(theme)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ModuleLock.followLock(context)?.let { lock ->
            if (!lock.isAlwaysOpen()) {
                garbage.add(lock.isOpen()
                        .subscribeOn(AndroidSchedulers.mainThread()).startWith(false).subscribe { isOpen ->
                            canFollow = isOpen
                        })
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        garbage.clear()
    }
}
