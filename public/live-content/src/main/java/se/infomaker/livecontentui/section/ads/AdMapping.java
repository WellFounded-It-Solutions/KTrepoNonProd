package se.infomaker.livecontentui.section.ads;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import se.infomaker.frtutilities.JSONUtil;
import se.infomaker.library.OnAdFailedListener;
import se.infomaker.livecontentui.livecontentrecyclerview.adapter.AdInsertHelper;
import se.infomaker.livecontentui.livecontentrecyclerview.adapter.AdPosition;
import se.infomaker.livecontentui.section.ContentPresentationAware;
import se.infomaker.livecontentui.section.PropertyObjectSectionItem;
import se.infomaker.livecontentui.section.SectionItem;
import se.infomaker.livecontentui.section.configuration.AdsConfiguration;
import se.infomaker.livecontentui.section.ktx.SectionItemUtils;

public class AdMapping {

    private final AdInsertHelper adInsertHelper;
    private final AdsConfiguration configuration;
    private final String moduleTitle;
    private final OnAdFailedListener onAdFailed;
    private List<SectionItem> sectionItems;
    private List<Object> mapping;

    public AdMapping(AdsConfiguration configuration, String moduleTitle, OnAdFailedListener onAdFailed) {
        this.moduleTitle = moduleTitle;
        this.configuration = configuration;
        this.onAdFailed = onAdFailed;

        List<String> adBlockers = new ArrayList<>();
        adBlockers.add(AdsSectionWrapper.RELATED_KEY);
        this.adInsertHelper = new AdInsertHelper(configuration.getStartIndex(), configuration.getDistanceMin(), configuration.getDistanceMax(), configuration.getProviderConfiguration(), AdsSectionWrapper.ARTICLE_KEY, adBlockers);
    }

    public synchronized List<SectionItem> update(final List<SectionItem> sectionItems) {

        if (this.sectionItems != null) {

            if (this.sectionItems.size() > sectionItems.size()) {
                remove(this.sectionItems.size() - sectionItems.size());
            }
            if (this.sectionItems.size() < sectionItems.size()) {
                add(sectionItems.size() - this.sectionItems.size());
            }
        }
        else {
            mapping = new ArrayList<>();
            for (SectionItem sectionItem : sectionItems) {
                mapping.add(SectionItemUtils.isRelated(sectionItem) ? AdsSectionWrapper.RELATED_KEY : AdsSectionWrapper.ARTICLE_KEY);
            }
        }
        if (this.sectionItems == null || this.sectionItems.size() < sectionItems.size()) {
            adInsertHelper.fillAds(mapping);
        }
        this.sectionItems = sectionItems;

        ArrayList<SectionItem> insert = new ArrayList<>(sectionItems);
        ArrayList<SectionItem> out = new ArrayList<>();
        boolean followsAd = false;
        for (Object type : mapping) {
            if (insert.size() == 0) {
                break;
            }
            if (AdsSectionWrapper.ARTICLE_KEY.equals(type) || AdsSectionWrapper.RELATED_KEY.equals(type)) {
                SectionItem outItem = insert.remove(0);
                if (followsAd && outItem instanceof ContentPresentationAware) {
                    JSONObject context = ((ContentPresentationAware) outItem).getContext();
                    if (context == null) {
                        context = new JSONObject();
                    }
                    JSONUtil.put(context, "position.firstAfterAd", true);
                    ((ContentPresentationAware) outItem).setContext(context);
                    followsAd = false;
                }
                out.add(outItem);
            } else if (type instanceof AdPosition) {
                List<JSONObject> content = new ArrayList<>();

                if (out.size() > 0 && out.get(out.size() - 1) instanceof PropertyObjectSectionItem) {
                    content.add(((PropertyObjectSectionItem) out.get(out.size() - 1)).getPropertyObject().getProperties());
                }

                if (insert.size() > 0 && insert.get(0) instanceof PropertyObjectSectionItem) {
                    content.add(((PropertyObjectSectionItem) insert.get(0)).getPropertyObject().getProperties());
                }
                out.add(new AdSectionItem(configuration, moduleTitle, (AdPosition) type, content, onAdFailed));
                followsAd = true;
            }
        }
        return out;
    }

    private void add(int add) {
        for (int i = 0; i < add; i++) {
            mapping.add(AdsSectionWrapper.ARTICLE_KEY);
        }
    }

    private void remove(int remove) {
        for (int i = mapping.size() - 1; i > 0; i--) {
            Object s = mapping.remove(i);
            if (AdsSectionWrapper.ARTICLE_KEY.equals(s)) {
                remove--;
            }
            if (remove == 0) {
                break;
            }
        }
    }

    public synchronized void reset() {
        mapping = null;
        sectionItems = null;
        adInsertHelper.reset();
    }
}
