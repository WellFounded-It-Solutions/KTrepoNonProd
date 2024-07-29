package se.infomaker.datastore

import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import se.infomaker.livecontentui.livecontentdetailview.frequency.DatabaseCallback

class DatabaseManager {

    private val db: AppDatabase = DatabaseSingleton.getDatabaseInstance()
    private val garbage = CompositeDisposable()

    fun getFrequencies(databaseCallback: DatabaseCallback) {
        garbage.add(DatabaseSingleton.getDatabaseInstance().frequencyDao().getAll()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe { frequencies -> databaseCallback.onFrequencyLoaded(frequencies) })
    }

    fun insertFrequency(databaseCallback: DatabaseCallback, uuid: String, permission: String, property: String?) {
        Completable.fromAction {
            db.frequencyDao().insert(FrequencyRecord(uuid, permission, property))
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                    override fun onComplete() {
                        databaseCallback.onSuccess()
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onError(e: Throwable) {
                        databaseCallback.onDataNotAvailable()
                    }
                })
    }

    fun deleteFrequency(databaseCallback: DatabaseCallback, uuid: String) {

        Completable.fromAction { db.frequencyDao().delete(uuid) }
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onComplete() {
                        databaseCallback.onSuccess()
                    }

                    override fun onError(e: Throwable) {
                        databaseCallback.onDataNotAvailable()
                    }
                })
    }
}
