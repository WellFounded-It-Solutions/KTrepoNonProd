package se.infomaker.iap.provisioning

import android.net.Uri
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Query


class ConnectIdBrowserReplacement(private val headers: Map<String,String>) {

    val baseUrl = "https://connectid.no"
    val clientId = "no.medierogledelse.app"
    val responseType = "code"
    val service: ConnectIdService

    init {
        val builder = OkHttpClient().newBuilder()
        builder.followRedirects(false)
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.HEADERS
        builder.addInterceptor(logging).build()


        val retrofit = Retrofit.Builder()
                .client(builder.build())
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()

        service = retrofit.create(ConnectIdService::class.java)

    }
    fun getAuthCode(redirectUri: String): Single<String> {
        return service.requestAuthCode(clientId, responseType, redirectUri, headers).map {
            val location = it.headers()["Location"]
            return@map Uri.parse(location).getQueryParameter("code")
        }
    }
}

interface ConnectIdService {

    @GET("/user/oauth/authorize?client_id=no.medierogledelse.app&response_type=code&redirect_uri=test")
    fun requestAuthCode(@Query("client_id") clientId: String, @Query("response_type") responseType: String, @Query("redirect_uri") redirectUri: String, @HeaderMap headers: Map<String,String>): Single<Response<String>>
}