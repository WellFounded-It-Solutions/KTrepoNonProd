package se.infomaker.frt.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.navigaglobal.mobile.pref.SaveGravitoPref;

import org.json.JSONObject;

import java.util.Locale;

import se.infomaker.frt.util.WebViewCallback;


public class WebViewAdapter {
   public  interface  GravitoStatus{
        String SAVE="save";
        String CLOSE="close";

   }
    private final WebViewCallback mWebViewCallback;
    Context mContext;
    /** Instantiate the interface and set the context */
    public WebViewAdapter(Context c,WebViewCallback webViewCallback) {
        mContext = c;
        mWebViewCallback=webViewCallback;

    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        //Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();

    }

    @JavascriptInterface
    public void getValueFromWebView(String value) {
       // webViewCallback.updateTextBox(value);

        try {
            JSONObject response = new JSONObject(value);
            String type = response.getString("type");
            switch (type){
                case GravitoStatus.SAVE:
                  saveGravitoData(value);
                    break;
                case GravitoStatus.CLOSE:
                    if(mWebViewCallback!=null) {
                        mWebViewCallback.onClose();
                    }
                    break;

            }

        }catch (Exception e){
            e.printStackTrace();
        }

     }

    private void saveGravitoData(String value) {
        SaveGravitoPref.INSTANCE.set(SaveGravitoPref.APP_PREFERENCES_KEY_TOKEN,value);
        SaveGravitoPref.INSTANCE.setGravitoKeys(value);

    }

    @JavascriptInterface
    public String getValueFromStorage(){
        String value=SaveGravitoPref.preferences.getString(SaveGravitoPref.APP_PREFERENCES_KEY_TOKEN, "");
        return value;
    }


    @JavascriptInterface
    public void onButtonClick(){
        ((Activity)mContext).finish();

    }



}
