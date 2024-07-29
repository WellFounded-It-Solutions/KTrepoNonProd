package se.infomaker.livecontentui.section.configuration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import se.infomaker.livecontentmanager.config.PresentationBehaviour;
import se.infomaker.livecontentmanager.config.SublistConfig;
import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentui.config.ThemeOverlayConfig;

public abstract class SectionConfig {
    private String group;
    private String propertyMapReference;
    private String templatePrefix;
    private String header;
    private String footer;
    private ThemeOverlayConfig themeOverlayMapping;
    private boolean updateAllOnChange;
    private Map<String, String> queryParams;
    private Map<String, Map<String, String>> propertyQueryParams;
    private DividerConfig dividers;
    private PresentationBehaviour presentationBehaviour;
    @SerializedName("sublist")
    private SublistConfig sublistConfig;
    private ExtraContent extra;

    private String readTheme;

    public SectionConfig() {

    }


    public SublistConfig getSublistConfig() { return sublistConfig; }

    public DividerConfig getDividerConfig() {
        return dividers;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public String getPropertyMapReference() {
        return propertyMapReference;
    }

    public String getTemplatePrefix() {
        return templatePrefix;
    }

    public String getHeader() {
        return header;
    }

    public String getFooter() {
        return footer;
    }
    
    public boolean isUpdateAllOnChange() {
        return updateAllOnChange;
    }

    public String getGroup() {
        return group;
    }

    public Map<String, Map<String, String>> getPropertyQueryParams() {
        return propertyQueryParams;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SectionConfig that = (SectionConfig) o;

        if (propertyMapReference != null ? !propertyMapReference.equals(that.propertyMapReference) : that.propertyMapReference != null) {
            return false;
        }
        if (templatePrefix != null ? !templatePrefix.equals(that.templatePrefix) : that.templatePrefix != null) {
            return false;
        }
        if (header != null ? !header.equals(that.header) : that.header != null) {
            return false;
        }
        if (footer != null ? !footer.equals(that.footer) : that.footer != null) {
            return false;
        }

        return queryParams != null ? matchQueryParams(that) : that.queryParams == null;
    }

    private boolean matchQueryParams(SectionConfig that) {
        return queryParams.equals(that.queryParams);
    }

    @Override
    public int hashCode() {
        int result = propertyMapReference != null ? propertyMapReference.hashCode() : 0;
        result = 31 * result + (templatePrefix != null ? templatePrefix.hashCode() : 0);
        result = 31 * result + (header != null ? header.hashCode() : 0);
        result = 31 * result + (footer != null ? footer.hashCode() : 0);
        result = 31 * result + (queryParams != null ? queryParams.hashCode() : 0);
        return result;
    }

    public ThemeOverlayConfig getThemeOverlayMapping() {
        return themeOverlayMapping;
    }

    @Nullable
    public String resolveThemeOverlay(PropertyObject aPackage) {
        if (themeOverlayMapping != null) {
            return themeOverlayMapping.getOverlayThemeFile(aPackage);
        }
        return null;
    }

    @NonNull
    public List<String> resolveThemeOverlayAsList(PropertyObject aPackage) {
        if (themeOverlayMapping != null) {
            List<String> overlays = new ArrayList<>();
            overlays.add(themeOverlayMapping.getOverlayThemeFile(aPackage));
            return overlays;
        }
        return new ArrayList<>();
    }

    public String readTheme() {
        return readTheme;
    }

    public PresentationBehaviour getPresentationBehaviour() {
        return presentationBehaviour != null ? presentationBehaviour : PresentationBehaviour.DEFAULT;
    }
}
