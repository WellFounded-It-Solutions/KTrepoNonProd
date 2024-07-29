package se.infomaker.iap.articleview.select

import android.text.SpannableStringBuilder
import com.google.gson.Gson
import java.util.*

fun mapOf(build: MapBuilder.() -> Unit): Map<String, Any> = MapBuilder().map(build)
class MapBuilder {
    private val deque: Deque<MutableMap<String, Any>> = ArrayDeque()

    fun map(build: MapBuilder.() -> Unit): Map<String, Any> {
        deque.push(mutableMapOf())
        this.build()
        return deque.pop()
    }

    infix fun String.to(value: Any) {
        deque.peek().put(this, value)
    }
}

fun Any.jsonFormat(): String = Gson().toJson(this)

class TestingSpannableStringBuilder(val text: String) : SpannableStringBuilder() {
    override fun toString(): String = text
}