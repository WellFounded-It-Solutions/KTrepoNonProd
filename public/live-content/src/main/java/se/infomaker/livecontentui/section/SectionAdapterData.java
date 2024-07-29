package se.infomaker.livecontentui.section;

import androidx.recyclerview.widget.DiffUtil;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class SectionAdapterData {
    public final List<SectionItem> items;
    public final Map<String, Integer> reverseTemplateMap;
    public final Map<Integer, String> templateToLayoutMap;
    public final DiffUtil.DiffResult diffResult;
    public final SectionState state;
    public final Date lastUpdated;

    public SectionAdapterData(List<SectionItem> items, Map<String, Integer> reverseTemplateMap, Map<Integer, String> templateToLayoutMap, DiffUtil.DiffResult diffResult, SectionState state, Date lastUpdated) {
        this.items = items;
        this.reverseTemplateMap = reverseTemplateMap;
        this.templateToLayoutMap = templateToLayoutMap;
        this.diffResult = diffResult;
        this.state = state;
        this.lastUpdated = lastUpdated;
    }
}
