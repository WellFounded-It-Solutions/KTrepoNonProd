package se.infomaker.livecontentui.section;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.Map;

import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentui.config.TemplateConfig;

public class LayoutResolver {
    public static final String PRIORITY = "priority";
    public static final String DEFAULT = "default";
    private final Map<String, TemplateConfig> templates;

    public LayoutResolver(Map<String, TemplateConfig> templates) {
        this.templates = templates;
    }

    public String getValidTemplate(PropertyObject propertyObject, String prefix) {
        return getValidTemplate(propertyObject, prefix, null);
    }

    @NonNull
    public String getValidTemplate(PropertyObject propertyObject, String prefix, String templateKey) {
        String templateType = getTemplate(propertyObject, templateKey);
        if (!TextUtils.isEmpty(prefix)) {
            TemplateConfig templateConfig = templates.get(prefix + templateType);
            if (templateConfig != null) {
                return templateConfig.getName();
            }
            templateConfig = templates.get(prefix + DEFAULT);
            return templateConfig != null ? templateConfig.getName() : "";
        }
        TemplateConfig config = templates.get(isValid(templateType, propertyObject) ? templateType : DEFAULT);
        return config != null ? config.getName() : "";
    }

    public String getValidTemplateReference(PropertyObject propertyObject, String prefix) {
        return getValidTemplateReference(propertyObject, prefix, null);
    }

    public String getValidTemplateReference(PropertyObject propertyObject, String prefix, String templateKey) {
        String templateType = getTemplate(propertyObject, templateKey);
        if (!TextUtils.isEmpty(prefix)) {
            return templates.get(prefix + templateType) != null ? prefix + templateType : "";
        }
        TemplateConfig config = templates.get(isValid(templateType, propertyObject) ? templateType : DEFAULT);
        return config != null ? templateType : "";
    }

    private String getTemplate(PropertyObject propertyObject, String templateKey) {
        return propertyObject.optString(PRIORITY, templateKey != null ? templateKey : DEFAULT);
    }

    private boolean isValid(String template, PropertyObject propertyObject) {
        TemplateConfig templateConfig = templates.get(template);
        if (templateConfig == null) {
            return false;
        }
        for (String property : templateConfig.getRequire()) {
            if (propertyObject.optString(property, null) == null) {
                return false;
            }
        }
        return true;
    }
}