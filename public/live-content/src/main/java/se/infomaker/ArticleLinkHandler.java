package se.infomaker;

import android.content.Context;

import se.infomaker.frtutilities.ConfigManager;
import se.infomaker.iap.articleview.item.links.Link;
import se.infomaker.iap.articleview.item.links.LinkHandler;
import se.infomaker.livecontentui.config.LiveContentUIConfig;
import se.infomaker.livecontentui.livecontentdetailview.activity.ArticlePagerActivity;

public class ArticleLinkHandler implements LinkHandler {
    @Override
    public void open(Context context, String moduleId, Link link, String title) {
        LiveContentUIConfig config = ConfigManager.getInstance(context).getConfig(moduleId, LiveContentUIConfig.class);
        if (config != null && config.getContentViewConfig() != null &&
                config.getContentViewConfig().getArticleLinkModuleId() != null) {
            moduleId = config.getContentViewConfig().getArticleLinkModuleId();
        }
        ArticlePagerActivity.openArticle(context, moduleId, title, link.getUuid());
    }
}
