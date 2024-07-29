package se.infomaker.frt.ui.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.MailTo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.navigaglobal.mobile.R;

import java.io.IOException;
import java.util.Map;

import se.infomaker.frt.moduleinterface.BaseModule;
import se.infomaker.frt.statistics.StatisticsEvent;
import se.infomaker.frt.statistics.StatisticsManager;
import se.infomaker.frt.ui.adapter.WebViewAdapter;
import se.infomaker.frt.util.WebViewCallback;
import se.infomaker.frtutilities.ErrorUtil;
import se.infomaker.frtutilities.TemplateManager;
import se.infomaker.frtutilities.ktx.ContextUtils;
import timber.log.Timber;

public class WebContentJSFragment extends BaseModule {
    public static final String URL_KEY = "url";
    private WebView mWebView;
    private FragmentActivity mFragmentActivity;
    private WebContentConfig mConfig;
    private SwipeRefreshLayout refreshLayout;
    private boolean isDisplayingError;
                                   WebViewAdapter webViewAdapter;
    WebViewClient mWebViewClient = new WebViewClient() {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
         //   Log.d("onPageFinished","onPageFinished "+url);
           // Toast.makeText(mContext, "onPageFinished"+url, Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            mProgressBar.setVisibility(View.GONE);

        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                handleError(view, errorCode, description, failingUrl);
            }
        }

        private void handleError(WebView view, int errorCode, String description, String failingUrl) {
            if (failingUrl != null && failingUrl.equals(mWebView.getUrl())) {
                refreshLayout.setRefreshing(false);
                Timber.d("Failed to load %s %s %d", failingUrl, description, errorCode);
                Map<String, Object> data = ErrorUtil.localizedErrorInfo(view.getContext(), getResourceManager(), errorCode);
                data.put(URL_KEY, failingUrl);
                view.loadDataWithBaseURL(null, getErrorHtml(data), "text/html", "UTF-8", null);
                isDisplayingError = true;
            }else {
                Timber.w("Failed to load resource " + failingUrl + " while displaying " + view.getUrl());
            }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                handleError(view, error.getErrorCode(), error.getDescription().toString(), request.getUrl().toString());
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("mailto:")) {
                if (mFragmentActivity != null) {
                    MailTo mt = MailTo.parse(url);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mt.getTo()});
                    intent.putExtra(Intent.EXTRA_TEXT, mt.getBody());
                    intent.putExtra(Intent.EXTRA_SUBJECT, mt.getSubject());
                    intent.putExtra(Intent.EXTRA_CC, mt.getCc());
                    intent.setType("message/rfc822");
                    mFragmentActivity.startActivity(intent);
                    view.reload();
                    return true;
                }
            }
            return false;
        }
    };
    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

            if(!noConnectivity && isDisplayingError) {
                mWebView.loadUrl(getUrl());
            }
        }
    };
    private Context mContext;
    private ProgressBar mProgressBar;


    private String getErrorHtml(Map<String, Object> data) {
        if(getActivity() != null) {
            Template template = TemplateManager.getManager(getActivity(), getModuleIdentifier()).getTemplate(mConfig.getErrorTemplate(), TemplateManager.DEFAULT_ERROR_TEMPLATE);

            com.github.jknack.handlebars.Context templateContext =
                    com.github.jknack.handlebars.Context.newBuilder(data)
                            .resolver(MapValueResolver.INSTANCE)
                            .build();
            if (template != null) {
                try {
                    return template.apply(templateContext);
                } catch (IOException e) {
                    Timber.e(e, "Failed to process template");
                }
            } else {
                Timber.e("Failed to load template");
            }
        }
        return "";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConfig = getModuleConfig(WebContentConfig.class);
        if (savedInstanceState == null) {
            StatisticsManager.getInstance().logEvent(new StatisticsEvent.Builder()
                    .viewShow()
                    .moduleId(String.valueOf(getModuleIdentifier()))
                    .moduleName(getModuleName())
                    .moduleTitle(getModuleTitle())
                    .viewName("webContent")
                    .attribute("uri", getUrl())
                    .build());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFragmentActivity = super.getActivity();
        View fragmentView = inflater.inflate(R.layout.fragment_webviewjs, container, false);
           mProgressBar=fragmentView.findViewById(R.id.pgrWebView);
        mProgressBar.setVisibility(View.VISIBLE);
        refreshLayout = fragmentView.findViewById(R.id.swipeContainer);
        refreshLayout.setEnabled(mConfig.getRefreshEnabled());
        refreshLayout.setOnRefreshListener(
                () -> {
                    if (isDisplayingError) {
                        isDisplayingError = false;
                        mWebView.loadUrl(getUrl());
                    }
                    else {
                        mWebView.reload();
                    }
                }
        );

        mWebView = (WebView) fragmentView.findViewById(R.id.web_view);
        mWebView.setWebViewClient(mWebViewClient);
        mWebView.getSettings().setMediaPlaybackRequiresUserGesture(!getArguments().getBoolean("autoplay", false));
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        configureThirdPartyCookies();

        if (ContextUtils.isDebuggable(requireContext())) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webViewAdapter=new WebViewAdapter(mContext, new WebViewCallback() {
            @Override
            public void onClose() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       getActivity().finish();
                    }
                });

            }
        });

       mWebView.addJavascriptInterface(webViewAdapter , "Android");
            mWebView.loadUrl(getUrl());

        return fragmentView;
    }

    @SuppressLint("NewApi")
    private void configureThirdPartyCookies() {
        if(mConfig.isAcceptThirdPartyCookies()) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView,true);
        }
    }

    private String getUrl() {
        String url = null;
        if (getArguments() != null) {
            url = getArguments().getString("url");
        }
        if (TextUtils.isEmpty(url)) {
            return mConfig.getUrl();
        }
        return url;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        requireActivity().registerReceiver(networkStateReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        requireActivity().unregisterReceiver(networkStateReceiver);
    }

    @Override
    public boolean shouldDisplayToolbar() {
        return false;
    }

    @Override
    public boolean onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }else{
            getActivity().finishAffinity();
        }
        return false;
    }

    @Override
    public void onAppBarPressed() {
        mWebView.scrollTo(0, 0);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    mContext=context;
    }

    public String getTemplateName() {
        return "default_error_template.mustache";
    }
}
