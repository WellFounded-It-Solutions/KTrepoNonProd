package se.infomaker.googleanalytics.dispatcher

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class UserAgentInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestWithUserAgent = originalRequest.newBuilder()
                .header("User-Agent", userAgent)
                .build()
        return chain.proceed(requestWithUserAgent)
    }

    companion object {
        private val userAgent by lazy { System.getProperty("http.agent") ?: "Dalvik/ (Linux; U; Android ;)" }
    }
}