package se.infomaker.frtutilities;

import android.content.Context;
import android.webkit.WebViewClient;

import java.util.HashMap;
import java.util.Map;

public class ErrorUtil {

    public static final String WEBVIEW_ERROR_TITLE_RESOURCE = "webview_error_title";
    public static final String WEBVIEW_ERROR_SUBTITLE_RESOURCE = "webview_error_subtitle";
    public static final String WEBVIEW_ERROR_MESSAGE_RESOURCE = "webview_error_message";
    public static final String TITLE_KEY = "title";
    public static final String SUBTITLE_KEY = "subtitle";
    public static final String CODE_KEY = "code";
    public static final String DESCRIPTION_KEY = "description";
    public static final String URL_KEY = "url";
    public static final String MESSAGE_KEY = "message";

    /**
     * Creates a dictionary with localized error info to use in with template
     * @param context
     * @param resourceManager
     * @param errorCode
     * @return localized error Map
     */
    public static Map<String, Object> localizedErrorInfo(Context context, ResourceManager resourceManager, int errorCode) {
        HashMap<String, Object> data = new HashMap<>();
        putLocalized(context, resourceManager, data, TITLE_KEY, WEBVIEW_ERROR_TITLE_RESOURCE);
        putLocalized(context, resourceManager, data, MESSAGE_KEY, WEBVIEW_ERROR_MESSAGE_RESOURCE);
        data.put(CODE_KEY, errorCode);
        int errorCodeDescriptionResource = errorCodeDescriptionResource(resourceManager, errorCode);
        if (errorCodeDescriptionResource > 0) {
            data.put(DESCRIPTION_KEY, context.getString(errorCodeDescriptionResource));
        }
        return data;
    }

    private static void putLocalized(Context context, ResourceManager resourceManager, HashMap<String, Object> data, String key, String resourceName) {
        int resourceIdentifier = resourceManager.getStringIdentifier(resourceName);
        if (resourceIdentifier > 0) {
            data.put(key, context.getString(resourceIdentifier));
        }
    }

    public static int errorCodeDescriptionResource(ResourceManager manager, int code) {
        switch (code) {
            case WebViewClient.ERROR_HOST_LOOKUP: return manager.getStringIdentifier("webview_error_host_lookup");
            case WebViewClient.ERROR_UNSUPPORTED_AUTH_SCHEME: return manager.getStringIdentifier("webview_error_unsupported_auth_scheme");
            case WebViewClient.ERROR_AUTHENTICATION: return manager.getStringIdentifier("webview_error_authentication");
            case WebViewClient.ERROR_PROXY_AUTHENTICATION: return manager.getStringIdentifier("webview_error_proxy_authentication");
            case WebViewClient.ERROR_CONNECT: return manager.getStringIdentifier("webview_error_connect");
            case WebViewClient.ERROR_IO: return manager.getStringIdentifier("webview_error_io");
            case WebViewClient.ERROR_TIMEOUT: return manager.getStringIdentifier("webview_error_timeout");
            case WebViewClient.ERROR_REDIRECT_LOOP: return manager.getStringIdentifier("webview_error_redirect_loop");
            case WebViewClient.ERROR_UNSUPPORTED_SCHEME: return manager.getStringIdentifier("webview_error_unsupported_scheme");
            case WebViewClient.ERROR_FAILED_SSL_HANDSHAKE: return manager.getStringIdentifier("webview_error_failed_ssl_handshake");
            case WebViewClient.ERROR_BAD_URL: return manager.getStringIdentifier("webview_error_bad_url");
            case WebViewClient.ERROR_FILE: return manager.getStringIdentifier("webview_error_file");
            case WebViewClient.ERROR_FILE_NOT_FOUND: return manager.getStringIdentifier("webview_error_file_not_found");
            case WebViewClient.ERROR_TOO_MANY_REQUESTS: return manager.getStringIdentifier("webview_error_too_many_requests");
            case WebViewClient.ERROR_UNKNOWN: default: return manager.getStringIdentifier("webview_error_unknown_error");
                
        }
    }
}
