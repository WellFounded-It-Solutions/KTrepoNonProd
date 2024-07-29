package se.infomaker.iap.ui.theme;

import android.content.Context;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;
import se.infomaker.frtutilities.IOUtils;
import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.ThemeManager;
import se.infomaker.iap.ui.util.StringUtils;
import timber.log.Timber;

public class OverlayThemeProvider implements ThemeProvider {
    private static final Map<String, OverlayThemeProvider> moduleOverlayThemeProviders = new HashMap<>();
    private Theme baseTheme;
    private final ResourceManager resourceManager;
    private final Map<String, Theme> themeMap = new HashMap<>();
    private final Set<String> boomSet = new HashSet<>();
    private final ThemeManager themeManager;
    private final ThemeProvider baseThemeProvider;

    public synchronized static OverlayThemeProvider forModule(Context context, String moduleId) {
        if (moduleOverlayThemeProviders.containsKey(moduleId)) {
            return moduleOverlayThemeProviders.get(moduleId);
        }
        ThemeManager themeManager = ThemeManager.getInstance(context);
        OverlayThemeProvider overlayThemeProvider = new OverlayThemeProvider(themeManager, new ResourceManager(context, moduleId), new ThemeProvider() {
            @Override
            public Theme getTheme() {
                return themeManager.getModuleTheme(moduleId);
            }

            @Override
            public Theme getTheme(List<String> files) {
                return themeManager.getModuleTheme(moduleId);
            }
        });
        moduleOverlayThemeProviders.put(moduleId, overlayThemeProvider);
        return overlayThemeProvider;
    }

    public OverlayThemeProvider(ThemeManager themeManager, ResourceManager resourceManager, ThemeProvider baseThemeProvider) {
        this.themeManager = themeManager;
        this.baseThemeProvider = baseThemeProvider;
        this.baseTheme = baseThemeProvider.getTheme();
        this.resourceManager = resourceManager;
    }

    public void reset() {
        baseTheme = baseThemeProvider.getTheme();
        themeMap.clear();
        boomSet.clear();
    }

    @Override
    public Theme getTheme() {
        return baseTheme;
    }

    @Override
    public Theme getTheme(@Nullable List<String> overlays) {
        if (overlays == null || overlays.size() <= 0) {
            return baseTheme;
        }

        final String key = StringUtils.join(overlays, ":");
        if (themeMap.containsKey(key)) {
            return themeMap.get(key);
        }
        if (boomSet.contains(key)) {
            return baseTheme;
        }
        Theme layeredTheme = null;
        try {
            layeredTheme = Observable.fromArray(overlays.toArray()).map(overlay -> {
                String definition = read("configuration/" + overlay);
                if (definition == null) {
                    return "";
                }
                return definition;
            }).filter(definition -> !definition.isEmpty())
                    .reduce(baseTheme, (lastLayer, definition) -> {
                        try {
                            return themeManager.extendedFrom(lastLayer, resourceManager, new JSONObject(definition));
                        } catch (JSONException e) {
                            Timber.e(e, "Failed to parse theme");
                        }
                        return lastLayer;
                    }).toMaybe().blockingGet();
        }
        catch (Exception e) {
            Timber.e(e, "Failed to create layered theme, trying again");
            return getTheme(overlays);
        }


        if (layeredTheme == null) {
            themeMap.put(key, baseTheme);
            boomSet.add(key);
            return baseTheme;
        } else {
            themeMap.put(key, layeredTheme);
        }

        return layeredTheme;
    }

    private String read(String resource) {
        StringBuilder buf = new StringBuilder();

        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(resourceManager.getAssetStream(resource), "UTF-8"));
            String str;
            while ((str = in.readLine()) != null) {
                buf.append(str);
            }
            return buf.toString();
        } catch (IOException e) {
            Timber.w(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
        return null;
    }
}
