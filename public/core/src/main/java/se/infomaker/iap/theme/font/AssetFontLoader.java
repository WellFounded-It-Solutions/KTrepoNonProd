package se.infomaker.iap.theme.font;

import android.content.Context;
import android.graphics.Typeface;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import se.infomaker.iap.theme.ThemeException;

/**
 * Loads fonts from assets based on filename
 */
public class AssetFontLoader implements FontLoader {
    private final String folderPath;
    private final Context context;
    private final Map<String, Typeface> cache = new HashMap<>();
    private final Set<String> missList = new HashSet<>();

    @NonNull
    public static AssetFontLoader createDefault(Context context) {
        return new AssetFontLoader(context, "shared/fonts");
    }

    public AssetFontLoader(Context context, String fontFolderPath) {
        this.context = context.getApplicationContext();
        this.folderPath = fontFolderPath.endsWith("/") ? fontFolderPath : fontFolderPath + "/";
    }

    public Typeface getTypeFace(String fontFileName) throws ThemeException {
        if (cache.containsKey(fontFileName)) {
            return cache.get(fontFileName);
        }
        if (missList.contains(fontFileName)) {
            throw new ThemeException("Failed to load font");
        }
        try {
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), folderPath + fontFileName);
            cache.put(fontFileName, typeface);
            return typeface;
        }
        catch (RuntimeException e) {
            missList.add(fontFileName);
            throw new ThemeException("Failed to load font", e);
        }
    }
}
