package se.infomaker.livecontentui.livecontentrecyclerview.binder;

import android.view.View;

import java.util.HashSet;
import java.util.Set;

import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentui.livecontentrecyclerview.view.IMFrameLayout;
import se.infomaker.livecontentui.livecontentrecyclerview.view.LiveBinding;

public class IMFrameLayoutBinder implements ViewBinder {
    private final Set<Class> supportedTypes;

    public IMFrameLayoutBinder() {
        supportedTypes = new HashSet<>();
        supportedTypes.add(IMFrameLayout.class);
    }

    @Override
    public LiveBinding bind(View view, String value, PropertyObject properties) {
        if (!(view instanceof IMFrameLayout)) {
            throw new RuntimeException("Unexpected view + " + view);
        }
        boolean show = true;
        IMFrameLayout layout = (IMFrameLayout) view;
        String propertyKey = layout.getPropertyKey();
        if (propertyKey != null) {
            //Does the article say this is false or not?
            show = Boolean.parseBoolean(properties.optString(propertyKey));
            if (layout.isShowOnFalse()) {
                show = !show;
            }
        }

        String goneOnMissing = layout.getGoneOnMissing();
        if (goneOnMissing != null) {
            String propertyValue = properties.optString(goneOnMissing);
            if (propertyValue == null) {
                show = false;
            }
        }

        layout.setVisibility(show ? View.VISIBLE : View.GONE);
        return null;
    }

    @Override
    public Set<Class> supportedViews() {
        return supportedTypes;
    }

    @Override
    public String getKey(View view) {
        if (!(view instanceof IMFrameLayout)) {
            throw new RuntimeException("Unexpected view + " + view);
        }
        String propertyKey = ((IMFrameLayout) view).getPropertyKey();
        if (propertyKey != null) {
            return propertyKey;
        }
        return ((IMFrameLayout) view).getGoneOnMissing();
    }
}
