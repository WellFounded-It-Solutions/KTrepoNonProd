package se.infomaker.iap.articleview.extensions.ifragasatt.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IfragasattApiProvider @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val apis = mutableMapOf<String, IfragasattApi>()

    fun getApi(baseUrl: String): IfragasattApi {
        apis[baseUrl]?.let {
            return it
        }
        return build(baseUrl).also {
            apis[baseUrl] = it
        }
    }

    private fun build(baseUrl: String): IfragasattApi {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create()
    }
}