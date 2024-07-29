package se.infomaker.livecontentui.section.configuration;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import se.infomaker.frtutilities.ConfigManager;
import se.infomaker.iap.action.display.flow.MustacheUtilKt;
import se.infomaker.livecontentui.config.LiveContentUIConfig;
import se.infomaker.livecontentui.config.ThemeOverlayConfig;
import se.infomaker.livecontentui.section.datasource.list.ListSectionConfig;
import se.infomaker.livecontentui.section.datasource.newspackage.PackageSectionConfig;


public class SectionedLiveContentUIConfig extends LiveContentUIConfig {
    private List<SectionConfigWrapper> sections;
    private PackageSectionConfig packageNotificationConfiguration;
    private String loadingLayout;
    private boolean keepPositionOfScreen;
    private ThemeOverlayConfig themeOverlay;
    private boolean showBarPagerIndicator;

    public SectionedLiveContentUIConfig(SectionedLiveContentUIConfig from) {
        super(from);
        sections = from.sections;
        packageNotificationConfiguration = from.packageNotificationConfiguration;
        loadingLayout = from.loadingLayout;
        keepPositionOfScreen = from.keepPositionOfScreen;
        themeOverlay = from.themeOverlay;
        showBarPagerIndicator = from.showBarPagerIndicator;
    }

    public boolean keepPositionOfScreen() {
        return keepPositionOfScreen;
    }

    public List<SectionConfigWrapper> getSections() {
        return sections;
    }

    public String getLoadingLayout() {
        return loadingLayout;
    }

    public ThemeOverlayConfig getThemeOverlay() {
        return themeOverlay;
    }

    public boolean showBarPagerIndicator() {
        return showBarPagerIndicator;
    }

    public PackageSectionConfig getPackageNotificationConfiguration() {
        return packageNotificationConfiguration;
    }

    public SectionedLiveContentUIConfig forPackageNotification(String uuid) {
        if (packageNotificationConfiguration == null) {
            return null;
        }

        SectionedLiveContentUIConfig config = new SectionedLiveContentUIConfig(this);
        config.sections = new ArrayList<>();
        SectionConfigWrapper sectionConfigWrapper = new SectionConfigWrapper(packageNotificationConfiguration);

        // THIS IS NOT PRETTY!!! Refactor at will
        PackageSectionConfig configuration = sectionConfigWrapper.getConfiguration(PackageSectionConfig.class);
        String q = configuration.getQueryParams().get("q");
        if (TextUtils.isEmpty(q)) {
            q = "uuid=" + uuid;
        } else {
            q = "(" + q + ") AND uuid:" + uuid;
        }
        configuration.getQueryParams().put("q", q);
        sectionConfigWrapper.updateConfiguration(configuration);
        config.sections.add(sectionConfigWrapper);

        return config;
    }

    public SectionedLiveContentUIConfig packageOverlay(String uuid, String moduleName, String moduleId) {
        if (getContentViewConfiguration() != null) {
            String overlay = MustacheUtilKt.mustachify(getContentViewConfiguration().toString(), new SingleValueProvider("contentId", uuid));
            return ConfigManager.getInstance().getConfig(moduleName, moduleId, SectionedLiveContentUIConfig.class, overlay);
        }
        return this;
    }
}
