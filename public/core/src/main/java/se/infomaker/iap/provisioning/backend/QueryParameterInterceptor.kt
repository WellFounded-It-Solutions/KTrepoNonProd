package se.infomaker.iap.provisioning.backend

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Add queryparameter to ALL requests
 */
class QueryParameterInterceptor(val parameter: String, val value: String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().url.newBuilder()
        builder.addQueryParameter(parameter, value)
        return chain.proceed(chain.request().newBuilder().url(builder.build()).build())
    }
}
