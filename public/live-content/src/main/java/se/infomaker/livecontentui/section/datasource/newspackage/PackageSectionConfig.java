package se.infomaker.livecontentui.section.datasource.newspackage;

import se.infomaker.livecontentui.section.configuration.SectionConfig;

public class PackageSectionConfig extends SectionConfig {

    private String coverTemplatePrefix;
    private String coverDetailTemplate;

    public String getCoverTemplatePrefix() {
        return coverTemplatePrefix;
    }

    public String getCoverDetailTemplate() {
        return coverDetailTemplate;
    }
}
