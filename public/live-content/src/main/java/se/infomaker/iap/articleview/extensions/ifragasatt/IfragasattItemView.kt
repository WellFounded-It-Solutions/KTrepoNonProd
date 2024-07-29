package se.infomaker.iap.articleview.extensions.ifragasatt

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.constraintlayout.widget.ConstraintLayout
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import se.infomaker.frtutilities.ktx.findActivity
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.size.ThemeSize
import se.infomaker.iap.theme.view.Themeable
import se.infomaker.iap.theme.view.ThemeableTextView

class IfragasattItemView(context: Context): FrameLayout(context), Themeable {
    val numberComments: ThemeableTextView
    val addComment: ThemeableTextView
    val container: ConstraintLayout
    val border: ConstraintLayout

    init {
        LayoutInflater.from(context).inflate(R.layout.ifragasatt_item, this)
        border = findViewById(R.id.ifragasattBorder)
        container = findViewById(R.id.container)
        numberComments = findViewById(R.id.numberComments)
        addComment = findViewById(R.id.addComment)
    }

    fun bind(item: IfragasattItem) {
        val count = item.commentCount ?: 0
        if (count == 0) {
            numberComments.text = context.resources.getString(R.string.no_comments)
            addComment.text = context.resources.getString(R.string.first_to_comment)
        } else {
            numberComments.text = context.resources.getQuantityString(R.plurals.number_of_comments, count, count)
            addComment.text = context.resources.getQuantityString(R.plurals.add_comment, count, count)
        }
        setOnClickListener { view ->
            (view.findActivity())?.let { activity ->
                val intent = Intent(activity, IfragasattDetailActivity::class.java)
                intent.putExtra("articleId", item.id)
                intent.putExtra("commentUrl", item.commentUrl)
                activity.startActivity(intent)
            }
        }
    }

    override fun apply(theme: Theme) {
        theme.apply(border)
        val marginVertical = theme.getSize("ifragasattMarginVertical", ThemeSize.DEFAULT)
        container.layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            marginVertical.sizePx.toInt().let { margin ->
                topMargin = margin
                bottomMargin = margin
            }

        }
        val paddingVertical = theme.getSize("ifragasattPaddingVertical", ThemeSize.DEFAULT)
        container.apply {
            paddingVertical.sizePx.toInt().let { padding ->
                setPadding(paddingLeft, padding, paddingRight, padding)
            }
        }

        theme.getColor("background", null)?.let {
            container.setBackgroundColor(it.get())
        }

        theme.getColor("articleBackground", null)?.let {
            border.setBackgroundColor(it.get())
        }
    }
}