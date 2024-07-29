package se.infomaker.frt.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.jknack.handlebars.context.MapValueResolver
import com.navigaglobal.mobile.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import se.infomaker.frt.moduleinterface.BaseModule
import se.infomaker.frt.statistics.StatisticsEvent
import se.infomaker.frt.statistics.StatisticsManager
import se.infomaker.frt.ui.adapter.CentrePointAdapter
import se.infomaker.frt.ui.adapter.CentrePointDTO
import se.infomaker.frtutilities.TemplateManager
import se.infomaker.frtutilities.connectivity.hasInternetConnection
import se.infomaker.frtutilities.ktx.resources
import timber.log.Timber
import java.io.IOException

class CentrePointFragment :BaseModule() {
    private lateinit var offlineErrorContainer: ViewGroup
    private lateinit  var mConfig: CentrePointConfig
    private lateinit var mContext: Context
    private lateinit var mProgressBar: ProgressBar
    private  var mFragmentActivity: FragmentActivity?=null
    var centrePointAdapter: CentrePointAdapter = CentrePointAdapter()
    private lateinit var refreshLayout: SwipeRefreshLayout
    private var isDisplayingError = false
    private val resources by resources()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mConfig = getModuleConfig<CentrePointConfig>(CentrePointConfig::class.java)

        if (savedInstanceState == null) {
            StatisticsManager.getInstance().logEvent(
                StatisticsEvent.Builder()
                    .viewShow()
                    .moduleId(moduleIdentifier.toString())
                    .moduleName(moduleName)
                    .moduleTitle(moduleTitle)
                    .viewName("centrePoint")
                    .attribute("uri", getUrl())
                    .build()
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mFragmentActivity = super.getActivity()
        val fragmentView: View = inflater.inflate(R.layout.fragment_centrepoint, container, false)
        val recyclerview=fragmentView.findViewById<RecyclerView>(R.id.recyclerview)
        refreshLayout = fragmentView.findViewById<SwipeRefreshLayout>(R.id.swipeContainer)
        mProgressBar = fragmentView.findViewById<ProgressBar>(R.id.progressDialog)
        mProgressBar.setVisibility(View.VISIBLE)
        offlineErrorContainer=fragmentView.findViewById<ViewGroup>(R.id.offline_error_container)
        refreshLayout.isEnabled = mConfig.refreshEnabled

        refreshLayout.setOnRefreshListener(
            SwipeRefreshLayout.OnRefreshListener {
                if (isDisplayingError) {
                    isDisplayingError = false
                    apiCall()
                } else {
                   apiCall()
                }
            }
        )


        recyclerview.adapter = centrePointAdapter
        centrePointAdapter.setContext(mContext)
        apiCall()
        return fragmentView
    }

    private fun apiCall() {
        try{
        val quotesApi = RetrofitHelper.getInstance(getUrl()).create(CentrePointService::class.java)
         GlobalScope.launch {
             val result = quotesApi.getCentrePointList()
             if (!result.isNullOrEmpty()) {
                 GlobalScope.launch(Dispatchers.Main) {
                     centrePointAdapter.setList(quotesApi.getCentrePointList())
                     mProgressBar.visibility = View.GONE
                     refreshLayout.isRefreshing = false
                 }
             } else {
                 isDisplayingError = true

             }
         }
        }catch (e:Exception){
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        requireActivity().registerReceiver(networkStateReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(networkStateReceiver)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
            mContext=context

    }

    private val networkStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val noConnectivity =
                intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)
            if (!noConnectivity && isDisplayingError) {
                 if (context?.hasInternetConnection() == true) {
                    offlineErrorContainer.visibility=View.VISIBLE
                }
            }
        }
    }


    override fun shouldDisplayToolbar(): Boolean {
        //TODO("Not yet implemented")
        return false
    }

    override fun onBackPressed(): Boolean {
        //TODO("Not yet implemented")
        return false
    }

    override fun onAppBarPressed() {
       // TODO("Not yet implemented")

    }
    private fun getErrorHtml(data: Map<String, Any>): String? {
        if (activity != null) {
            val template = TemplateManager.getManager(
                activity, moduleIdentifier
            ).getTemplate(mConfig.errorTemplate, TemplateManager.DEFAULT_ERROR_TEMPLATE)
            val templateContext = com.github.jknack.handlebars.Context.newBuilder(data)
                .resolver(MapValueResolver.INSTANCE)
                .build()
            if (template != null) {
                try {
                    return template.apply(templateContext)
                } catch (e: IOException) {
                    Timber.e(e, "Failed to process template")
                }
            } else {
                Timber.e("Failed to load template")
            }
        }
        return ""
    }

    private fun getUrl(): String? {
        var url: String? = null
        if (arguments != null) {
            url = arguments!!.getString("centrePointUrl")
        }
        return if (TextUtils.isEmpty(url)) {
            mConfig.centrePointUrl
        } else url
    }


}

object RetrofitHelper {
    fun getInstance(url:String?): Retrofit {
        return Retrofit.Builder().baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            // we need to add converter factory to
            // convert JSON object to Java object
            .build()
    }
}

interface CentrePointService {

    @GET("app_category_url")
    suspend fun getCentrePointList(): List<CentrePointDTO>
}

class CentrePointConfig {
    var centrePointUrl: String? = null
    val refreshEnabled = true
    val errorTemplate: String? = null
}