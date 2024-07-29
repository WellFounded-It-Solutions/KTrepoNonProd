package se.infomaker.livecontentui.section.datasource.newspackage;

import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.util.List;

import se.infomaker.iap.articleview.item.author.DividerDecorationConfig;
import se.infomaker.livecontentmanager.parser.PropertyObject;
import com.navigaglobal.mobile.livecontent.R;
import se.infomaker.livecontentui.impressions.ContentTracker;
import se.infomaker.livecontentui.livecontentdetailview.pageadapters.UpdatableContentFragment;
import se.infomaker.livecontentui.section.PropertyObjectSectionItem;
import se.infomaker.livecontentui.section.ktx.SectionItemUtils;

public class ArticleSectionItem extends PropertyObjectSectionItem {

    private final DividerDecorationConfig dividerConfig;
    private final PropertyObjectSectionItemContentTracker tracker;

    public ArticleSectionItem(PropertyObject article, String sectionIdentifier, String groupKey, String template, String templateReference, List<String> overlayThemeFiles, DividerDecorationConfig dividerConfig, JSONObject context) {
        super(article, sectionIdentifier, groupKey, template, templateReference, context);
        setOverlayThemes(overlayThemeFiles);
        this.dividerConfig = dividerConfig;
        tracker = new PropertyObjectSectionItemContentTracker(this);
    }

    @Override
    public int defaultTemplate() {
        return SectionItemUtils.isRelated(this) ? R.layout.standard_related_teaser : R.layout.standard_default_teaser;
    }

    @Override
    public boolean isClickable() {
        return true;
    }

    @Override
    public Fragment createDetailView(String moduleId) {
        return UpdatableContentFragment.newInstance(moduleId, "SectionContentList", getPropertyObject().getProperties(), overlayThemes(), getContext(), getPropertyObject().getId());
    }

    @Override
    public ContentTracker getContentTracker(String moduleId) {
        tracker.setModuleId(moduleId);
        return tracker;
    }

    @Override
    public DividerDecorationConfig getDividerConfig() {
        return dividerConfig;
    }
}
