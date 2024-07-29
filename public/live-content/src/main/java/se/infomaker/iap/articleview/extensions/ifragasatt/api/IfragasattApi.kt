package se.infomaker.iap.articleview.extensions.ifragasatt.api

import io.reactivex.Single
import retrofit2.Response

import retrofit2.http.Body
import retrofit2.http.POST

interface IfragasattApi {
    @POST("comment/count")
    fun commentCount(@Body request: CommentCountRequest): Single<Response<List<CommentCount>>>
}