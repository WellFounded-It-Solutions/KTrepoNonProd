package se.infomaker.livecontentui.section.datasource.newspackage;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import se.infomaker.iap.articleview.item.author.DividerDecorationConfig;
import se.infomaker.livecontentmanager.parser.PropertyObject;
import com.navigaglobal.mobile.livecontent.R;
import se.infomaker.livecontentui.impressions.ContentTracker;
import se.infomaker.livecontentui.section.PropertyObjectSectionItem;
import se.infomaker.livecontentui.section.adapter.SectionItemViewHolder;
import se.infomaker.livecontentui.section.detail.DetailTemplateFragment;

public class PackageCoverSectionItem extends PropertyObjectSectionItem {

    private final String detailTemplate;

    public PackageCoverSectionItem(PropertyObject newsPackage, String sectionIdentifier, String templates, String templateReference, String groupKey, String detailTemplate, String overlayThemeFile, JSONObject context) {
        super(newsPackage, sectionIdentifier, groupKey, templates, templateReference, context);
        this.detailTemplate = detailTemplate;
        if (!TextUtils.isEmpty(overlayThemeFile)) {
            List<String> overlays = new ArrayList<>();
            overlays.add(overlayThemeFile);
            setOverlayThemes(overlays);
        }
    }

    @Override
    public void onDetach(SectionItemViewHolder viewHolder) {

    }

    @Override
    public int defaultTemplate() {
        return R.layout.package_cover_default;
    }

    @Override
    public boolean isClickable() {
        return false;
    }

    @NonNull
    @Override
    public DividerDecorationConfig getDividerConfig() {
        return NO_DIVIDER_CONFIG;
    }

    @Override
    public Fragment createDetailView(String moduleId) {
        return DetailTemplateFragment.createInstance(moduleId, "SectionContentList", detailTemplate, getPropertyObject(), overlayThemes(), getContext());
    }

    @Override
    public ContentTracker getContentTracker(String moduleId) {
        return null;
    }
}
