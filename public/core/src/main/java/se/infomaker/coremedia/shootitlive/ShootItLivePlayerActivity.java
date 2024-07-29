package se.infomaker.coremedia.shootitlive;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.navigaglobal.mobile.R;

import se.infomaker.frtutilities.ktx.ContextUtils;

public class ShootItLivePlayerActivity extends AppCompatActivity {

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shoot_it_live_player);

        if (ContextUtils.isDebuggable(this)) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        mWebView = findViewById(R.id.web_view);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);

        if (getIntent() != null) {
            String contentId = getIntent().getStringExtra("id");
            String category = getIntent().getStringExtra("category");
            String client = getIntent().getStringExtra("client");
            String showAds = getIntent().getStringExtra("showAds");
            showAds = (showAds != null)? showAds : "false";

            if (contentId != null && category != null && client != null) {
                String data = String.format("<html>\n" +
                        "<body style=\"margin: 0; padding: 0; background: #000;\">\n" +
                        "<div class=\"shootitlive-embed\" data=\"share=false&category=%s&ads=%s&client=%s&project=%s\">\n" +
                        "<p>Live-published photos and videos via <a href=\"http://shootitlive.com/\">Shootitlive</a></p>\n" +
                        "</div>\n" +
                        "<script>\n" +
                        "var ShootItLive = function () {\n" +
                        "var d = document,\n" +
                        "b = d.body,\n" +
                        "s = d.createElement('script');\n" +
                        "s.src = \"//s3-eu-west-1.amazonaws.com/shootitlive/shootitlive.load.v1.1.%s.js\";\n" +
                        "b.insertBefore(s, b.firstChild);\n" +
                        "};\n" +
                        "ShootItLive()\n" +
                        "</script>\n" +
                        "</body>\n" +
                        "</html>\n", category, showAds, client, contentId, client);

                mWebView.loadDataWithBaseURL("http://shootitlive.com", data, "text/html", "utf-8", null);
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
