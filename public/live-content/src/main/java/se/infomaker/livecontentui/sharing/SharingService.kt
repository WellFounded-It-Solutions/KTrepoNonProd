package se.infomaker.livecontentui.sharing

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface SharingService {
    @GET
    fun getUrl(@Url endpoint: String?, @Query("uuid") uuid: String): Observable<SharingResponse>

    @GET
    fun getUuid(@Url endpoint: String?, @Query("url") url: String): Observable<SharingResponse>
}

data class SharingResponse(val uuid: String, val url: String)