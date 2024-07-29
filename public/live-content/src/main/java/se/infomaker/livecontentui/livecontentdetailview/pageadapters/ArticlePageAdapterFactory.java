package se.infomaker.livecontentui.livecontentdetailview.pageadapters;

import android.content.Context;
import android.text.TextUtils;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentui.DetailViewRegistry;
import se.infomaker.livecontentui.config.LiveContentUIConfig;
import se.infomaker.livecontentui.config.ThemeOverlayConfig;
import se.infomaker.livecontentui.livecontentdetailview.adapter.ArticleFragmentStatePagerAdapter;

public class ArticlePageAdapterFactory {
    public static final String READ_ARTICLE = "featureConfiguration";
    public static final String MUSTACHE_TEMPLATE_CONTENT_VIEW = "mustacheTemplate";
    final Context context;
    final List<PropertyObject> hitsLists;
    final LiveContentUIConfig config;
    final String moduleId;
    private final DetailFragmentFactory factory;

    private ArticlePageAdapterFactory(Context context, DetailFragmentFactory factory, List<PropertyObject> hitsListList, LiveContentUIConfig config, String moduleId) {
        this.factory = factory;
        this.context = context;
        this.hitsLists = hitsListList;
        this.config = config;
        this.moduleId = moduleId;
    }

    public static ArticlePageAdapterFactory getFactory(Context articlePagerActivity, List<PropertyObject> hitsListList, LiveContentUIConfig config, String moduleId) {
        String contentView = config.getContentView();
        DetailFragmentFactory factory = null;
        if (contentView != null) {
            factory = DetailViewRegistry.INSTANCE.get(contentView);
        }
        if (factory == null) {
            factory = NativeContentFragmentFactory.SHARED_INSTANCE;
        }
        return new ArticlePageAdapterFactory(articlePagerActivity, factory, hitsListList, config, moduleId);
    }

    public ArticleFragmentStatePagerAdapter getPageAdapter(FragmentManager fm, ThemeOverlayConfig themeOverlayConfig) {
        return new ArticleFragmentStatePagerAdapter(fm, hitsLists) {
            @Override
            public Fragment getItem(int position) {
                return createFragment(position, themeOverlayConfig);
            }
        };
    }

    public Fragment createFragment(int position, ThemeOverlayConfig themeOverlayConfig) {
        PropertyObject object = hitsLists.get(position);
        String themeFile = null;
        if (themeOverlayConfig != null) {
            themeFile = themeOverlayConfig.getOverlayThemeFile(object);
        }

        List<String> overlay = null;
        if (!TextUtils.isEmpty(themeFile)) {
            overlay = new ArrayList<>();
            overlay.add(themeFile);
        }
        return factory.createFragment(context, moduleId, object, overlay);
    }
}
