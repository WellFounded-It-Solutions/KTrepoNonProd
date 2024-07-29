package se.infomaker.coremedia.coveritlive;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.navigaglobal.mobile.R;

import se.infomaker.frtutilities.ktx.ContextUtils;

public class CoverItLivePlayerActivity extends AppCompatActivity {

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cover_it_live_player);

        if (ContextUtils.isDebuggable(this)) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        mWebView = findViewById(R.id.web_view);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);

        if (getIntent() != null) {
            String contentId = getIntent().getStringExtra("id");

            if (contentId != null) {
                String url = String.format("http://www.coveritlive.com/embed.html?altcastCode=%s&srcdom=www.coveritlive.com&srcdomsec=wwwssl.coveritlive.com&width=\"fit\"&height=\"infinite\"&entryLoc=top&commentLoc=top&titlePage=on&replayContentOrder=chronological&embedType=stream&pinsGrowSize=on", contentId);
                mWebView.loadUrl(url);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int color = getIntent().getIntExtra("color", Color.BLACK);
                Window window = getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(color);
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
