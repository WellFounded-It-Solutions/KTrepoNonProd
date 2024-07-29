package se.infomaker.iap.theme;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Allos injection of extra theme layers.
 */
public class ThemeInjector {
    private static final ThemeInjector INSTANCE = new ThemeInjector();
    private final List<String> definitions = new ArrayList<>();

    private ThemeInjector() {}

    public static ThemeInjector getInstance() {
        return INSTANCE;
    }

    /**
     * Inject a theme definition as an extra layer on top of the default theme
     * @param definition theme definition to inject as an extra layer
     */
    public void inject(@NonNull String definition) {
        definitions.add(definition);
        ThemeManager.reset();
    }

    public List<String> getDefinitions() {
        return definitions;
    }
}
