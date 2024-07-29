package se.infomaker.iap.theme.font;

import android.graphics.Typeface;

import se.infomaker.iap.theme.ThemeException;

/**
 * Provides fonts
 */
public interface FontLoader {
    Typeface getTypeFace(String fontFileName) throws ThemeException;
}
