package se.infomaker.frtutilities;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * Android implementation of ResourceProvider
 * Provides access to resources based on current module identifier
 * Resources are first picked from
 */
public class ResourceManager implements ResourceProvider {
    public static final String DRAWABLE_TYPE = "drawable";
    public static final String LAYOUT_TYPE = "layout";
    public static final String BOOL_TYPE = "bool";
    public static final String STRING_TYPE = "string";
    public static final String RAW_TYPE = "raw";

    private static final String SHARED_PREFIX = "shared_";

    private final Context mContext;
    private final String mModuleIdentifier;
    private final String mPrefix;

    public ResourceManager(Context context, String moduleIdentifier) {
        mContext = context.getApplicationContext();
        mModuleIdentifier = moduleIdentifier;
        mPrefix = moduleIdentifier + (TextUtils.isEmpty(moduleIdentifier) ? "" : "_");
    }

    public String getModuleIdentifier() {
        return mModuleIdentifier;
    }

    /**
     * Get drawable identifier, the identifier is delivered in the first matching place searching
     * module, shared, global (no prefix).
     * @param resourceName
     * @return resource id or 0 if no match
     */
    public int getDrawableIdentifier(String resourceName) {
        return getIdentifier(DRAWABLE_TYPE, resourceName);
    }

    /**
     * Get String identifier, the identifier is delivered in the first matching place searching
     * module, shared, global (no prefix).
     * @param resourceName
     * @return resource id or 0 if no match
     */
    public int getStringIdentifier(String resourceName) {
        return getIdentifier(STRING_TYPE, resourceName);
    }

    /**
     * Get Raw identifier, the identifier is delivered in the first matching place searching
     * module, shared, global (no prefix).
     * @param resourceName
     * @return resource id or 0 if no match
     */
    public int getRawIdentifier(String resourceName) {
        return getIdentifier(RAW_TYPE, resourceName);
    }

    /**
     * Get string, from first matching identifier
     * @param resourceName name of identifier
     * @param fallback string if no string resource is found
     * @return resolved string
     */
    public String getString(String resourceName, String fallback) {
        int identifier = getStringIdentifier(resourceName);
        if (identifier != 0) {
            return mContext.getString(identifier);
        }
        return fallback;
    }

    /**
     * Get string, from first matching identifier
     * @param resourceName name of identifier
     * @param fallback string if no string resource is found
     * @param formatArgs arguments used to resolve format of the resourceName
     * @return resolved string
     */
    public String getString(String resourceName, String fallback, Object... formatArgs) {
        int identifier = getStringIdentifier(resourceName);
        if (identifier != 0) {
            return mContext.getString(identifier, formatArgs);
        }
        return fallback;
    }

    /**
     * Get layout identifier, the identifier is delivered in the first matching place searching
     * module, shared, global (no prefix).
     * @param resourceName
     * @return resource id or 0 if no match
     */
    public int getLayoutIdentifier(String resourceName) {
        return getIdentifier(LAYOUT_TYPE, resourceName);
    }

    /**
     * Get bool identifier, the identifier is delivered in the first matching place searching
     * module, shared, global (no prefix).
     * @param resourceName
     * @return resource id or 0 if no match
     */
    public int getBoolIdentifier(String resourceName) {
        return getIdentifier(BOOL_TYPE, resourceName);
    }

    private int getIdentifier(String type, String resourceName) {
        int identifier = mContext.getResources().getIdentifier(mPrefix + resourceName, type, mContext.getPackageName());
        if (identifier == 0) {
            identifier = mContext.getResources().getIdentifier(SHARED_PREFIX + resourceName, type, mContext.getPackageName());
            if (identifier == 0) {
                identifier = mContext.getResources().getIdentifier(resourceName, type, mContext.getPackageName());
            }
        }
        return identifier;
    }

    public String getAssetsPath() {
        if (mModuleIdentifier != null) {
            return "file:///android_asset/" + mModuleIdentifier + "/";
        }
        else {
            return "file:///android_asset/";
        }
    }

    /**
     * Returns the asset path for a resource
     * @param asset to get path for
     * @return file:///android_asset/ path
     * @throws IOException if the asset does not exist
     */
    public String getAssetPath(String asset) throws IOException {
        String path = mModuleIdentifier + "/" + asset;
        if (canOpen(path)) {
            return "file:///android_asset/" + path;
        }
        path = "shared/" + asset;
        if (canOpen(path)) {
            return "file:///android_asset/" + path;
        }
        path = asset;
        if (canOpen(path)) {
            return "file:///android_asset/" + path;
        }
        throw new IOException("File not found");
    }

    private boolean canOpen(String asset){
        try {
            mContext.getAssets().open(asset).close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Tries to open asset for path by accessing in the order module, shared, global
     * first match is served
     * @param asset
     * @return an InputStream of the asset
     *
     * @throws IOException
     */
    public InputStream getAssetStream(String asset) throws IOException {
        String path = asset.startsWith("/") ? asset.substring(1) : asset;
        try {
            return mContext.getAssets().open(mModuleIdentifier + "/" + path);
        } catch (IOException e) {
            try {
                return mContext.getAssets().open("shared/" + path);
            } catch (IOException e1) {
                return mContext.getAssets().open(path);
            }
        }
    }

    @Override
    public <T> T getAsset(String asset, Class<T> classOfT) throws IOException {
        return getAsset(asset, classOfT, new Gson());
    }

    public <T> T getAsset(String asset, Class<T> classOfT, Gson gson) throws IOException {
        Reader reader = null;
        try {
            reader = new InputStreamReader(getAssetStream(asset), StandardCharsets.UTF_8);
            return gson.fromJson(reader, classOfT);
        } finally {
            IOUtils.safeClose(reader);
        }
    }
}
