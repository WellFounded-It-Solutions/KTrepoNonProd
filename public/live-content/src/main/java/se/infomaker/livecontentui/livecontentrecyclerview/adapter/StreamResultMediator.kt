package se.infomaker.livecontentui.livecontentrecyclerview.adapter

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import se.infomaker.livecontentmanager.parser.PropertyObject
import se.infomaker.livecontentui.extensions.isRelated
import se.infomaker.livecontentui.extensions.optRelatedArticles
import kotlin.math.min

class StreamResultMediator(value: List<PropertyObject>) {

    private val index = mutableListOf<String>()
    private val articles = mutableMapOf<String, PropertyObject>()
    private val relatedArticles = mutableMapOf<String, List<PropertyObject>?>()
    private val merged = BehaviorRelay.create<List<PropertyObject>>()

    private val articleAmount: Int
        get() {
            var total = articles.size
            relatedArticles.forEach { entry ->
                entry.value?.let {
                    total += it.size
                }
            }
            return total
        }

    private val current: List<PropertyObject>
        get() = mutableListOf<PropertyObject>().apply {
            index.forEach { uuid ->
                articles[uuid]?.let { add(it) }
                relatedArticles[uuid]?.let { addAll(it) }
            }
        }.toList()

    init {
        add(value, 0)
    }

    fun add(list: List<PropertyObject>, location: Int): Pair<Int, List<PropertyObject>> {
        val out = mutableListOf<PropertyObject>()
        val actualLocation = positionWithRelatedOffset(location)
        var indexLocation = min(location, index.size)
        list.forEach { article ->

            if (indexLocation >= 0 && indexLocation <= index.size) {
                index.add(indexLocation++, article.id)
            }

            out.add(article)

            articles[article.id] = article

            val relatedArticles = article.optRelatedArticles() ?: emptyList()
            relatedArticles.forEach { it.isRelated = true }
            this.relatedArticles[article.id] = relatedArticles
            out.addAll(relatedArticles)
        }
        emit()
        return Pair(actualLocation, out)
    }

    private fun positionWithRelatedOffset(position: Int) = when {
        position == 0 -> {
            0
        }
        position >= index.size -> {
            articleAmount
        }
        else -> {
            val passedRelatedArticles = index.subList(0, position).map {
                relatedArticles[it]?.size ?: 0
            }.sum()
            position + passedRelatedArticles
        }
    }

    fun remove(list: List<PropertyObject>): List<PropertyObject> {
        val out = mutableListOf<PropertyObject>()
        list.forEach {
            index.remove(it.id)
            articles.remove(it.id)?.let { removed -> out.add(removed) }
            relatedArticles.remove(it.id)?.let { removed -> out.addAll(removed) }
        }
        emit()
        return out
    }

    fun change(list: List<PropertyObject>) {
        list.forEach {
            val current = index.indexOf(it.id)
            if (current != -1) {
                articles[it.id] = it
                it.optRelatedArticles()?.apply {
                    forEach { related -> related.isRelated = true }
                }?.also { relatedArticles ->
                    this.relatedArticles[it.id] = relatedArticles
                }
            }
        }
        emit()
    }

    fun reset() {
        index.clear()
        articles.clear()
        relatedArticles.clear()
        emit()
    }

    private fun emit() {
        merged.accept(current)
    }

    fun observe(): Observable<List<PropertyObject>> = merged
}