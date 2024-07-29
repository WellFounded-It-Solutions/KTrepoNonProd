package se.infomaker.flowplayer.library

import android.content.Context
import android.net.Uri
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.navigaglobal.mobile.livecontent.R

class FlowplayerView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr)
{
    var text:AppCompatTextView
        private set

    private var image: AppCompatImageView

    var isLive:Boolean = false
    var placeholder = R.drawable.placeholder
    var url:String? = null
    var start: String? = null
    var playerId:String? = null
    var autoplay:String? = null
    var mute:String? = null
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
    var description: String? = null
        set(value) {
            field = value
            this.text.text = field
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.flowplayer, this)
        text = findViewById(R.id.text)
        image = findViewById(R.id.image)

        setOnClickListener {
            url?.let { url ->
                var url = Uri.parse(url).buildUpon()

                if (!playerId.isNullOrEmpty()) {
                    url.appendQueryParameter("pi", playerId)
                }

                if (!autoplay.isNullOrEmpty()) {
                    url.appendQueryParameter("autoplay", autoplay)
                }

                if (!mute.isNullOrEmpty()) {
                    url.appendQueryParameter("mute", mute)
                }

                if (!start.isNullOrEmpty()) {
                    url.appendQueryParameter("start", start)
                }

                queryParameters?.forEach {
                    url.appendQueryParameter(it.key, it.value)
                }

                println("**********************************> url=${url.build()}")
                context.startActivity(FlowplayerActivity.createIntent(context, url.build().toString()))
            }
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