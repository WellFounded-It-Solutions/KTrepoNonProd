package se.infomaker.iap.articleview.item.youplay

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.iap.articleview.item.embed.FullscreenEmbedActivity

class YouplayView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr)
{
    var text: AppCompatTextView
        private set

    private var image: AppCompatImageView
    var baseUrl:String? = null
    var isLive:Boolean = false
    var placeholder = R.drawable.placeholder
    var url:String? = null
    var start: String? = null
    var autoplay:String? = null
    var embedCode:String? = null
    var queryParameters:MutableMap<String, String>? = null
    var thumbnailUrl:String? = null
        set(value) {
            field = value
            updateThumbnail()
        }
    var duration:String? = null
        set(value) {
            field = value
            this.text.text = field
        }
    var moduleId: String? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.youplay, this)
        text = findViewById(R.id.text)
        image = findViewById(R.id.image)

        setOnClickListener {
            context.startActivity(FullscreenEmbedActivity.createIntent(context, baseUrl ?: "", embedCode ?: "", moduleId = moduleId))
        }
    }

    private fun updateThumbnail()
    {
        if (thumbnailUrl == null) {
            image.setImageResource(placeholder)
        }

        val options = RequestOptions()
        options.placeholder(placeholder)
        options.error(placeholder)
        Glide.with(context).load(thumbnailUrl).apply(options).into(image)
    }
}