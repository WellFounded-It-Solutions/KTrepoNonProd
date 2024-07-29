package se.infomaker.iap.ui.theme;

import java.util.List;

import se.infomaker.iap.theme.Theme;

public interface ThemeProvider {
    Theme getTheme();
    Theme getTheme(List<String> files);
}
