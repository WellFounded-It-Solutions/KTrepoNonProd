package se.infomaker.livecontentui.section.adapter;

import java.util.HashMap;
import java.util.Map;

import se.infomaker.livecontentui.config.TemplateConfig;
import se.infomaker.livecontentui.section.SectionAdapterData;
import se.infomaker.livecontentui.section.SectionItem;

class ViewTypeMapper {
    private final Map<String, TemplateConfig> templates;
    private Map<String, Integer> templateMap;
    private Map<Integer, Layout> typeLayoutMap = new HashMap<>();

    public ViewTypeMapper(Map<String, TemplateConfig> templates) {
        this.templates = templates;
    }

    public int getViewType(SectionItem item) {
        return typeForItem(item);
    }

    public int getLayoutResource(int viewType) {
        Layout layout = typeLayoutMap.get(viewType);
        if (layout != null) {
            return layout.layoutIdentifier;
        }
        return 0;
    }

    public ViewTypeMapper update(SectionAdapterData adapterData) {
        templateMap = adapterData.reverseTemplateMap;
        for (SectionItem item : adapterData.items) {
            reserveType(item);
        }
        return null;
    }

    private void reserveType(SectionItem item) {
        int type = typeForItem(item);
        if (!typeLayoutMap.containsKey(type)) {
            Integer identifier = templateMap.get(item.template());
            if (identifier == null) {
                identifier = item.defaultTemplate();
            }
            TemplateConfig config = templates.get(item.templateReference());
            typeLayoutMap.put(type, new Layout(type, identifier, config));
        }
    }

    private int typeForItem(SectionItem item) {
        String a = item.template() != null ? item.template() : "";
        String b = item.templateReference() != null ? item.templateReference() : "";
        Integer identifier = templateMap.get(item.template());
        if (identifier == null) {
            identifier = item.defaultTemplate();
        }
        return 2 * a.hashCode() + b.hashCode() + identifier;
    }

    public TemplateConfig getTemplateConfig(int viewType) {
        Layout layout = typeLayoutMap.get(viewType);
        if (layout != null) {
            return layout.config;
        }
        return null;
    }

    private static class Layout {
        final int layoutIdentifier;
        final TemplateConfig config;
        final int type;

        Layout(int type, int identifier, TemplateConfig config) {
            this.type = type;
            this.layoutIdentifier = identifier;
            this.config = config;
        }
    }
}
