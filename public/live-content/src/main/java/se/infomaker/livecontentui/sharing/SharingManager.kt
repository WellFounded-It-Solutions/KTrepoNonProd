package se.infomaker.livecontentui.sharing

import android.util.LruCache
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import se.infomaker.livecontentui.StatsHelper
import se.infomaker.livecontentui.di.SharingAllowedDomains
import se.infomaker.livecontentui.di.SharingBaseUrl
import javax.inject.Inject

class SharingManager @Inject constructor(
    @SharingBaseUrl private val baseUrl: String?,
    private val sharingService: SharingService,
    @SharingAllowedDomains private val allowedDomains: List<String>? = null
) {

    private val urlCache = LruCache<String, SharingResponse>(100)
    private val uuidCache = LruCache<String, SharingResponse>(100)

    fun getSharingUrl(uuid:String): Observable<SharingResponse> {
        urlCache.get(uuid)?.let {
            return Observable.just(it)
        }

        return sharingService.getUrl(baseUrl, uuid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    if (it.url.isNotEmpty()) {
                        urlCache.put(uuid, it)
                    }
                    if (it.uuid.isNotEmpty()) {
                        uuidCache.put(it.uuid, it)
                    }
                }
                .onErrorResumeNext(Observable.just(StatsHelper.NO_SHARING_RESPONSE))
                .share()
                .replay()
                .autoConnect(1)
    }

    fun canHandleUrl(url: String): Boolean = allowedDomains?.let {
        it.any { allowedDomain -> url.contains(allowedDomain) }
    } ?: true

    fun getArticleUuid(url: String): Observable<SharingResponse> {
        uuidCache.get(url)?.let {
            return Observable.just(it)
        }

        return sharingService.getUuid(baseUrl, url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    if (it.uuid.isNotEmpty()) {
                        urlCache.put(url, it)
                    }
                    if (it.url.isNotEmpty()) {
                        uuidCache.put(it.uuid, it)
                    }
                }
                .share()
                .replay()
                .autoConnect(1)
    }
}