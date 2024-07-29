package se.infomaker.coremedia.youtube;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.navigaglobal.mobile.R;

import se.infomaker.frtutilities.ktx.ContextUtils;

public class YouTubePlayerActivity extends AppCompatActivity {

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_player);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        if (ContextUtils.isDebuggable(this)) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        mWebView = findViewById(R.id.web_view);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.getSettings().setJavaScriptEnabled(true);

        if (getIntent() != null) {
            String id = getIntent().getStringExtra("id");

            if (id != null) {
                String data = String.format("<html><body style=\"margin: 0; padding: 0\"><iframe width=100%% height=100%% src='http://www.youtube.com/embed/%s' frameborder='0' allowfullscreen></iframe></body></html>", id);
                mWebView.loadData(data, "text/html", "utf-8");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWebView != null) {
            mWebView.onPause();
            mWebView.pauseTimers();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWebView != null) {
            mWebView.onResume();
            mWebView.resumeTimers();
        }
    }
}
