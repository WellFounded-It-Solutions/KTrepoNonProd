package se.infomaker.streamviewer.topicpicker

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.view.ThemeableImageView
import se.infomaker.iap.theme.view.ThemeableTextView
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.streamviewer.stream.SubscriptionUtil
import se.infomaker.streamviewer.topicpicker.TopicManager.isSelected
import se.infomaker.streamviewer.topicpicker.TopicManager.key
import java.io.Serializable

class TopicAdapter(val moduleId: String, val theme: Theme, val rootTopic: Topic, var flatten: Boolean, val headerLayout: Int, val topicLayout: Int) : RecyclerView.Adapter<TopicViewHolder>() {
    private var topics: MutableList<TopicWrapper> = mutableListOf()
    private var search: String? = null
    private var currentSubscriptions: Set<String>

    init {
        currentSubscriptions = SubscriptionUtil.matchSubscriptions()
        update()

        topics.forEach {

            if (currentSubscriptions.contains("${it.topic.property}:${it.topic.matching}")){
                TopicManager.selected[it.topic.key()] = it.topic
            }

        }

    }

    private fun update() {
        val isInitial = topics.isEmpty() && TextUtils.isEmpty(search)
        val old = topics.map { topicWrapper -> topicWrapper.topic }
        topics.clear()
        rootTopic.topics
            ?.filter { (it.matching != null && it.property != null) || !(it.topics?.isEmpty() ?: true) }
            ?.filter { it.hasSubtopicsMatchingSearch(search ?: "", true) }
            ?.forEach {
                topics.add(TopicWrapper(it, TopicType.ROOT_CATEGORY))
                add(it.topics)
            }
        if (!isInitial) {
            DiffUtil.calculateDiff(object: DiffUtil.Callback() {
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return old[oldItemPosition] == topics[newItemPosition].topic
                }

                override fun getOldListSize(): Int {
                    return old.size
                }

                override fun getNewListSize(): Int {
                    return topics.size
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return old[oldItemPosition] == topics[newItemPosition].topic
                }

            }, true).dispatchUpdatesTo(this)
        }
    }

    private fun Topic.matchesSearch(search: String): Boolean {
        return (TextUtils.isEmpty(search) || (title?.lowercase()?.contains(search.lowercase().trim()) == true))
    }

    private fun Topic.hasSubtopicsMatchingSearch(search: String, isRoot: Boolean): Boolean {
        if (!isRoot && matchesSearch(search)) {
            return true
        }
        topics?.forEach {
            if (it.hasSubtopicsMatchingSearch(search, false)) {
                return true
            }
        }
        return false
    }

    private fun add(list: List<Topic>?) {
        list?.forEach {
            if (it.matchesSearch(search ?: "")) {
                topics.add(TopicWrapper(it, TopicType.TOPIC))
            }
            if (flatten) {
                add(it.topics)
            }
        }
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        val topic = topics[position].topic
        holder.title.text = topic.title

        if (topic.matching != null && topic.property != null) {
            if (topics[position].type == TopicType.ROOT_CATEGORY && flatten) {
                holder.image?.visibility = View.GONE
                holder.following?.visibility = View.GONE

            }

            else if (currentSubscriptions.contains("${topic.property}:${topic.matching}")) {
                // TODO display following view

                holder.image?.visibility = View.VISIBLE
                holder.following?.visibility = View.GONE
                val themeKey = if (topics[position].type == TopicType.ROOT_CATEGORY) "topicHeadline" else "topic"
                holder.title.setThemeKey(themeKey)
                val selected = topic.isSelected()
                val colorKeys = if (selected) listOf("topicSelected", "brandColor") else listOf("topicUnselected")
                holder.image?.setThemeTintColor(colorKeys)
                holder.image?.setImageDrawable(getSelectDrawable(holder.view.context, theme, selected))
                holder.view.setOnClickListener {
                    TopicManager.toggleSelected(topic)
                    notifyDataSetChanged()
                }
            }

            else {
                holder.image?.visibility = View.VISIBLE
                holder.following?.visibility = View.GONE
                val themeKey = if (topics[position].type == TopicType.ROOT_CATEGORY) "topicHeadline" else "topic"
                holder.title.setThemeKey(themeKey)
                val selected = topic.isSelected()
                val colorKeys = if (selected) listOf("topicSelected", "brandColor") else listOf("topicUnselected")
                holder.image?.setThemeTintColor(colorKeys)
                holder.image?.setImageDrawable(getSelectDrawable(holder.view.context, theme, selected))
                holder.view.setOnClickListener {
                    TopicManager.toggleSelected(topic)
                    notifyDataSetChanged()
                }
            }
        } else {
            holder.view.setOnClickListener(null)
            holder.image?.visibility = View.GONE
        }
        if (topics[position].type == TopicType.ROOT_CATEGORY && topic.hasDepth && !flatten) {
//            holder.moreChoices?.visibility = View.VISIBLE
//            holder.moreChoices?.setOnClickListener {
//                val intent = Intent(holder.view.context, TopicPickerActivity::class.java)
//                intent.putExtra("topic", topic as Serializable)
//                intent.putExtra("moduleId", moduleId)
//                holder.view.context.startActivity(intent)
//            }
        } else {
          //  holder.moreChoices?.visibility = View.GONE
        }
        theme.apply(holder.view)
    }

    private fun getSelectDrawable(context: Context, theme: Theme, selected: Boolean): Drawable? {
        val resource = if (selected) "topicSelected" else "topicUnselected"
        val image = theme.getImage(resource, null)
        if (image != null) {
            return image.getImage(context).mutate()
        }
        return AppCompatResources.getDrawable(context, if (selected) R.drawable.heart else R.drawable.heart_outline)?.mutate()
    }

    override fun getItemCount(): Int {
        return topics.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return TopicViewHolder(view).also {
            if (viewType == headerLayout) {
                it.drawDivider = false
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (topics[position].type == TopicType.ROOT_CATEGORY) headerLayout else topicLayout
    }

    fun setSearch(search: String) {
        this.search = search
        update()
    }
}

private val Topic.hasDepth: Boolean
    get() {
        topics?.forEach { topic ->
            topic.topics?.let {
                if (it.isNotEmpty()) {
                    return true
                }
            }
        }
        return false
    }

class TopicViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    var drawDivider = true
    val title: ThemeableTextView = view.findViewById(R.id.title)
    val image: ThemeableImageView? = view.findViewById(R.id.image)
    val following: View? = view.findViewById(R.id.followingIndicator)
}
