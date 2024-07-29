package se.infomaker.livecontentui.section;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.infomaker.livecontentui.section.ads.AdsSectionWrapper;
import se.infomaker.livecontentui.section.configuration.Orientation;
import se.infomaker.livecontentui.section.configuration.SectionConfigWrapper;
import se.infomaker.livecontentui.section.configuration.SectionedLiveContentUIConfig;
import se.infomaker.livecontentui.section.datasource.DataSource;
import se.infomaker.livecontentui.section.datasource.DataSourceProvider;
import se.infomaker.livecontentui.section.datasource.DataSourceSection;

public class SectionManager {
    private static final SectionManager INSTANCE = new SectionManager();
    private final Map<SectionConfigWrapper.Key, Section> register = new HashMap<>();

    private SectionManager() {
    }

    /**
     * Singleton instance getter.
     *
     * @return - singleton
     * @deprecated - Use {@link #getInstance()} instead.
     */
    @Deprecated
    public static SectionManager get() {
        return getInstance();
    }

    /**
     * Singleton instance getter.
     *
     * @return - singleton
     */
    public static SectionManager getInstance() {
        return INSTANCE;
    }

    public List<Section> create(DataSourceProvider provider, SectionedLiveContentUIConfig config, String moduleTitle) {
        return create(provider,config, moduleTitle, null);
    }

    public List<Section> create(DataSourceProvider provider, SectionedLiveContentUIConfig config, String moduleTitle, Orientation forceOrientation) {
        ArrayList<Section> list = new ArrayList<>();
        if (config != null && config.getSections() != null) {
            for (SectionConfigWrapper wrapperConfig : config.getSections()) {
                Orientation orientation = forceOrientation != null ? forceOrientation : wrapperConfig.getLayout();
                DataSource source = provider.getSource(wrapperConfig, orientation);
                if (source == null) {
                    continue;
                }
                Section section;
                SectionConfigWrapper.Key key = SectionConfigWrapper.createKey(wrapperConfig, orientation);
                if (register.containsKey(key)) {
                    section = register.get(key);
                } else {
                    section = new DataSourceSection(source);
                    register.put(key, section);
                }

                if (wrapperConfig.getAds() != null && wrapperConfig.getAds().getProviderConfiguration() != null &&
                        wrapperConfig.getAds().getProviderConfiguration().size() > 0) {
                    section = new AdsSectionWrapper(wrapperConfig.getAds(), section, moduleTitle);
                }
                list.add(section);
            }
        }
        return list;
    }

    public List<Section> current(SectionedLiveContentUIConfig config) {
        List<Section> out = new ArrayList<>();
        if (config != null && config.getSections() != null) {
            for (SectionConfigWrapper wrapperConfig : config.getSections()) {
                SectionConfigWrapper.Key key = SectionConfigWrapper.createKey(wrapperConfig);
                if (register.containsKey(key)) {
                    out.add(register.get(key));
                }
            }
        }
        return out;
    }
}
