package se.infomaker.livecontentui.livecontentrecyclerview.binder;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.infomaker.frtutilities.ConfigManager;
import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.iap.articleview.presentation.match.MatchMapKt;
import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentui.PresentationContextMatchable;
import se.infomaker.livecontentui.config.LiveContentUIConfig;
import se.infomaker.livecontentui.livecontentrecyclerview.image.ImageUrlBuilderFactory;
import se.infomaker.livecontentui.livecontentrecyclerview.image.ImageUrlFactoryProvider;
import se.infomaker.livecontentui.livecontentrecyclerview.view.LiveBinding;
import timber.log.Timber;

public class PropertyBinder {
    private static final Map<String, PropertyBinder> DEFAULT_BINDERS = new HashMap<>();

    private final ViewBinder binder;

    public PropertyBinder(ViewBinder binder) {
        this.binder = binder;
    }

    public Set<LiveBinding> bind(PropertyObject propertyObject, List<View> views) {
        return bind(propertyObject, views, null);
    }

    public Set<LiveBinding> bind(PropertyObject propertyObject, List<View> views, JSONObject context) {
        if (views == null || views.size() == 0) {
            Timber.d("No views to bind");
            return null;
        }
        HashSet<LiveBinding> updaters = new HashSet<>();
        for (View view : views) {
            if (view instanceof PresentationContextMatchable) {
                if (matchesPresentationContext((PresentationContextMatchable) view, propertyObject.getProperties(), context)) {
                    view.setVisibility(View.VISIBLE);
                }
                else {
                    view.setVisibility(View.GONE);
                    continue;
                }
            }
            try {
                String key = binder.getKey(view);
                if (key != null) {
                    LiveBinding updater = binder.bind(view, optString(propertyObject, key), propertyObject);
                    if (updater != null) {
                        updaters.add(updater);
                    }
                }
            } catch (Resources.NotFoundException ex) {
                Timber.e(ex);
            }
        }

        return updaters;
    }

    private boolean matchesPresentationContext(PresentationContextMatchable view, JSONObject properties, JSONObject context) {
        Map<String, List<String>> matchMap = view.getPresentationContextMatchMap();
        return matchMap == null || MatchMapKt.matches(matchMap, properties, context);
    }

    private String optString(PropertyObject propertyObject, String key) {
        if (".".equals(key)) {
            return propertyObject.getProperties().toString();
        }
        return propertyObject.optString(key);
    }

    public static synchronized PropertyBinder getModuleDefault(Context context, String moduleIdentifier) {
        PropertyBinder propertyBinder = DEFAULT_BINDERS.get(moduleIdentifier);
        if (propertyBinder != null) {
            return propertyBinder;
        }

        ResourceManager resourceManager = new ResourceManager(context, moduleIdentifier);
        LiveContentUIConfig config = ConfigManager.getInstance(context).getConfig(moduleIdentifier, LiveContentUIConfig.class);
        ImageUrlBuilderFactory imageUrlFactory = new ImageUrlFactoryProvider().provide(config.getImageProvider(), config.getImageBaseUrl());
        List<Double> imageSizes = null;
        try {
            imageSizes = config.getMedia().getImage().getSizes();
        } catch (Exception e) {
            Timber.e(e);
        }

        IMImageViewBinder imageViewBinder = new IMImageViewBinder(imageUrlFactory, imageSizes);
        IMTextViewBinder textViewBinder = new IMTextViewBinder(resourceManager);
        IMFrameLayoutBinder frameLayoutBinder = new IMFrameLayoutBinder();
        ViewBinder moduleDefaults = BinderCollection.with(imageViewBinder, textViewBinder, frameLayoutBinder);

        PropertyBinder modulePropertyBinder = new PropertyBinder(moduleDefaults);
        DEFAULT_BINDERS.put(moduleIdentifier, modulePropertyBinder);

        return modulePropertyBinder;
    }
}
