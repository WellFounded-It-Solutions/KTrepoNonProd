package se.infomaker.datastore

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

object ArticleLastViewMemoryCache {

    private val garbage = CompositeDisposable()
    private val relay = BehaviorRelay.createDefault(listOf<Article>())

    init {
        garbage.add(DatabaseSingleton.getDatabaseInstance().userLastViewDao().subscribeAll()
                .subscribeOn(Schedulers.io())
                .subscribe { articles ->
                    relay.accept(articles)
                })
    }

    fun observe(): Observable<List<Article>> = relay

    fun get() = relay.value ?: emptyList()
}